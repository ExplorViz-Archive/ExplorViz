package explorviz.plugin_server.anomalydetection.util;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class takes a method and a bit of data and executes the method with each
 * bit of data concurrently with an arbitrary number of threads.
 *
 * @author Kim Mannstedt
 * @author Enno Schwanke
 * @author Christian Claus Wiechmann (base)
 *
 * @param <T>
 *            type of data we want to use (a thread is going to be started for
 *            each of these objects)
 * @param <L>
 *            type of data we want to additionally give to a method (is not
 *            going to be threaded)
 */
public class ADThreadPool<T, L> {

	private final IThreadable<T, L> method;
	private final int numThreads;
	private final Queue<T> data;

	private final L attr;

	private class ADThread<S> extends Thread {
		private final IThreadable<S, L> method;
		private final Queue<S> data;

		private final L attr;

		public ADThread(final IThreadable<S, L> method, final Queue<S> data, final L attr) {
			this.method = method;
			this.data = data;
			this.attr = attr;
		}

		@Override
		public void run() {
			S currentData = data.poll();
			while (currentData != null) {
				method.calculate(currentData, attr);
				currentData = data.poll();
			}
		}
	}

	/**
	 * Create a new ADThreadPool with a method and a number of threads to be
	 * used.
	 *
	 * @param method
	 *            calculation method
	 * @param numThreads
	 *            number of threads
	 * @param attr
	 *            additional parameter given to method (not threaded)
	 */
	public ADThreadPool(final IThreadable<T, L> method, final int numThreads, final L attr) {
		this.method = method;
		this.numThreads = numThreads;
		data = new LinkedBlockingQueue<>();
		this.attr = attr;
	}

	/**
	 * Add data to be processed after the ADThreadPool has been started.
	 *
	 * @param data
	 *            piece of data to be processed
	 */
	public void addData(final T data) {
		this.data.add(data);
	}

	/**
	 * Start the specified number of threads, each calculating a piece of data.
	 * If a thread is finished, it will take another piece of data to process.
	 *
	 * @throws InterruptedException
	 *             This only gets thrown if we somehow could not wait for the
	 *             threads to finish.
	 */
	public void startThreads() throws InterruptedException {
		final List<Thread> threads = new LinkedList<>();

		for (int i = 0; i < numThreads; i++) {
			threads.add(new ADThread<>(method, data, attr));
		}

		for (final Thread thread : threads) {
			thread.start();
		}

		for (final Thread thread : threads) {
			thread.join();
		}
	}

}
