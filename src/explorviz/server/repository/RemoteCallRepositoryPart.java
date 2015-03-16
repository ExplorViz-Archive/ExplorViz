package explorviz.server.repository;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import explorviz.live_trace_processing.record.event.AbstractEventRecord;
import explorviz.live_trace_processing.record.event.remote.BeforeReceivedRemoteCallRecord;
import explorviz.live_trace_processing.record.event.remote.BeforeSentRemoteCallRecord;
import explorviz.live_trace_processing.record.trace.HostApplicationMetaDataRecord;
import explorviz.server.repository.helper.RemoteRecordBuffer;
import explorviz.shared.model.*;

public class RemoteCallRepositoryPart {
	private final Map<BeforeSentRemoteCallRecord, RemoteRecordBuffer> sentRemoteCallRecordCache = new HashMap<BeforeSentRemoteCallRecord, RemoteRecordBuffer>();
	private final Map<BeforeReceivedRemoteCallRecord, RemoteRecordBuffer> receivedRemoteCallRecordCache = new HashMap<BeforeReceivedRemoteCallRecord, RemoteRecordBuffer>();

	protected void checkForTimedoutRemoteCalls() {
		final long currentTime = java.lang.System.nanoTime();

		final Iterator<Entry<BeforeReceivedRemoteCallRecord, RemoteRecordBuffer>> receivedIterator = receivedRemoteCallRecordCache
				.entrySet().iterator();
		while (receivedIterator.hasNext()) {
			final Entry<BeforeReceivedRemoteCallRecord, RemoteRecordBuffer> entry = receivedIterator
					.next();

			if ((currentTime - TimeUnit.SECONDS.toNanos(30)) > entry.getValue()
					.getTimestampPutIntoBuffer()) {
				receivedIterator.remove();
			}
		}

		final Iterator<Entry<BeforeSentRemoteCallRecord, RemoteRecordBuffer>> sentIterator = sentRemoteCallRecordCache
				.entrySet().iterator();

		while (sentIterator.hasNext()) {
			final Entry<BeforeSentRemoteCallRecord, RemoteRecordBuffer> entry = sentIterator.next();

			if ((currentTime - TimeUnit.SECONDS.toNanos(30)) > entry.getValue()
					.getTimestampPutIntoBuffer()) {
				sentIterator.remove();
			}
		}
	}

	public void insertSentRecord(final Clazz callerClazz,
			final BeforeSentRemoteCallRecord sentRemoteCallRecord, final Landscape landscape,
			final InsertionRepositoryPart inserter) {
		final BeforeReceivedRemoteCallRecord receivedRecord = seekMatchingReceivedRemoteRecord(sentRemoteCallRecord);

		if (receivedRecord == null) {
			final RemoteRecordBuffer remoteRecordBuffer = new RemoteRecordBuffer();
			remoteRecordBuffer.setBelongingClazz(callerClazz);

			sentRemoteCallRecordCache.put(sentRemoteCallRecord, remoteRecordBuffer);
		} else {
			seekOrCreateCommunication(sentRemoteCallRecord, receivedRecord, callerClazz,
					receivedRemoteCallRecordCache.get(receivedRecord).getBelongingClazz(),
					landscape, inserter);

			receivedRemoteCallRecordCache.remove(receivedRecord);
		}
	}

	public void insertReceivedRecord(final BeforeReceivedRemoteCallRecord receivedRemoteCallRecord,
			final Clazz firstReceiverClazz, final Landscape landscape,
			final InsertionRepositoryPart inserter) {
		final BeforeSentRemoteCallRecord sentRecord = seekSentRemoteTraceIDandOrderID(receivedRemoteCallRecord);

		if (sentRecord == null) {
			final RemoteRecordBuffer remoteRecordBuffer = new RemoteRecordBuffer();
			remoteRecordBuffer.setBelongingClazz(firstReceiverClazz);

			receivedRemoteCallRecordCache.put(receivedRemoteCallRecord, remoteRecordBuffer);
		} else {
			seekOrCreateCommunication(sentRecord, receivedRemoteCallRecord,
					sentRemoteCallRecordCache.get(sentRecord).getBelongingClazz(),
					firstReceiverClazz, landscape, inserter);

			sentRemoteCallRecordCache.remove(sentRecord);
		}
	}

	private BeforeReceivedRemoteCallRecord seekMatchingReceivedRemoteRecord(
			final BeforeSentRemoteCallRecord sentRecord) {
		for (final BeforeReceivedRemoteCallRecord receivedRemoteRecord : receivedRemoteCallRecordCache
				.keySet()) {
			if ((receivedRemoteRecord.getCallerTraceId() == sentRecord.getTraceId())
					&& (receivedRemoteRecord.getCallerOrderIndex() == sentRecord.getOrderIndex())) {
				return receivedRemoteRecord;
			}
		}

		return null;
	}

	private BeforeSentRemoteCallRecord seekSentRemoteTraceIDandOrderID(
			final BeforeReceivedRemoteCallRecord remoteRecord) {
		for (final BeforeSentRemoteCallRecord sentRemoteRecord : sentRemoteCallRecordCache.keySet()) {
			if ((sentRemoteRecord.getTraceId() == remoteRecord.getCallerTraceId())
					&& (sentRemoteRecord.getOrderIndex() == remoteRecord.getCallerOrderIndex())) {
				return sentRemoteRecord;
			}
		}

		return null;
	}

	private void seekOrCreateCommunication(final BeforeSentRemoteCallRecord sentRemoteCallRecord,
			final BeforeReceivedRemoteCallRecord receivedRemoteCallRecord,
			final Clazz sentRemoteClazz, final Clazz receivedRemoteClazz,
			final Landscape landscape, final InsertionRepositoryPart inserter) {

		final Application callerApplication = getHostApplication(sentRemoteCallRecord, inserter,
				landscape);
		final Application currentApplication = getHostApplication(receivedRemoteCallRecord,
				inserter, landscape);

		for (final Communication commu : landscape.getApplicationCommunication()) {
			if (((commu.getSource() == callerApplication) && (commu.getTarget() == currentApplication))
					|| ((commu.getSource() == currentApplication) && (commu.getTarget() == callerApplication))) {
				commu.setRequests(commu.getRequests()
						+ sentRemoteCallRecord.getRuntimeStatisticInformation().getCount());

				final float oldAverage = commu.getAverageResponseTimeInNanoSec();

				commu.setAverageResponseTimeInNanoSec((float) (oldAverage + sentRemoteCallRecord
						.getRuntimeStatisticInformation().getAverage()) / 2f);

				landscape.setActivities(landscape.getActivities()
						+ sentRemoteCallRecord.getRuntimeStatisticInformation().getCount());
				return;
			}
		}

		final Communication communication = new Communication();
		communication.setSource(callerApplication);
		communication.setSourceClazz(sentRemoteClazz);

		communication.setTarget(currentApplication);
		communication.setTargetClazz(receivedRemoteClazz);

		communication.setRequests(sentRemoteCallRecord.getRuntimeStatisticInformation().getCount());
		communication.setAverageResponseTimeInNanoSec((float) sentRemoteCallRecord
				.getRuntimeStatisticInformation().getAverage());
		communication.setTechnology(sentRemoteCallRecord.getTechnology());
		landscape.getApplicationCommunication().add(communication);

		landscape.setActivities(landscape.getActivities()
				+ sentRemoteCallRecord.getRuntimeStatisticInformation().getCount());
	}

	public Application getHostApplication(final AbstractEventRecord record,
			final InsertionRepositoryPart inserter, final Landscape landscape) {
		final HostApplicationMetaDataRecord hostMeta = record.getHostApplicationMetadataList()
				.iterator().next();
		final Node host = inserter.seekOrCreateNode(hostMeta, landscape);
		final Application hostApplication = inserter.seekOrCreateApplication(host, hostMeta,
				landscape);
		return hostApplication;
	}
}
