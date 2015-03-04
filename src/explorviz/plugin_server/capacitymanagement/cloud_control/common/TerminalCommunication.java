package explorviz.plugin_server.capacitymanagement.cloud_control.common;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration;
import explorviz.plugin_server.capacitymanagement.configuration.InvalidConfigurationException;

/**
 * Executes commands on server terminal. <br>
 * Mainly taken from capacity-manager-project.
 */
public class TerminalCommunication {

	private static final Logger LOG = LoggerFactory.getLogger(TerminalCommunication.class);
	private static String authData = "";
	private static final String loginDatafilename = "explorviz.capacity_manager.login_data.properties";

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

	public static String getAuthentificationData() throws FileNotFoundException, IOException,
			InvalidConfigurationException {
		if ((authData == null) || authData.equals("")) {

			final Properties settings = new Properties();
			String filepath = CapManConfiguration.getResourceFolder() + loginDatafilename;
			settings.load(new FileInputStream(filepath));

			String username = settings.getProperty("OS_USERNAME");
			String tenant = settings.getProperty("OS_TENANT_NAME");
			String url = settings.getProperty("OS_AUTH_URL");
			String password = settings.getProperty("OS_PASSWORD");

			if ((username == null) || (tenant == null) || (url == null) || (password == null)) {
				throw new InvalidConfigurationException(
						"The file "
								+ loginDatafilename
								+ " must provide the properties: OS_USERNAME, OS_TENANT_NAME, OS_AUTH_URL and OS_PASSWORD!");
			}

			authData = "--os_username=" + username//
					+ " --os_tenant_name=" + tenant//
					+ " --os_auth_url=" + url//
					+ " --os_password=" + password;
		}

		return authData;
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
