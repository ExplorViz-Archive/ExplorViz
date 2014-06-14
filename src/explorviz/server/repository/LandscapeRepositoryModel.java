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
import explorviz.live_trace_processing.record.event.remote.ReceivedRemoteCallRecord;
import explorviz.live_trace_processing.record.event.remote.SentRemoteCallRecord;
import explorviz.live_trace_processing.record.misc.SystemMonitoringRecord;
import explorviz.live_trace_processing.record.trace.HostApplicationMetaDataRecord;
import explorviz.live_trace_processing.record.trace.Trace;
import explorviz.server.repository.helper.SignatureParser;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

public class LandscapeRepositoryModel implements IPeriodicTimeSignalReceiver {
	private static final String DEFAULT_COMPONENT_NAME = "(default)";

	public static final List<String> databaseNames = new ArrayList<String>();
	public static final int outputIntervalSeconds = 15;

	private final Landscape landscape;
	private final Kryo kryo;
	private final Map<SentRemoteCallRecord, Long> sentRemoteCallRecordCache = new HashMap<SentRemoteCallRecord, Long>();
	private final Map<ReceivedRemoteCallRecord, Long> receivedRemoteCallRecordCache = new HashMap<ReceivedRemoteCallRecord, Long>();

	private final Map<String, Node> nodeCache = new HashMap<String, Node>();
	private final Map<String, Application> applicationCache = new HashMap<String, Application>();

	private final Map<String, String> fullQNameCache = new HashMap<String, String>();

	private final Map<Application, Map<String, Clazz>> clazzCache = new HashMap<Application, Map<String, Clazz>>();

	static {
		databaseNames.add("hsqldb");
		databaseNames.add("postgres");
		databaseNames.add("db2");
		databaseNames.add("mysql");
	}

	public LandscapeRepositoryModel() {
		landscape = new Landscape();
		kryo = new Kryo();

		updateLandscapeAccess();

		new TimeSignalReader(TimeUnit.SECONDS.toMillis(outputIntervalSeconds), this).start();
	}

	private void updateLandscapeAccess() {
		landscape.setHash(java.lang.System.nanoTime());
	}

	public final Landscape getCurrentLandscape() {
		synchronized (landscape) {
			return kryo.copy(landscape);
		}
	}

	public final Landscape getLandscape(final long timestamp) throws FileNotFoundException {
		return RepositoryStorage.readFromFile(timestamp);
	}

	public final Map<Long, Long> getAvailableLandscapes() {
		return RepositoryStorage.getAvailableModels();
	}

	public void reset() {
		synchronized (landscape) {
			landscape.getApplicationCommunication().clear();
			landscape.getSystems().clear();
			landscape.setActivities(0L);
			updateLandscapeAccess();
		}
	}

	@Override
	public void periodicTimeSignal(final long timestamp) {
		synchronized (landscape) {
			RepositoryStorage.writeToFile(landscape, java.lang.System.currentTimeMillis());

			updateRemoteCalls();

			resetCommunication();
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
		landscape.setActivities(0L);

		for (final Application application : applicationCache.values()) {
			final Map<String, Clazz> clazzes = clazzCache.get(application);
			for (final Clazz clazz : clazzes.values()) {
				clazz.getObjectIds().clear();
				clazz.setInstanceCount(0);
			}

			for (final explorviz.shared.model.CommunicationClazz commu : application
					.getCommuncations()) {
				commu.setRequestsPerSecond(0);
			}
		}

		for (final Communication commu : landscape.getApplicationCommunication()) {
			commu.setRequestsPerSecond(0);
		}

		updateLandscapeAccess();
	}

	public void insertIntoModel(final IRecord inputIRecord) {
		if (inputIRecord instanceof Trace) {
			final Trace trace = (Trace) inputIRecord;

			final HostApplicationMetaDataRecord hostApplicationRecord = trace.getTraceEvents()
					.get(0).getHostApplicationMetadata();

			synchronized (landscape) {
				final Node node = seekOrCreateNode(hostApplicationRecord);
				final Application application = seekOrCreateApplication(node,
						hostApplicationRecord.getApplication());

				createCommunicationInApplication(trace.getTraceEvents(),
						hostApplicationRecord.getHostname(), application);

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

			final NodeGroup nodeGroup = new NodeGroup(); // TODO match
															// nodegroups
			nodeGroup.setName(hostApplicationRecord.getHostname()); // TODO
			nodeGroup.getNodes().add(node);

			if (landscape.getSystems().isEmpty()) {
				final System system = new System(); // TODO
				system.setName(hostApplicationRecord.getSystemname());
				system.getNodeGroups().add(nodeGroup);
				landscape.getSystems().add(system);
			} else {
				landscape.getSystems().get(0).getNodeGroups().add(nodeGroup);
			}
		}

		return node;
	}

	private Application seekOrCreateApplication(final Node node, final String applicationName) {
		Application application = applicationCache.get(applicationName);

		if (application == null) {
			application = new Application();

			application.setDatabase(isApplicationDatabase(applicationName));
			application.setId((node.getName() + "_" + applicationName).hashCode());
			application.setLastUsage(java.lang.System.currentTimeMillis());
			application.setName(applicationName);

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
			// communication.setRequestsPerSecond(30);
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
			// communication2.setRequestsPerSecond(30);
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
		for (final String databaseName : databaseNames) {
			if (applicationName.toLowerCase().contains(databaseName)) {
				isDatabase = true;
				break;
			}
		}
		return isDatabase;
	}

	private void createCommunicationInApplication(final List<AbstractEventRecord> events,
			final String currentHostname, final Application currentApplication) {
		Clazz callerClazz = null;
		final Stack<Clazz> callerClazzesHistory = new Stack<Clazz>();

		for (final AbstractEventRecord event : events) {
			if (event instanceof AbstractBeforeEventRecord) {
				final AbstractBeforeEventRecord abstractBeforeEventRecord = (AbstractBeforeEventRecord) event;

				final String fullQName = getClazzFullQName(abstractBeforeEventRecord
						.getOperationSignature());

				final Clazz currentClazz = seekOrCreateClazz(fullQName, currentApplication,
						abstractBeforeEventRecord.getRuntimeStatisticInformation().getObjectIds());

				if (callerClazz != null) {
					createOrUpdateCall(callerClazz, currentClazz, currentApplication,
							abstractBeforeEventRecord.getRuntimeStatisticInformation().getCount(),
							(float) abstractBeforeEventRecord.getRuntimeStatisticInformation()
									.getAverage());
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
			}
			// TODO unknown recv remote classes
		}
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

		for (final Communication commu : landscape.getApplicationCommunication()) {
			if (((commu.getSource() == callerApplication) && (commu.getTarget() == currentApplication))
					|| ((commu.getSource() == currentApplication) && (commu.getTarget() == callerApplication))) {
				commu.setRequestsPerSecond(commu.getRequestsPerSecond() + 1);
				return;
			}
		}

		final Communication communication = new Communication();
		communication.setSource(callerApplication);
		communication.setTarget(currentApplication);
		communication.setRequestsPerSecond(1);
		landscape.getApplicationCommunication().add(communication);
	}

	private void createOrUpdateCall(final Clazz caller, final Clazz callee,
			final Application application, final int count, final float average) {
		if (callee == caller) {
			// TODO system activity is wrong if we exclude those
			return; // dont create self edges
		}

		for (final CommunicationClazz commu : application.getCommuncations()) {
			if (((commu.getSource() == caller) && (commu.getTarget() == callee))
					|| ((commu.getSource() == callee) && (commu.getTarget() == caller))) {
				landscape.setActivities(landscape.getActivities() + count);

				commu.setRequestsPerSecond(commu.getRequestsPerSecond() + count);
				commu.setAverageResponseTime(average); // TODO add?
				// TODO if edge back is also in this, the response time is wrong
				return;
			}
		}

		final CommunicationClazz commu = new CommunicationClazz();

		commu.setSource(caller);
		commu.setTarget(callee);

		landscape.setActivities(landscape.getActivities() + count);
		commu.setRequestsPerSecond(count);
		commu.setAverageResponseTime(average);

		application.getCommuncations().add(commu);
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
			parent.getClazzes().add(clazz);
			return clazz;
		}
	}

	private String getClazzFullQName(final String operationSignatureStr) {
		String fullQName = fullQNameCache.get(operationSignatureStr);

		if (fullQName == null) {
			fullQName = SignatureParser.parse(operationSignatureStr, false).getFullQualifiedName();

			if (fullQName.indexOf("$") > 0) {
				fullQName = fullQName.substring(0, fullQName.indexOf("$"));
			}

			if (fullQName.equals("")) {
				fullQName = operationSignatureStr;
			}

			fullQNameCache.put(operationSignatureStr, fullQName);
		}

		return fullQName;
	}
}
