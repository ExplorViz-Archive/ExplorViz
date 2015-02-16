package explorviz.plugin.capacitymanagement.cloud_control.common;

import java.util.List;

import org.junit.Test;

import explorviz.plugin_server.capacitymanagement.cloud_control.common.SSHCommunication;

public class SSHCommunicationTest {

	@Test
	public void testRunSSHCommand() throws Exception {
		List<String> output = SSHCommunication.runScriptViaSSH("192.168.48.67", "ubuntu",
				"/home/julia/Dokumente/Masterprojekt/ssh/default.pem",
				"nohup ./endlos > /dev/null 2>&1 &");
		System.out.println(output);
		List<String> pid = SSHCommunication.runScriptViaSSH("192.168.48.67", "ubuntu",
				"/home/julia/Dokumente/Masterprojekt/ssh/default.pem", "pidof endlos");
		System.out.println(pid);
		List<String> output2 = SSHCommunication.runScriptViaSSH("192.168.48.67", "ubuntu",
				"/home/julia/Dokumente/Masterprojekt/ssh/default.pem", "pkill endlos");
		System.out.println(output2);
		try {
			List<String> pid2 = SSHCommunication.runScriptViaSSH("192.168.48.67", "ubuntu",
					"/home/julia/Dokumente/Masterprojekt/ssh/default.pem", "pidof endlos");
			System.out.println(pid2);
		} catch (Exception e) {

			System.out.println(e.getMessage());
		}
	}
}
