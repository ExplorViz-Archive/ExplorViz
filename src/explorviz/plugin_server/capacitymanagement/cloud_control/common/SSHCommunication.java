package explorviz.plugin_server.capacitymanagement.cloud_control.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.jcraft.jsch.*;

/**
 * This Class executes script with ssh-connection.<br>
 * Taken from capacity-manager-project.
 */
public class SSHCommunication {

	private static final Logger LOG = Logger.getLogger("SSHCommunication");

	/**
	 * runs a given command via ssh on host
	 *
	 * @param host
	 *            IP of host
	 * @param sshUsername
	 *            usernamke
	 * @param privateKey
	 *            private ssh key
	 * @param command
	 *            ssh command
	 * @return output
	 * @throws Exception
	 *             connection and/or input errors
	 */
	public static List<String> runScriptViaSSH(final String host, final String sshUsername,
			final String privateKey, final String command) throws Exception {
		final List<String> output = new ArrayList<String>();

		Session session = null;
		BufferedReader in = null;
		BufferedReader err = null;
		try {
			final JSch jsch = new JSch();
			session = jsch.getSession(sshUsername, host, 22);
			if (privateKey != null) {
				jsch.addIdentity(privateKey);
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			final ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

			in = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
			err = new BufferedReader(new InputStreamReader(channelExec.getErrStream()));

			channelExec.setCommand(command);
			channelExec.connect();
			String result = in.readLine();
			while (!channelExec.isEOF() || (result != null)) {

				if (result != null) {
					output.add(result);
				}
				result = in.readLine();
			}

			channelExec.disconnect();
		} finally {
			readInErrorsIfOccurred(output, err);

			closeOpenIO(session, in, err);
		}

		return output;
	}

	private static void readInErrorsIfOccurred(final List<String> output, final BufferedReader err) {
		try {
			if (err != null) {
				while (err.ready()) {
					output.add(err.readLine());
				}
			}
		} catch (final IOException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void closeOpenIO(final Session session, final BufferedReader in,
			final BufferedReader err) {
		if (session != null) {
			session.disconnect();
		}
		try {
			if ((in != null) && in.ready()) {
				in.close();
			}
			if ((err != null) && err.ready()) {
				err.close();
			}
		} catch (final IOException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}
	}
}
