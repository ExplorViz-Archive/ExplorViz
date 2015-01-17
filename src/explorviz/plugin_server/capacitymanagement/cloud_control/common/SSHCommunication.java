package explorviz.plugin_server.capacitymanagement.cloud_control.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.*;

/**
 * @author jgi,dj This Class executes script with ssh-connection
 */
public class SSHCommunication {

	private static final Logger LOG = LoggerFactory.getLogger(SSHCommunication.class);

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

			while (in.ready()) {
				output.add(in.readLine());
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
			LOG.error(e.getMessage(), e);
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
			LOG.error(e.getMessage(), e);
		}
	}
}
