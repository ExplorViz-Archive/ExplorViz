package explorviz.visualization.engine.main;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ClassnameSplitterTest {

	@Test
	public void testSplitClassname() throws Exception {
		List<String> result = ClassnameSplitter.splitClassname(
				"MonitoredClassManualInstrumentation", 14, 2);

		assertEquals(2, result.size());
		assertEquals("MonitoredClassManual", result.get(0));
		assertEquals("Instrumentation", result.get(1));

		result = ClassnameSplitter.splitClassname("MonitoredClassManualInstrumentation", 10, 2);

		assertEquals(2, result.size());
		assertEquals("MonitoredClassManual", result.get(0));
		assertEquals("Instrumentation", result.get(1));

		result = ClassnameSplitter.splitClassname("MonitoredClassManualInstrumentation", 10, 3);

		assertEquals(3, result.size());
		assertEquals("MonitoredClass", result.get(0));
		assertEquals("Manual", result.get(1));
		assertEquals("Instrumentation", result.get(2));

		result = ClassnameSplitter.splitClassname("Monitored", 10, 2);

		assertEquals(1, result.size());
		assertEquals("Monitored", result.get(0));

		result = ClassnameSplitter.splitClassname("Monitored", 4, 2);

		assertEquals(2, result.size());
		assertEquals("Monit", result.get(0));
		assertEquals("ored", result.get(1));

		result = ClassnameSplitter.splitClassname("IMonitoringclassedxyzxyz", 4, 2);

		assertEquals(2, result.size());
		assertEquals("IMonitoringc", result.get(0));
		assertEquals("lassedxyzxyz", result.get(1));

		result = ClassnameSplitter.splitClassname("FsWriterThread", 4, 2);

		assertEquals(2, result.size());
		assertEquals("FsWriter", result.get(0));
		assertEquals("Thread", result.get(1));

		result = ClassnameSplitter.splitClassname("RegistryRecord", 4, 2);

		assertEquals(2, result.size());
		assertEquals("Registry", result.get(0));
		assertEquals("Record", result.get(1));
	}

	@Test
	public void testDoHyphenation() throws Exception {
		List<String> result = new ArrayList<String>();
		ClassnameSplitter.doHyphenation("Monitored", 2, result);

		assertEquals(2, result.size());
		assertEquals("Monit", result.get(0));
		assertEquals("ored", result.get(1));

		result = new ArrayList<String>();
		ClassnameSplitter.doHyphenation("Instrumentation", 2, result);

		assertEquals(2, result.size());
		assertEquals("Instrume", result.get(0));
		assertEquals("ntation", result.get(1));
	}
}
