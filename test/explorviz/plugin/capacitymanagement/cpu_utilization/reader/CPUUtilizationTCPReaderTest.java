package explorviz.plugin.capacitymanagement.cpu_utilization.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import explorviz.live_trace_processing.StringRegistry;
import explorviz.live_trace_processing.record.misc.StringRegistryRecord;
import explorviz.live_trace_processing.record.misc.SystemMonitoringRecord;
import explorviz.live_trace_processing.record.trace.HostApplicationMetaDataRecord;
import explorviz.plugin.capacitymanagement.TestConstants;
import explorviz.plugin.capacitymanagement.configuration.Configuration;
import explorviz.plugin.capacitymanagement.configuration.ScalingGroupsReader;
import explorviz.plugin.capacitymanagement.cpu_utilization.CPUUtilizationDistributor;
import explorviz.plugin.capacitymanagement.cpu_utilization.IAverageCPUUtilizationReceiver;
import explorviz.plugin.capacitymanagement.cpu_utilization.reader.CPUUtilizationTCPReader;
import explorviz.plugin.capacitymanagement.node.repository.Node;
import explorviz.plugin.capacitymanagement.node.repository.ScalingGroup;
import explorviz.plugin.capacitymanagement.node.repository.ScalingGroupRepository;

public class CPUUtilizationTCPReaderTest {

	protected Map<Node, Double> averageCPUUtil;

	@Test
	public void testTCPReader() throws Exception {
		final StringRegistry stringRegistry = new StringRegistry(new StringRegistrySenderDummy());

		final Configuration config = new Configuration(TestConstants.CONFIG_FILE);
		final ScalingGroupRepository scalingGroupRepository = new ScalingGroupRepository(config);
		ScalingGroupsReader.readInScalingGroups(scalingGroupRepository, TestConstants.CONFIG_FILE);
		final ScalingGroup scalingGroup = scalingGroupRepository.getScalingGroupByName("jpetstore");

		scalingGroup.addNode("0", "inst0", "host0");
		final CPUUtilizationDistributor distributor = new CPUUtilizationDistributor(180,
				scalingGroupRepository, new IAverageCPUUtilizationReceiver() {
					@Override
					public void newCPUUtilizationAverage(
							final Map<Node, Double> averageCPUUtilizations) {
						averageCPUUtil = averageCPUUtilizations;
					}
				});

		final CPUUtilizationTCPReader tcpReader = new CPUUtilizationTCPReader(10550, distributor);
		tcpReader.start();

		Thread.sleep(500);

		final URL url = new URL("http://127.0.0.1:10550");
		final SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(url.getHost(),
				url.getPort()));
		final ByteBuffer bufferForMetaData = ByteBuffer
				.allocateDirect(HostApplicationMetaDataRecord.BYTE_LENGTH_WITH_CLAZZ_ID);

		bufferForMetaData.put(HostApplicationMetaDataRecord.CLAZZ_ID);
		bufferForMetaData.putInt(0);
		bufferForMetaData.putInt(1);
		bufferForMetaData.putInt(2);
		bufferForMetaData.putInt(3);
		bufferForMetaData.flip();

		socketChannel.write(buildStringRegistryRecord("system0", 0));
		socketChannel.write(buildStringRegistryRecord("ip0", 1));
		socketChannel.write(buildStringRegistryRecord("host0", 2));
		socketChannel.write(buildStringRegistryRecord("app1", 3));
		socketChannel.write(bufferForMetaData);
		for (int i = 0; i < config.getCpuUtilizationHistoryLimit(); i++) {
			socketChannel.write(buildCPUUtil(stringRegistry));
		}

		Thread.sleep(1500);

		assertNotNull(averageCPUUtil);
		assertEquals(1, averageCPUUtil.size());
		for (final Entry<Node, Double> entry : averageCPUUtil.entrySet()) {
			assertEquals("host0", entry.getKey().getHostname());
			assertEquals(0.5, entry.getValue(), 0.01);
		}

		tcpReader.terminate(false);
	}

	private ByteBuffer buildCPUUtil(final StringRegistry stringRegistry) {
		final ByteBuffer buffer = ByteBuffer
				.allocateDirect(SystemMonitoringRecord.COMPRESSED_BYTE_LENGTH_WITH_CLAZZ_ID);
		buffer.put(SystemMonitoringRecord.CLAZZ_ID);
		buffer.putDouble(0.5);
		buffer.putLong(1000);
		buffer.putLong(2000);
		buffer.flip();
		return buffer;
	}

	private static ByteBuffer buildStringRegistryRecord(final String value, final int result) {
		final int regRecordLength = StringRegistryRecord.BYTE_LENGTH_WITHOUT_STRING_WITH_CLAZZ_ID
				+ value.length();
		final ByteBuffer buffer = ByteBuffer.allocateDirect(regRecordLength);

		buffer.put(StringRegistryRecord.CLAZZ_ID);
		buffer.putInt(result);
		buffer.putInt(value.length());
		buffer.put(value.getBytes());

		buffer.flip();
		return buffer;
	}
}
