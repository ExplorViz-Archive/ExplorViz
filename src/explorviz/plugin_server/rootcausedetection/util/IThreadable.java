package explorviz.plugin_server.rootcausedetection.util;

/**
 * This interface identifies a class as a method usable by {@link RCDThreadPool}
 * .
 *
 * @author Christian Claus Wiechmann
 *
 * @param <T>
 *            type that should be handed to the {@link IThreadable#calculate
 *            calculate(T)} method.
 */
public interface IThreadable<T> {
	/**
	 * This method is used by {@link RCDThreadPool} to calculate multiple things
	 * concurrently. Please note that there is not built-in synchronization.
	 *
	 * @param input
	 *            input data for the method to work with.
	 */
	public void calculate(final T input);
}
