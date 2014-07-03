package explorviz.server.repository.helper;

import static org.junit.Assert.*;

import org.junit.Test;

public class SignatureParserTest {

	@Test
	public void testParse() throws Exception {
		Signature sig = SignatureParser
				.parse("kieker.monitoring.core.controller.MonitoringController$1.<init>(kieker.monitoring.core.controller.MonitoringController)",
						true);
		assertEquals("new MonitoringController$1", sig.getOperationName());

		sig = SignatureParser
				.parse("public kieker.common.record.flow.trace.operation.BeforeOperationEvent.<init>(long, long, int, java.lang.String, java.lang.String)",
						true);
		assertEquals("new BeforeOperationEvent", sig.getOperationName());

		sig = SignatureParser
				.parse("protected final void kieker.monitoring.core.controller.MonitoringController.init()",
						false);
		assertEquals("init", sig.getOperationName());
	}
}
