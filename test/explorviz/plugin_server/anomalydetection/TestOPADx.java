package explorviz.plugin_server.anomalydetection;
import static org.junit.Assert.*;

import org.junit.*;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;
import explorviz.plugin_server.anomalydetection.OPADx;
import explorviz.shared.model.Landscape;
import explorviz.shared.model.TestLandscapeBuilder;

public class TestOPADx {

	private static OPADx opadx;
	private Landscape landscape;

	@BeforeClass
	public static void beforeClass() {
		opadx = new OPADx();
	}

	@Before
	public void before() {
		landscape = TestLandscapeBuilder.createStandardLandscape(0);
		// TODO create Clazz and CommunicationClazz
	}

	@Test
	public void testDelimitTreeMap() {
		TreeMapLongDoubleIValue map = new TreeMapLongDoubleIValue();
		for (int i = 0; i < (Configuration.TIME_SERIES_WINDOW_SIZE + 2); i++) {
			map.put(new Long(i), i * 2.0);
		}
		TreeMapLongDoubleIValue resultMap = new TreeMapLongDoubleIValue();
		resultMap.putAll(map);
		resultMap.remove(0L);
		resultMap.remove(1L);

		TreeMapLongDoubleIValue resultMapFromOPADx = opadx.delimitTreeMap(map);

		assertEquals(resultMap.size(), resultMapFromOPADx.size());
		for (int i = resultMapFromOPADx.size() - 1; i > 2; i--) {
			assertEquals(resultMap.get(new Long(i)), resultMapFromOPADx.get(new Long(i)), 0);
		}
	}

}
