package explorviz.plugin.capacitymanagement.cpu_utilization;

import java.util.Map;

import explorviz.shared.model.Node;

@SuppressWarnings("unused")
public class CPUUtilizationDistributorTest {

	private static final double DELTA = 0.01;

	private Map<Node, Double> averageCPUUtil;

	// @Test
	// public void testOnEvent() throws Exception {
	// final CapManConfiguration config = new
	// CapManConfiguration(TestConstants.CONFIG_FILE);
	// final ScalingGroupRepository scalingGroupRepository = new
	// ScalingGroupRepository(config);
	// ScalingGroupsReader.readInScalingGroups(scalingGroupRepository,
	// TestConstants.CONFIG_FILE);
	// final ScalingGroup scalingGroup =
	// scalingGroupRepository.getScalingGroupByName("jpetstore");
	//
	// scalingGroup.addNode("0", "inst0", "host0");
	// final CPUUtilizationDistributor distributor = new
	// CPUUtilizationDistributor(50,
	// scalingGroupRepository, new IAverageCPUUtilizationReceiver() {
	// @Override
	// public void newCPUUtilizationAverage(
	// final Map<Node, Double> averageCPUUtilizations) {
	// averageCPUUtil = averageCPUUtilizations;
	// }
	// });
	//
	// final RecordEvent recordEvent = RecordEvent.EVENT_FACTORY.newInstance();
	// recordEvent.setValue(new SystemMonitoringRecord(0.5, 1000, 2000, null));
	// recordEvent.setMetadata(new HostApplicationMetaDataRecord("testSystem",
	// "testIp", "host0",
	// "app0"));
	// for (int i = 0; i <= config.getCpuUtilizationHistoryLimit(); i++) {
	// distributor.onEvent(recordEvent, i, false);
	// }
	//
	// Thread.sleep(100);
	// assertEquals(1, averageCPUUtil.size());
	// for (final Entry<Node, Double> entry : averageCPUUtil.entrySet()) {
	// // TODO: assertEquals("host0", entry.getKey().getHostname());
	// assertEquals(0.5, entry.getValue(), DELTA);
	// }
	// }
}
