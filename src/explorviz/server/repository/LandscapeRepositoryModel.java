package explorviz.server.repository;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryo.Kryo;

import explorviz.live_trace_processing.reader.IPeriodicTimeSignalReceiver;
import explorviz.live_trace_processing.reader.TimeSignalReader;
import explorviz.live_trace_processing.record.IRecord;
import explorviz.live_trace_processing.record.event.*;
import explorviz.live_trace_processing.record.event.constructor.BeforeConstructorEventRecord;
import explorviz.live_trace_processing.record.event.remote.*;
import explorviz.live_trace_processing.record.misc.SystemMonitoringRecord;
import explorviz.live_trace_processing.record.trace.HostApplicationMetaDataRecord;
import explorviz.live_trace_processing.record.trace.Trace;
import explorviz.server.export.rsf.RigiStandardFormatExporter;
import explorviz.server.main.Configuration;
import explorviz.server.repository.helper.Signature;
import explorviz.server.repository.helper.SignatureParser;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

public class LandscapeRepositoryModel implements IPeriodicTimeSignalReceiver {
	private static final String DEFAULT_COMPONENT_NAME = "(default)";
	private static final boolean LOAD_LAST_LANDSCAPE_ON_LOAD = false;

	private Landscape lastPeriodLandscape;
	private final Landscape internalLandscape;
	private final Kryo kryo;

	private final Map<SentRemoteCallRecord, Long> sentRemoteCallRecordCache = new HashMap<SentRemoteCallRecord, Long>();
	private final Map<ReceivedRemoteCallRecord, Long> receivedRemoteCallRecordCache = new HashMap<ReceivedRemoteCallRecord, Long>();

	private final Map<String, Node> nodeCache = new HashMap<String, Node>();
	private final Map<String, Application> applicationCache = new HashMap<String, Application>();
	private final Map<Application, Map<String, Clazz>> clazzCache = new HashMap<Application, Map<String, Clazz>>();

	static {
		Configuration.databaseNames.add("hsqldb");
		Configuration.databaseNames.add("postgres");
		Configuration.databaseNames.add("db2");
		Configuration.databaseNames.add("mysql");
	}

	public LandscapeRepositoryModel() {
		kryo = initKryo();

		if (LOAD_LAST_LANDSCAPE_ON_LOAD) {
			Landscape readLandscape = null;
			try {
				readLandscape = RepositoryStorage
						.readFromFile(java.lang.System.currentTimeMillis());
			} catch (final FileNotFoundException e) {
				readLandscape = new Landscape();
			}

			internalLandscape = readLandscape;
		} else {
			internalLandscape = new Landscape();
		}

		updateLandscapeAccess();

		lastPeriodLandscape = LandscapePreparer.prepareLandscape(kryo.copy(internalLandscape));

		new TimeSignalReader(TimeUnit.SECONDS.toMillis(Configuration.outputIntervalSeconds), this)
				.start();
	}

	public Kryo initKryo() {
		final Kryo result = new Kryo();
		result.register(Landscape.class);
		result.register(System.class);
		result.register(NodeGroup.class);
		result.register(Node.class);
		result.register(Communication.class);
		result.register(Application.class);
		result.register(Component.class);
		result.register(CommunicationClazz.class);
		result.register(Clazz.class);

		return result;
	}

	private void updateLandscapeAccess() {
		internalLandscape.setHash(java.lang.System.nanoTime());
	}

	public final Landscape getLastPeriodLandscape() {
		synchronized (lastPeriodLandscape) {
			return lastPeriodLandscape;
		}
	}

	public final Landscape getLandscape(final long timestamp) throws FileNotFoundException {
		return LandscapePreparer.prepareLandscape(RepositoryStorage.readFromFile(timestamp));
	}

	public final Map<Long, Long> getAvailableLandscapes() {
		return RepositoryStorage.getAvailableModelsForTimeshift();
	}

	public void reset() {
		synchronized (internalLandscape) {
			internalLandscape.getApplicationCommunication().clear();
			internalLandscape.getSystems().clear();
			internalLandscape.setActivities(0L);
			updateLandscapeAccess();
		}
	}

	@Override
	public void periodicTimeSignal(final long timestamp) {
		synchronized (internalLandscape) {
			synchronized (lastPeriodLandscape) {
				RepositoryStorage.writeToFile(internalLandscape,
						java.lang.System.currentTimeMillis());
				lastPeriodLandscape = LandscapePreparer.prepareLandscape(kryo
						.copy(internalLandscape));

				updateRemoteCalls();

				resetCommunication();
			}
		}

		RepositoryStorage.cleanUpTooOldFiles(java.lang.System.currentTimeMillis());
	}

	private void updateRemoteCalls() {
		final long currentTime = java.lang.System.nanoTime();

		final List<ReceivedRemoteCallRecord> toRemove = new ArrayList<ReceivedRemoteCallRecord>();

		for (final Entry<ReceivedRemoteCallRecord, Long> receivEntry : receivedRemoteCallRecordCache
				.entrySet()) {
			if ((currentTime - TimeUnit.SECONDS.toNanos(30)) > receivEntry.getValue()) {
				toRemove.add(receivEntry.getKey());
			}
		}

		// caller = new
		// HostApplicationMetaDataRecord("<Unknown-Host>",
		// "<Unknown-App>");
		// TODO create communication for blackboxes
		// seekOrCreateCommunication(caller, receivedRemoteCallRecord);

		for (final ReceivedRemoteCallRecord toRemoveRecord : toRemove) {
			sentRemoteCallRecordCache.remove(toRemoveRecord);
		}

		final List<SentRemoteCallRecord> toRemoveSent = new ArrayList<SentRemoteCallRecord>();

		for (final Entry<SentRemoteCallRecord, Long> sentEntry : sentRemoteCallRecordCache
				.entrySet()) {
			if ((currentTime - TimeUnit.SECONDS.toNanos(30)) > sentEntry.getValue()) {
				toRemoveSent.add(sentEntry.getKey());
			}
		}

		for (final SentRemoteCallRecord toRemoveRecord : toRemoveSent) {
			sentRemoteCallRecordCache.remove(toRemoveRecord);
		}
	}

	private void resetCommunication() {
		internalLandscape.setActivities(0L);

		for (final Application application : applicationCache.values()) {
			final Map<String, Clazz> clazzes = clazzCache.get(application);
			for (final Clazz clazz : clazzes.values()) {
				clazz.getObjectIds().clear();
				clazz.setInstanceCount(0);
			}

			for (final explorviz.shared.model.CommunicationClazz commu : application
					.getCommunications()) {
				commu.reset();
			}
		}

		for (final Communication commu : internalLandscape.getApplicationCommunication()) {
			commu.setRequests(0);
		}

		updateLandscapeAccess();
	}

	public void insertIntoModel(final IRecord inputIRecord) {
		if (inputIRecord instanceof Trace) {
			final Trace trace = (Trace) inputIRecord;

			if (Configuration.rsfExportEnabled) {
				RigiStandardFormatExporter.insertTrace(trace);
			}

			final HostApplicationMetaDataRecord hostApplicationRecord = trace.getTraceEvents()
					.get(0).getHostApplicationMetadata();

			synchronized (internalLandscape) {
				final Node node = seekOrCreateNode(hostApplicationRecord);
				final Application application = seekOrCreateApplication(node,
						hostApplicationRecord.getApplication());
				// TODO check if node should be placed in a different nodeGroup

				createCommunicationInApplication(trace, hostApplicationRecord.getHostname(),
						application);

				updateLandscapeAccess();
			}
		} else if (inputIRecord instanceof SystemMonitoringRecord) {
			final SystemMonitoringRecord systemMonitoringRecord = (SystemMonitoringRecord) inputIRecord;

			for (final Node node : nodeCache.values()) {
				if (node.getName().equalsIgnoreCase(
						systemMonitoringRecord.getHostApplicationMetadata().getHostname())
						&& node.getIpAddress().equalsIgnoreCase(
								systemMonitoringRecord.getHostApplicationMetadata().getIpaddress())) {
					node.setCpuUtilization(systemMonitoringRecord.getCpuUtilization());
					node.setFreeRAM(systemMonitoringRecord.getAbsoluteRAM()
							- systemMonitoringRecord.getUsedRAM());
					node.setUsedRAM(systemMonitoringRecord.getUsedRAM());
				}
			}
		}
	}

	private Node seekOrCreateNode(final HostApplicationMetaDataRecord hostApplicationRecord) {
		final String nodeName = hostApplicationRecord.getHostname() + "_"
				+ hostApplicationRecord.getIpaddress();
		Node node = nodeCache.get(nodeName);

		if (node == null) {
			node = new Node();
			node.setIpAddress(hostApplicationRecord.getIpaddress());
			node.setName(hostApplicationRecord.getHostname());
			nodeCache.put(nodeName, node);

			final System system = seekOrCreateSystem(hostApplicationRecord.getSystemname());
			seekOrCreateNodeGroup(system, node);
		}

		return node;
	}

	private System seekOrCreateSystem(final String systemname) {
		for (final System system : internalLandscape.getSystems()) {
			if (system.getName().equalsIgnoreCase(systemname)) {
				return system;
			}
		}

		final System system = new System();
		system.setName(systemname);
		system.setParent(internalLandscape);
		internalLandscape.getSystems().add(system);

		return system;
	}

	private NodeGroup seekOrCreateNodeGroup(final System system, final Node node) {
		for (final NodeGroup existingNodeGroup : system.getNodeGroups()) {
			if (!existingNodeGroup.getNodes().isEmpty()) {
				if (nodeMatchesNodeType(node, existingNodeGroup.getNodes().get(0))) {
					final List<String> ipAddresses = new ArrayList<String>();
					ipAddresses.add(node.getIpAddress());
					for (final Node existingNode : existingNodeGroup.getNodes()) {
						ipAddresses.add(existingNode.getIpAddress());
					}

					existingNodeGroup.setName(getStartAndEndRangeForNodeGroup(ipAddresses));
					existingNodeGroup.getNodes().add(node);
					node.setParent(existingNodeGroup);

					return existingNodeGroup;
				}
			}
		}

		final NodeGroup nodeGroup = new NodeGroup();
		nodeGroup.setName(node.getIpAddress());
		nodeGroup.getNodes().add(node);
		node.setParent(nodeGroup);

		system.getNodeGroups().add(nodeGroup);
		nodeGroup.setParent(system);

		return nodeGroup;
	}

	private String getStartAndEndRangeForNodeGroup(final List<String> ipAddresses) {
		Collections.sort(ipAddresses);
		if (ipAddresses.size() >= 2) {
			return ipAddresses.get(0) + " - " + ipAddresses.get(ipAddresses.size() - 1);
		} else if (ipAddresses.size() == 1) {
			return ipAddresses.get(0);
		}

		return "";
	}

	private boolean nodeMatchesNodeType(final Node node, final Node node2) {
		if (node.getApplications().size() != node2.getApplications().size()) {
			return false;
		}

		for (final Application app1 : node.getApplications()) {
			boolean found = false;
			for (final Application app2 : node2.getApplications()) {
				if (app1.getName().equalsIgnoreCase(app2.getName())) {
					found = true;
				}
			}
			if (found == false) {
				return false;
			}
		}

		return true;
	}

	private Application seekOrCreateApplication(final Node node, final String applicationName) {
		Application application = applicationCache.get(applicationName);

		if (application == null) {
			application = new Application();

			application.setDatabase(isApplicationDatabase(applicationName));
			application.setId((node.getName() + "_" + applicationName).hashCode());
			application.setLastUsage(java.lang.System.currentTimeMillis());
			application.setName(applicationName);
			application.setParent(node);

			// big SESoS paper hack...

			// landscape.getApplicationCommunication().clear();
			//
			// final Communication communication = new Communication();
			// communication.setSource(new Application());
			// communication.getSource().setName("Webinterface");
			// communication.getSource().setId((node.getName() + "_" +
			// "Webinterface").hashCode());
			// communication.setTarget(application);
			// communication.setTargetClazz(new Clazz());
			// communication.getTargetClazz().setFullQualifiedName("EPrints");
			// communication.setRequests(30);
			// landscape.getApplicationCommunication().add(communication);
			//
			// final Communication communication2 = new Communication();
			// communication2.setSource(application);
			// communication2.setTarget(new Application());
			// communication2.getTarget().setName("Database");
			// communication2.getTarget().setId((node.getName() + "_" +
			// "Database").hashCode());
			// communication2.setSourceClazz(new Clazz());
			// communication2.getSourceClazz().setFullQualifiedName("EPrints.Database.Pg");
			// communication2.setRequests(30);
			// landscape.getApplicationCommunication().add(communication2);

			node.getApplications().add(application);
			applicationCache.put(applicationName, application);
			// node.getApplications().add(communication.getSource());
			// node.getApplications().add(communication2.getTarget());
		}
		return application;
	}

	private boolean isApplicationDatabase(final String applicationName) {
		boolean isDatabase = false;
		final List<String> databaseNames = Configuration.databaseNames;
		for (final String databaseName : databaseNames) {
			if (applicationName.toLowerCase().contains(databaseName)) {
				isDatabase = true;
				break;
			}
		}
		return isDatabase;
	}

	private void createCommunicationInApplication(final Trace trace, final String currentHostname,
			final Application currentApplication) {
		Clazz callerClazz = null;
		final Stack<Clazz> callerClazzesHistory = new Stack<Clazz>();

		int orderIndex = 1;
		double overallTraceDuration = -1d;

		for (final AbstractEventRecord event : trace.getTraceEvents()) {
			if (event instanceof AbstractBeforeEventRecord) {
				final AbstractBeforeEventRecord abstractBeforeEventRecord = (AbstractBeforeEventRecord) event;

				if (overallTraceDuration < 0d) {
					overallTraceDuration = abstractBeforeEventRecord
							.getRuntimeStatisticInformation().getAverage();
				}

				final String clazzName = getClazzName(abstractBeforeEventRecord);

				final Clazz currentClazz = seekOrCreateClazz(clazzName, currentApplication,
						abstractBeforeEventRecord.getRuntimeStatisticInformation().getObjectIds());

				if (callerClazz != null) {
					final boolean isConstructor = abstractBeforeEventRecord instanceof BeforeConstructorEventRecord;
					final String methodName = getMethodName(
							abstractBeforeEventRecord.getOperationSignature(), isConstructor);

					boolean isAbstractConstructor = false;

					if (isConstructor) {
						final BeforeConstructorEventRecord constructor = (BeforeConstructorEventRecord) abstractBeforeEventRecord;
						final String constructorClass = constructor.getClazz().substring(
								constructor.getClazz().lastIndexOf('.') + 1);
						final String constructorClassFromOperation = methodName.substring(4);

						isAbstractConstructor = !constructorClass
								.equalsIgnoreCase(constructorClassFromOperation);
					}

					if (!isAbstractConstructor) {
						createOrUpdateCall(callerClazz, currentClazz, currentApplication,
								trace.getCalledTimes(), abstractBeforeEventRecord
										.getRuntimeStatisticInformation().getCount(),
								abstractBeforeEventRecord.getRuntimeStatisticInformation()
										.getAverage(), overallTraceDuration,
								abstractBeforeEventRecord.getTraceId(), orderIndex, methodName);
						orderIndex++;
					}
				}

				callerClazz = currentClazz;
				callerClazzesHistory.push(currentClazz);
			} else if ((event instanceof AbstractAfterEventRecord)
					|| (event instanceof AbstractAfterFailedEventRecord)) {
				if (!callerClazzesHistory.isEmpty()) {
					callerClazzesHistory.pop();
				}
				if (!callerClazzesHistory.isEmpty()) {
					callerClazz = callerClazzesHistory.peek();
				}
			} else if (event instanceof SentRemoteCallRecord) {
				final SentRemoteCallRecord sentRemoteCallRecord = (SentRemoteCallRecord) event;

				final ReceivedRemoteCallRecord receivedRecord = seekMatchingReceivedRemoteRecord(sentRemoteCallRecord);

				if (receivedRecord == null) {
					sentRemoteCallRecordCache
							.put(sentRemoteCallRecord, java.lang.System.nanoTime());
				} else {
					seekOrCreateCommunication(sentRemoteCallRecord, receivedRecord);
				}
			} else if (event instanceof ReceivedRemoteCallRecord) {
				final ReceivedRemoteCallRecord receivedRemoteCallRecord = (ReceivedRemoteCallRecord) event;

				final SentRemoteCallRecord sentRecord = seekSentRemoteTraceIDandOrderID(receivedRemoteCallRecord);

				if (sentRecord == null) {
					receivedRemoteCallRecordCache.put(receivedRemoteCallRecord,
							java.lang.System.nanoTime());
				} else {
					seekOrCreateCommunication(sentRecord, receivedRemoteCallRecord);
				}
			} else if (event instanceof UnknownReceivedRemoteCallRecord) {
				// TODO unknown recv remote classes
			}
		}
	}

	public static String getClazzName(final AbstractBeforeEventRecord abstractBeforeEventRecord) {
		String clazzName = abstractBeforeEventRecord.getClazz();

		if (clazzName.contains("$")) {
			// found an anonymous class
			final String implementedInterface = abstractBeforeEventRecord.getImplementedInterface();

			if ((implementedInterface != null) && !implementedInterface.isEmpty()) {
				final int lastIndexOfDollar = clazzName.lastIndexOf('$');
				if ((lastIndexOfDollar > -1) && ((lastIndexOfDollar + 1) < clazzName.length())) {
					final char suffixChar = clazzName.charAt(lastIndexOfDollar + 1);
					if (('0' <= suffixChar) && (suffixChar <= '9')) {
						String interfaceName = implementedInterface;
						final int interfaceNameIndex = interfaceName.lastIndexOf('.');
						if (interfaceNameIndex > -1) {
							interfaceName = interfaceName.substring(interfaceNameIndex + 1);
						}

						clazzName = clazzName.substring(0, lastIndexOfDollar + 1) + "["
								+ interfaceName + "]" + clazzName.substring(lastIndexOfDollar + 1);
					}
				}
			}
		}
		return clazzName;
	}

	private ReceivedRemoteCallRecord seekMatchingReceivedRemoteRecord(
			final SentRemoteCallRecord sentRecord) {
		for (final ReceivedRemoteCallRecord receivedRemoteRecord : receivedRemoteCallRecordCache
				.keySet()) {
			if ((receivedRemoteRecord.getCallerTraceId() == sentRecord.getTraceId())
					&& (receivedRemoteRecord.getCallerOrderIndex() == sentRecord.getOrderIndex())) {
				receivedRemoteCallRecordCache.remove(receivedRemoteRecord);

				return receivedRemoteRecord;
			}
		}

		return null;
	}

	private SentRemoteCallRecord seekSentRemoteTraceIDandOrderID(
			final ReceivedRemoteCallRecord remoteRecord) {
		for (final SentRemoteCallRecord sentRemoteRecord : sentRemoteCallRecordCache.keySet()) {
			if ((sentRemoteRecord.getTraceId() == remoteRecord.getCallerTraceId())
					&& (sentRemoteRecord.getOrderIndex() == remoteRecord.getCallerOrderIndex())) {
				sentRemoteCallRecordCache.remove(sentRemoteRecord);

				return sentRemoteRecord;
			}
		}

		return null;
	}

	private void seekOrCreateCommunication(final SentRemoteCallRecord sentRemoteCallRecord,
			final ReceivedRemoteCallRecord receivedRemoteCallRecord) {
		final Node callerHost = seekOrCreateNode(sentRemoteCallRecord.getHostApplicationMetadata());
		final Application callerApplication = seekOrCreateApplication(callerHost,
				sentRemoteCallRecord.getHostApplicationMetadata().getApplication());

		final Node currentHost = seekOrCreateNode(receivedRemoteCallRecord
				.getHostApplicationMetadata());
		final Application currentApplication = seekOrCreateApplication(currentHost,
				receivedRemoteCallRecord.getHostApplicationMetadata().getApplication());

		if (callerApplication == currentApplication) {
			return;
		}

		for (final Communication commu : internalLandscape.getApplicationCommunication()) {
			if (((commu.getSource() == callerApplication) && (commu.getTarget() == currentApplication))
					|| ((commu.getSource() == currentApplication) && (commu.getTarget() == callerApplication))) {
				commu.setRequests(commu.getRequests() + 1);
				return;
			}
		}

		final Communication communication = new Communication();
		communication.setSource(callerApplication);
		communication.setTarget(currentApplication);
		communication.setRequests(1);
		internalLandscape.getApplicationCommunication().add(communication);
	}

	private void createOrUpdateCall(final Clazz caller, final Clazz callee,
			final Application application, final int calledTimes, final int requests,
			final double average, final double overallTraceDuration, final long traceId,
			final int orderIndex, final String methodName) {
		internalLandscape.setActivities(internalLandscape.getActivities() + requests);

		for (final CommunicationClazz commu : application.getCommunications()) {
			if (((commu.getSource() == caller) && (commu.getTarget() == callee) && (commu
					.getMethodName().equalsIgnoreCase(methodName)))) {

				commu.addRuntimeInformation(traceId, calledTimes, orderIndex, requests
						/ calledTimes, (float) average, (float) overallTraceDuration);
				return;
			}
		}

		final CommunicationClazz commu = new CommunicationClazz();

		commu.setSource(caller);
		commu.setTarget(callee);

		commu.addRuntimeInformation(traceId, calledTimes, orderIndex, requests / calledTimes,
				(float) average, (float) overallTraceDuration);
		commu.setMethodName(methodName);

		application.getCommunications().add(commu);
	}

	private Clazz seekOrCreateClazz(final String fullQName, final Application application,
			final Set<Integer> objectIds) {
		final String[] splittedName = fullQName.split("\\.");

		Map<String, Clazz> appCached = clazzCache.get(application);
		if (appCached == null) {
			appCached = new HashMap<String, Clazz>();
			clazzCache.put(application, appCached);
		}
		Clazz clazz = appCached.get(fullQName);

		if (clazz == null) {
			clazz = seekrOrCreateClazzHelper(fullQName, splittedName, application, null, 0);
			appCached.put(fullQName, clazz);
		}

		if (objectIds != null) {
			clazz.getObjectIds().addAll(objectIds);
			clazz.setInstanceCount(clazz.getObjectIds().size());
		}

		return clazz;
	}

	private Clazz seekrOrCreateClazzHelper(final String fullQName, final String[] splittedName,
			final Application application, Component parent, final int index) {
		final String currentPart = splittedName[index];

		if (index < (splittedName.length - 1)) {
			List<Component> list = null;

			if (parent == null) {
				list = application.getComponents();
			} else {
				list = parent.getChildren();
			}

			for (final Component component : list) {
				if (component.getName().equalsIgnoreCase(currentPart)) {
					return seekrOrCreateClazzHelper(fullQName, splittedName, application,
							component, index + 1);
				}
			}
			final Component component = new Component();
			String fullQNameComponent = "";
			for (int i = 0; i <= index; i++) {
				fullQNameComponent += splittedName[i] + ".";
			}
			fullQNameComponent = fullQNameComponent.substring(0, fullQNameComponent.length() - 1);
			component.setFullQualifiedName(fullQNameComponent);
			component.setName(currentPart);
			component.setParentComponent(parent);
			component.setBelongingApplication(application);
			list.add(component);
			return seekrOrCreateClazzHelper(fullQName, splittedName, application, component,
					index + 1);
		} else {
			if (parent == null) {
				for (final Component component : application.getComponents()) {
					if (component.getFullQualifiedName().equals(DEFAULT_COMPONENT_NAME)) {
						parent = component;
						break;
					}
				}

				if (parent == null) {
					final Component component = new Component();
					component.setFullQualifiedName(DEFAULT_COMPONENT_NAME);
					component.setName(DEFAULT_COMPONENT_NAME);
					component.setParentComponent(null);
					component.setBelongingApplication(application);
					application.getComponents().add(component);
					parent = component;
				}
			}

			for (final Clazz clazz : parent.getClazzes()) {
				if (clazz.getName().equalsIgnoreCase(currentPart)) {
					return clazz;
				}
			}

			final Clazz clazz = new Clazz();
			clazz.setName(currentPart);
			clazz.setFullQualifiedName(fullQName);
			clazz.setParent(parent);
			parent.getClazzes().add(clazz);
			return clazz;
		}
	}

	public static String getMethodName(final String operationSignatureStr, final boolean constructor) {
		final Signature signature = SignatureParser.parse(operationSignatureStr, constructor);
		return signature.getOperationName();
	}
}
