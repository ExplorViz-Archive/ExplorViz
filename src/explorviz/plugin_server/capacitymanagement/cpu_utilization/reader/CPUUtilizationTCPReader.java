package explorviz.plugin_server.capacitymanagement.cpu_utilization.reader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import explorviz.live_trace_processing.filter.AbstractFilter;
import explorviz.plugin_server.capacitymanagement.cpu_utilization.CPUUtilizationDistributor;

/**
 * @author jgi, dtj Reads CPU utilization on given listeningPort. Calls
 *         CPUUtilizationonTCPReaderOneClient to read data
 */
public final class CPUUtilizationTCPReader extends Thread {
	private static final Logger LOG = LoggerFactory.getLogger(CPUUtilizationTCPReader.class);

	private static final int RINGBUFFER_LENGTH = 512;

	private final int listeningPort;
	private boolean active = true;

	private ServerSocketChannel serversocket;

	private final RingBuffer<RecordEvent> ringBuffer;

	private final List<CPUUtilizationTCPReaderOneClient> threads = new ArrayList<CPUUtilizationTCPReaderOneClient>();

	public CPUUtilizationTCPReader(final int listeningPort,
			final CPUUtilizationDistributor distributor) {
		this.listeningPort = listeningPort;

		final Disruptor<RecordEvent> disruptor = new Disruptor<RecordEvent>(
				RecordEvent.EVENT_FACTORY, RINGBUFFER_LENGTH, AbstractFilter.cachedThreadPool,
				ProducerType.MULTI, new BlockingWaitStrategy());

		@SuppressWarnings("unchecked")
		final EventHandler<RecordEvent>[] eventHandlers = new EventHandler[1];
		eventHandlers[0] = distributor;
		disruptor.handleEventsWith(eventHandlers);
		ringBuffer = disruptor.start();
	}

	@Override
	public void run() {
		try {
			open();
			while (active) {
				final CPUUtilizationTCPReaderOneClient thread = new CPUUtilizationTCPReaderOneClient(
						serversocket.accept(), ringBuffer);
				thread.start();
				threads.add(thread);
			}
		} catch (final IOException ex) {
			LOG.error("Error in read() " + ex.getMessage());
		} finally {
			try {
				serversocket.close();
			} catch (final IOException e) {
				LOG.error("Error in read() " + e.getMessage());
			}
		}
	}

	private final void open() throws IOException {
		serversocket = ServerSocketChannel.open();
		serversocket.socket().bind(new InetSocketAddress(listeningPort));
		LOG.info("listening on port " + listeningPort);
	}

	public final void terminate(final boolean error) {
		LOG.info("Shutdown of TCPReader requested.");
		active = false;
		for (final CPUUtilizationTCPReaderOneClient thread : threads) {
			thread.terminate();
		}
	}
}
