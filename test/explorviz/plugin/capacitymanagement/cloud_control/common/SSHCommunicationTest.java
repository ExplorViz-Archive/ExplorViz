package explorviz.plugin.capacitymanagement.cloud_control.common;

import org.junit.Test;

public class SSHCommunicationTest {

	@Test
	public void testRunSSHCommand() throws Exception {
		// List<String> output =
		// SSHCommunication.runScriptViaSSH("192.168.48.67", "ubuntu",
		// "/home/julia/Dokumente/Masterprojekt/ssh/default.pem",
		// "nohup ./endlos > /dev/null 2>&1 &");
		// System.out.println(output);
		// List<String> pid = SSHCommunication.runScriptViaSSH("192.168.48.67",
		// "ubuntu",
		// "/home/julia/Dokumente/Masterprojekt/ssh/default.pem",
		// "pidof endlos");
		// System.out.println(pid);
		// List<String> output2 =
		// SSHCommunication.runScriptViaSSH("192.168.48.67", "ubuntu",
		// "/home/julia/Dokumente/Masterprojekt/ssh/default.pem",
		// "pkill endlos");
		// System.out.println(output2);
		// try {
		// List<String> pid2 = SSHCommunication.runScriptViaSSH("192.168.48.67",
		// "ubuntu",
		// "/home/julia/Dokumente/Masterprojekt/ssh/default.pem",
		// "pidof endlos");
		// System.out.println(pid2);
		// } catch (Exception e) {
		//
		// System.out.println(e.getMessage());
		// }

		// List<String> output = SSHCommunication
		// .runScriptViaSSH(
		// "192.168.48.64",
		// "ubuntu",
		// "/home/julia/Dokumente/Masterprojekt/ssh/testjkr.pem",
		// "cd DemoA  && echo ' echo $! > pid' > getpidcommand && cat start.sh getpidcommand > script.sh && chmod a+x script.sh && ./script.sh DemoA");
		// System.out.println(output);
		//
		// List<String> output1 =
		// SSHCommunication.runScriptViaSSH("192.168.48.64", "ubuntu",
		// "/home/julia/Dokumente/Masterprojekt/ssh/testjkr.pem",
		// "cd DemoA && head pid");
		// System.out.println(output1);

	}
}
