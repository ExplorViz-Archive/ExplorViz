package explorviz.plugin_server.rootcausedetection.util;


/**
 * This interface identifies a class as a method usable by {@link RCDThreadPool}
 * .
 *
 * @author Christian Claus Wiechmann
 *
 * @param <T>
 *            type that should be handed to the {@link IThreadable#calculate
 *            calculate(T)} method (threaded).
 * @param <T>
 *            type that should also be handed to the
 *            {@link IThreadable#calculate calculate(T)} method (not threaded).
 */
public interface IThreadable<T, L> {
	/**
	 * This method is used by {@link RCDThreadPool} to calculate multiple things
	 * concurrently. Please note that there is not built-in synchronization.
	 *
	 * @param input
	 *            input data for the method to work with.
	 * @param lscp
	 *            landscape we want to work with
	 */
	public void calculate(final T input, final L attr);
}
