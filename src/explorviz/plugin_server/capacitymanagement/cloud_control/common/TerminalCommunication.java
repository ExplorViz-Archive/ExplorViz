package explorviz.plugin_server.capacitymanagement.cloud_control.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jgi, dj Executes commands on server terminal
 */
public class TerminalCommunication {

	private static final Logger LOG = LoggerFactory.getLogger(TerminalCommunication.class);

	/**
	 * @param command
	 *            command to execute
	 * @return output
	 * @throws Exception
	 *             io/connection error
	 */
	public static List<String> executeCommand(final String command) throws Exception {
		final List<String> output = new ArrayList<String>();

		BufferedReader in = null;
		BufferedReader err = null;
		try {
			final Process process = Runtime.getRuntime().exec(command);
			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			final int exitCode = process.waitFor();
			if (exitCode != 0) {
				String error = "Command didn't exit cleanly! Command exited with code: " + exitCode
						+ "\n Command was: " + command + "\n";
				while (err.ready()) {
					error += err.readLine() + "\n";
				}
				throw new Exception(error);
			}
			while (in.ready()) {
				output.add(in.readLine());
			}
		} finally {
			closeOpenIO(in, err);
		}
		return output;
	}

	public static List<String> executeNovaCommand(final String command) throws Exception {

		return executeCommand("nova " + getAuthentificationData() + " " + command);
	}

	private static String getAuthentificationData() {
		return "";
	}

	private static void closeOpenIO(final BufferedReader in, final BufferedReader err) {
		try {
			if ((in != null) && in.ready()) {
				in.close();
			}
			if ((err != null) && err.ready()) {
				err.close();
			}
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
