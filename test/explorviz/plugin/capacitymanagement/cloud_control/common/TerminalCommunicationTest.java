package explorviz.plugin.capacitymanagement.cloud_control.common;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TerminalCommunicationTest {
	@Test
	public void testSendCommand() throws Exception {
		final String text = "testing echo on console";
		final String command = "echo " + text;
		final List<String> output = TerminalCommunication.executeCommand(command);
		Assert.assertEquals("Echo should be read from console.", text, output.get(0));
	}
}
