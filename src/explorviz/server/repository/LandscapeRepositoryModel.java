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
import explorviz.live_trace_processing.record.trace.HostApplicationMetaDataRecord;
import explorviz.live_trace_processing.record.trace.Trace;
import explorviz.server.repository.helper.SignatureParser;
import explorviz.shared.model.*;

public class LandscapeRepositoryModel implements IPeriodicTimeSignalReceiver {
	private static final String DEFAULT_COMPONENT_NAME = "(default)";
	private final Landscape landscape;
	private final Kryo kryo;
	private final Map<SentRemoteCallRecord, Long> sentRemoteCallRecordCache = new HashMap<SentRemoteCallRecord, Long>();
	private final Map<ReceivedRemoteCallRecord, Long> receivedRemoteCallRecordCache = new HashMap<ReceivedRemoteCallRecord, Long>();

	public LandscapeRepositoryModel() {
		landscape = new Landscape();
		kryo = new Kryo();

		updateLandscapeAccess();

		new TimeSignalReader(10 * 1000, this).start();
	}

	private void updateLandscapeAccess() {
		landscape.setHash(System.nanoTime());
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
			landscape.getNodeGroups().clear();
			landscape.setActivities(0L);
			updateLandscapeAccess();
		}
	}

	@Override
	public void periodicTimeSignal(final long timestamp) {
		synchronized (landscape) {
			RepositoryStorage.writeToFile(landscape, System.currentTimeMillis());

			updateRemoteCalls();

			resetCommunication();
		}

		RepositoryStorage.cleanUpTooOldFiles(System.currentTimeMillis());
	}

	private void updateRemoteCalls() {
		final long currentTime = System.nanoTime();

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

		for (final NodeGroup nodeGroup : landscape.getNodeGroups()) {
			for (final Node node : nodeGroup.getNodes()) {
				for (final Application application : node.getApplications()) {
					for (final explorviz.shared.model.CommunicationClazz commu : application
							.getCommuncations()) {
						commu.setRequestsPerSecond(0);
					}
				}
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

			// TODO build multi threaded? and with caches
			final HostApplicationMetaDataRecord hostApplicationRecord = trace.getTraceEvents()
					.get(0).getHostApplicationMetadata();

			synchronized (landscape) {
				final Node node = seekOrCreateNode(hostApplicationRecord.getHostname());
				final Application application = seekOrCreateApplication(node,
						hostApplicationRecord.getApplication());

				createCommunicationInApplication(trace.getTraceEvents(),
						hostApplicationRecord.getHostname(), application);

				updateLandscapeAccess();
			}
		}
	}

	private Node seekOrCreateNode(final String hostname) {
		for (final NodeGroup nodeGroup : landscape.getNodeGroups()) {
			for (final Node node : nodeGroup.getNodes()) {
				if (node.getName().equalsIgnoreCase(hostname)) {
					return node;
				}
			}
		}

		final Node node = new Node();
		node.setIpAddress(hostname); // TODO
		node.setName(hostname);

		final NodeGroup nodeGroup = new NodeGroup(); // TODO match nodegroups
		nodeGroup.getNodes().add(node);

		landscape.getNodeGroups().add(nodeGroup);

		return node;
	}

	private Application seekOrCreateApplication(final Node node, final String applicationName) {
		for (final Application application : node.getApplications()) {
			if (application.getName().equalsIgnoreCase(applicationName)) {
				return application;
			}
		}

		final Application application = new Application();
		application.setDatabase(false);
		application.setId((node.getName() + applicationName).hashCode());
		application.setLastUsage(System.nanoTime());
		application.setName(applicationName);

		node.getApplications().add(application);
		return application;
	}

	private void createCommunicationInApplication(final List<AbstractEventRecord> events,
			final String currentHostname, final Application currentApplication) {
		Clazz callerClazz = null;
		final Stack<Clazz> callerClazzesHistory = new Stack<Clazz>();

		for (final AbstractEventRecord event : events) {
			if (event instanceof AbstractBeforeEventRecord) {
				final AbstractBeforeEventRecord abstractBeforeEventRecord = (AbstractBeforeEventRecord) event;

				String fullQName = getClazzFullQName(abstractBeforeEventRecord
						.getOperationSignature());
				if (fullQName.equals("")) {
					fullQName = abstractBeforeEventRecord.getOperationSignature();
				}
				final Clazz currentClazz = seekOrCreateClazz(fullQName, currentApplication);

				if (callerClazz != null) {
					createOrUpdateCall(callerClazz, currentClazz, currentApplication,
							abstractBeforeEventRecord.getRuntimeStatisticInformation().getCount(),
							abstractBeforeEventRecord.getRuntimeStatisticInformation().getAverage());
				}

				callerClazz = currentClazz;
				callerClazzesHistory.push(currentClazz);
			} else if ((event instanceof AbstractAfterEventRecord)
					|| (event instanceof AbstractAfterFailedEventRecord)) {
				if (!callerClazzesHistory.isEmpty()) {
					callerClazz = callerClazzesHistory.pop();
				}
			} else if (event instanceof SentRemoteCallRecord) {
				final SentRemoteCallRecord sentRemoteCallRecord = (SentRemoteCallRecord) event;

				final ReceivedRemoteCallRecord receivedRecord = seekMatchingReceivedRemoteRecord(sentRemoteCallRecord);

				if (receivedRecord == null) {
					sentRemoteCallRecordCache.put(sentRemoteCallRecord, System.nanoTime());
				} else {
					seekOrCreateCommunication(sentRemoteCallRecord, receivedRecord);
				}
			} else if (event instanceof ReceivedRemoteCallRecord) {
				final ReceivedRemoteCallRecord receivedRemoteCallRecord = (ReceivedRemoteCallRecord) event;

				final SentRemoteCallRecord sentRecord = seekSentRemoteTraceIDandOrderID(receivedRemoteCallRecord);

				if (sentRecord == null) {
					receivedRemoteCallRecordCache.put(receivedRemoteCallRecord, System.nanoTime());
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
			if ((receivedRemoteRecord.getTraceId() == sentRecord.getTraceId())
					&& (receivedRemoteRecord.getOrderIndex() == sentRecord.getOrderIndex())) {
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
		final Node callerHost = seekOrCreateNode(sentRemoteCallRecord.getHostApplicationMetadata()
				.getHostname());
		final Application callerApplication = seekOrCreateApplication(callerHost,
				sentRemoteCallRecord.getHostApplicationMetadata().getApplication());

		final Node currentHost = seekOrCreateNode(receivedRemoteCallRecord
				.getHostApplicationMetadata().getHostname());
		final Application currentApplication = seekOrCreateApplication(currentHost,
				receivedRemoteCallRecord.getHostApplicationMetadata().getApplication());

		System.out.println("Creating or updating Communication: " + callerHost.getName());
		System.out.println("\n");
		System.out.println("callerHost: " + callerHost.getName());
		System.out.println("callerApplication: " + callerApplication.getName());
		System.out.println("callerTraceId: " + receivedRemoteCallRecord.getCallerTraceId());
		System.out.println("callerOrderIndex: " + receivedRemoteCallRecord.getCallerOrderIndex());
		System.out.println("calleeHost: " + callerHost.getName());
		System.out.println("calleeApplication: " + currentApplication.getName());

		for (final Communication commu : landscape.getApplicationCommunication()) {
			if ((commu.getSource() == callerApplication)
					&& (commu.getTarget() == currentApplication)) {
				commu.setRequestsPerSecond(commu.getRequestsPerSecond() + 1);
				System.out.println("Request count: " + commu.getRequestsPerSecond() + "\n");
				return;
			}
		}

		final Communication communication = new Communication();
		communication.setSource(callerApplication);
		communication.setTarget(currentApplication);
		communication.setRequestsPerSecond(1);
		System.out.println("Request count: " + communication.getRequestsPerSecond() + "\n");
		landscape.getApplicationCommunication().add(communication);
	}

	private void createOrUpdateCall(final Clazz caller, final Clazz callee,
			final Application application, final int count, final double average) {
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

	private Clazz seekOrCreateClazz(final String fullQName, final Application application) {
		final String[] splittedName = fullQName.split("\\.");
		return seekrOrCreateClazzHelper(fullQName, splittedName, application, null, 0);
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
			clazz.setInstanceCount(20); // TODO 1
			clazz.setName(currentPart);
			clazz.setFullQualifiedName(fullQName);
			parent.getClazzes().add(clazz);
			return clazz;
		}
	}

	private String getClazzFullQName(final String operationSignatureStr) {
		final String fullQName = SignatureParser.parse(operationSignatureStr, false)
				.getFullQualifiedName();

		if (fullQName.indexOf("$") > 0) {
			return fullQName.substring(0, fullQName.indexOf("$"));
		} else {
			return fullQName;
		}
	}
}
