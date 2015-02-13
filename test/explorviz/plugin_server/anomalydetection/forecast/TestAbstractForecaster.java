package explorviz.plugin_server.anomalydetection.forecast;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

public class TestAbstractForecaster {

	private TreeMapLongDoubleIValue historyResponseTimes;
	private TreeMapLongDoubleIValue historyForecastResponseTimes;
	private AbstractForecaster abstractForecaster;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void before() {
		historyResponseTimes = new TreeMapLongDoubleIValue();
		historyResponseTimes.put(new Long(1), 7.6);
		historyResponseTimes.put(new Long(2), 3.4);
		historyResponseTimes.put(new Long(3), 2.4);
		historyResponseTimes.put(new Long(6), 4.6);
		historyResponseTimes.put(new Long(4), 4.6);
		historyResponseTimes.put(new Long(5), 7.9);
		historyResponseTimes.put(new Long(7), 12.0);
		historyResponseTimes.put(new Long(8), 11.1);
		historyResponseTimes.put(new Long(9), 7.4);
		historyResponseTimes.put(new Long(10), 9.8);

		historyForecastResponseTimes = new TreeMapLongDoubleIValue();
		historyForecastResponseTimes.put(new Long(1), 7.0);
		historyForecastResponseTimes.put(new Long(2), 3.8);
		historyForecastResponseTimes.put(new Long(3), 2.5);
		historyForecastResponseTimes.put(new Long(6), 4.6);
		historyForecastResponseTimes.put(new Long(4), 5.9);
		historyForecastResponseTimes.put(new Long(5), 8.9);
		historyForecastResponseTimes.put(new Long(7), 10.3);
		historyForecastResponseTimes.put(new Long(8), 11.0);
		historyForecastResponseTimes.put(new Long(9), 7.4);
		historyForecastResponseTimes.put(new Long(10), 9.9);
	}

	@Test
	public void test() {
		Configuration.FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.NaiveForecaster";
		assertEquals(9.8,
				abstractForecaster.forecast(historyResponseTimes, historyForecastResponseTimes),
				0.001);
		Configuration.FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.MovingAverageForecaster";
		assertEquals(7.08,
				abstractForecaster.forecast(historyResponseTimes, historyForecastResponseTimes),
				0.001);
		Configuration.FORECASTING_ALGORITHM = "this/is/a/false/path";
		thrown.expect(ForecasterNotFoundException.class);
		thrown.expectMessage("Forecaster not available. Check configuration!");
		abstractForecaster.forecast(historyResponseTimes, historyForecastResponseTimes);

	}

}
