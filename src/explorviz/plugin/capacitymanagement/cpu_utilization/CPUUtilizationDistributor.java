package explorviz.plugin.capacitymanagement.cpu_utilization;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

import explorviz.live_trace_processing.record.IRecord;
import explorviz.live_trace_processing.record.misc.SystemMonitoringRecord;
import explorviz.plugin.capacitymanagement.cpu_utilization.reader.RecordEvent;
import explorviz.plugin.capacitymanagement.node.repository.*;

public class CPUUtilizationDistributor implements EventHandler<RecordEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(CPUUtilizationDistributor.class);

	private final ScalingGroupRepository scalingGroupRepository;
	private final IAverageCPUUtilizationReceiver averageCPUUtilizationReceiver;

	private final int timeWindowForAverageInMillis;

	private final AverageThread averageThread;

	/**
	 * @param timeWindowForAverageInMillis
	 *            amount of time for calculated average
	 * @param scalingGroupRepository
	 *            the repository of scaling groups
	 * @param averageCPUUtilizationReceiver
	 *            average cpu utilization
	 */
	public CPUUtilizationDistributor(final int timeWindowForAverageInMillis,
			final ScalingGroupRepository scalingGroupRepository,
			final IAverageCPUUtilizationReceiver averageCPUUtilizationReceiver) {
		this.timeWindowForAverageInMillis = timeWindowForAverageInMillis;
		this.scalingGroupRepository = scalingGroupRepository;
		this.averageCPUUtilizationReceiver = averageCPUUtilizationReceiver;

		averageThread = new AverageThread();
		averageThread.start();
	}

	// if event is triggered, updates cpu utilization
	@Override
	public void onEvent(final RecordEvent event, final long seqId, final boolean endOfBatch)
			throws Exception {
		final IRecord record = event.getValue();
		if (record instanceof SystemMonitoringRecord) {
			final SystemMonitoringRecord systemMon = (SystemMonitoringRecord) record;
			final String hostname = event.getMetadata().getHostname();

			final Node node = scalingGroupRepository.getNodeByHostname(hostname);
			if (node == null) {
				LOG.info("Node " + hostname + " is unknown. Dropping CPU utilization.");
				return;
			}

			if ((0.0 <= systemMon.getCpuUtilization()) && (systemMon.getCpuUtilization() <= 1.0)) {
				// if (!node.isEnabled()) {
				// LOG.info(TimeProvider.getCurrentTimestamp() + ": Node " +
				// hostname
				// + " received cpu util: " + systemMon.getCpuUtilization());
				// }
				node.addCPUUtilizationHistoryEntry(systemMon.getCpuUtilization());
			}
		}
	}

	/**
	 * terminates thread
	 */
	public void terminate() {
		averageThread.interrupt();
	}

	/**
	 * @author jgi, dtj updates the average cpu utilization of enabled nodes in
	 *         a given time window
	 */
	private class AverageThread extends Thread {
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(timeWindowForAverageInMillis);
				} catch (final InterruptedException e) {
				}

				for (int i = 0; i < scalingGroupRepository.getScalingGroupsCount(); i++) {
					final ScalingGroup scalingGroup = scalingGroupRepository.getScalingGroup(i);

					if (scalingGroup.isEnabled()) {
						final Map<Node, Double> averageCPUUtilizations = new HashMap<Node, Double>();
						for (int j = 0; j < scalingGroup.getNodesCount(); j++) {
							final Node node = scalingGroup.getNode(j);

							if (node.isEnabled() && node.hasSufficientCPUUilizationHistoryEntries()) {
								averageCPUUtilizations.put(node, node.getAverageCPUUtilization());
							}
						}

						if (!averageCPUUtilizations.isEmpty()) {
							averageCPUUtilizationReceiver
									.newCPUUtilizationAverage(averageCPUUtilizations);
						}
					}
				}
			}
		}
	}
}
