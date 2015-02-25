package explorviz.plugin_server.anomalydetection.forecast;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;

public class TestNaiveForecaster {

	private static TreeMapLongDoubleIValue historyResponseTimes;

	@BeforeClass
	public static void beforeClass() {
		historyResponseTimes = new TreeMapLongDoubleIValue();
		historyResponseTimes.put(new Long(1), 7.6);
		historyResponseTimes.put(new Long(2), 3.4);
		historyResponseTimes.put(new Long(3), 2.4);
		historyResponseTimes.put(new Long(6), 4.6);
		historyResponseTimes.put(new Long(4), 4.6);
		historyResponseTimes.put(new Long(5), 7.9);
		historyResponseTimes.put(new Long(7), 12.0);
		historyResponseTimes.put(new Long(8), 11.1);
		historyResponseTimes.put(new Long(1023245323), 7.49275);
		historyResponseTimes.put(new Long(11), 9.87654123);
	}

	@Test
	public void testForecast() {
		assertEquals(7.49275, NaiveForecaster.forecast(historyResponseTimes), 0);
	}
}
