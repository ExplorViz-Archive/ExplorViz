package explorviz.plugin_server.capacitymanagement.cpu_utilization.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;

import explorviz.live_trace_processing.record.IRecord;
import explorviz.live_trace_processing.record.misc.StringRegistryRecord;
import explorviz.live_trace_processing.record.misc.SystemMonitoringRecord;
import explorviz.live_trace_processing.record.trace.HostApplicationMetaDataRecord;

/**
 * @author jgi, dtj read CPU ultilization
 */
public class CPUUtilizationTCPReaderOneClient extends Thread {
	private static final Logger LOG = LoggerFactory
			.getLogger(CPUUtilizationTCPReaderOneClient.class);

	private HostApplicationMetaDataRecord hostApplicationMetadata;

	private final Map<Integer, String> stringRegistry = new TreeMap<Integer, String>();
	private final List<byte[]> waitingForStringMessages = new ArrayList<byte[]>(1024);

	private final SocketChannel socketChannel;
	private final RingBuffer<RecordEvent> ringBuffer;

	public CPUUtilizationTCPReaderOneClient(final SocketChannel socketChannel,
			final RingBuffer<RecordEvent> ringBuffer) {
		this.socketChannel = socketChannel;
		this.ringBuffer = ringBuffer;
	}

	@Override
	public void run() {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
		try {
			while (socketChannel.read(buffer) != -1) {
				buffer.flip();
				messagesfromByteArray(buffer);
			}
		} catch (final IOException ex) {
			LOG.error("Error in read() " + ex.getMessage());
		}
	}

	private final void messagesfromByteArray(final ByteBuffer buffer) {
		while (buffer.remaining() > 0) {
			final byte clazzId = buffer.get();
			switch (clazzId) {
				case HostApplicationMetaDataRecord.CLAZZ_ID: {
					if (buffer.remaining() >= (HostApplicationMetaDataRecord.BYTE_LENGTH_WITH_CLAZZ_ID - 1)) {
						readInTraceMetadata(buffer);
					} else {
						buffer.position(buffer.position() - 1);
						buffer.compact();
						return;
					}
					break;
				}
				case StringRegistryRecord.CLAZZ_ID: {
					int mapId = 0;
					int stringLength = 0;
					if (buffer.remaining() >= 8) {
						mapId = buffer.getInt();
						stringLength = buffer.getInt();
					} else {
						buffer.position(buffer.position() - 1);
						buffer.compact();
						return;
					}

					if (buffer.remaining() >= stringLength) {
						final byte[] stringByteArray = new byte[stringLength];

						buffer.get(stringByteArray);

						addToRegistry(mapId, new String(stringByteArray));
					} else {
						buffer.position(buffer.position() - 9);
						buffer.compact();
						return;
					}
					break;
				}
				case SystemMonitoringRecord.CLAZZ_ID: {
					if (buffer.remaining() >= (SystemMonitoringRecord.COMPRESSED_BYTE_LENGTH_WITH_CLAZZ_ID - 1)) {
						readInSystemMonitoringRecord(buffer);
					} else {
						buffer.position(buffer.position() - 1);
						buffer.compact();
						return;
					}
					break;
				}
				default: {
					LOG.error("unknown class id " + clazzId + " at offset "
							+ (buffer.position() - 4));
					buffer.clear();
					return;
				}
			}
		}

		buffer.clear();
	}

	private final void readInTraceMetadata(final ByteBuffer buffer) {
		final int systemId = buffer.getInt();
		final int ipId = buffer.getInt();
		final int hostnameId = buffer.getInt();
		final int applicationId = buffer.getInt();

		final String system = getStringFromRegistry(systemId);
		final String ip = getStringFromRegistry(ipId);
		final String hostname = getStringFromRegistry(hostnameId);
		final String application = getStringFromRegistry(applicationId);

		if ((system != null) && (ip != null) && (hostname != null) && (application != null)) {
			hostApplicationMetadata = new HostApplicationMetaDataRecord(system, ip, hostname,
					application);
		} else {
			final byte[] message = new byte[HostApplicationMetaDataRecord.BYTE_LENGTH_WITH_CLAZZ_ID];
			buffer.position(buffer.position()
					- HostApplicationMetaDataRecord.BYTE_LENGTH_WITH_CLAZZ_ID);
			buffer.get(message);
			putInWaitingMessages(message);
		}
	}

	private final void readInSystemMonitoringRecord(final ByteBuffer buffer) {
		final double cpuUtil = buffer.getDouble();
		final long usedRAM = buffer.getLong();
		final long absoluteRAM = buffer.getLong();

		putInRingBuffer(new SystemMonitoringRecord(cpuUtil, usedRAM, absoluteRAM,
				hostApplicationMetadata));
	}

	private final void putInWaitingMessages(final byte[] message) {
		waitingForStringMessages.add(message);
	}

	private final void checkWaitingMessages() {
		final List<byte[]> localWaitingList = new ArrayList<byte[]>();
		for (final byte[] waitingMessage : waitingForStringMessages) {
			localWaitingList.add(waitingMessage);
		}
		waitingForStringMessages.clear();

		for (final byte[] waitingMessage : localWaitingList) {
			final ByteBuffer buffer = ByteBuffer.wrap(waitingMessage);
			final byte waitingMessageClazzId = buffer.get();
			switch (waitingMessageClazzId) {
				case HostApplicationMetaDataRecord.CLAZZ_ID:
					readInTraceMetadata(buffer);
					break;
				default:
					break;
			}
		}
	}

	private final void putInRingBuffer(final IRecord message) {
		final long hiseq = ringBuffer.next();
		final RecordEvent valueEvent = ringBuffer.get(hiseq);
		valueEvent.setValue(message);
		valueEvent.setMetadata(hostApplicationMetadata);
		ringBuffer.publish(hiseq);
	}

	private final void addToRegistry(final int key, final String value) {
		stringRegistry.put(key, value);

		checkWaitingMessages();
	}

	private final String getStringFromRegistry(final int id) {
		return stringRegistry.get(id);
	}

	public void terminate() {
	}
}
