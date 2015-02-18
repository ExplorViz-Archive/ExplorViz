package explorviz.plugin_server.anomalydetection.forecast;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;

public class TestMovingAverageForecaster {

	private static TreeMapLongDoubleIValue historyResponseTimes;

	@BeforeClass
	public static void beforeClass() {
		historyResponseTimes = new TreeMapLongDoubleIValue();
		for (int i = 6; i <= 20; i++) {
			historyResponseTimes.put(new Long(i), i * 1.5);
		}
	}

	@Test
	public void testForecast() {
		assertEquals(19.5, MovingAverageForecaster.forecast(historyResponseTimes), 0);
	}

}
