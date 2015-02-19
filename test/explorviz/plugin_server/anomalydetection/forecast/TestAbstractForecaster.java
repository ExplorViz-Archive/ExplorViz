package explorviz.plugin_server.anomalydetection.forecast;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

public class TestAbstractForecaster {

	private static TreeMapLongDoubleIValue enoughHistoryResponseTimes;
	private static TreeMapLongDoubleIValue enoughHistoryForecastResponseTimes;
	private static TreeMapLongDoubleIValue notEnoughHistoryResponseTimes;
	private static TreeMapLongDoubleIValue notEnoughHistoryForecastResponseTimes;
	private static TreeMapLongDoubleIValue nullValues;
	private static TreeMapLongDoubleIValue oneValue;
	private static String actualForecaster = Configuration.FORECASTING_ALGORITHM;
	private static String actualInitForecaster = Configuration.INIT_FORECASTING_ALGORITHM;
	private static String actualWeight = Configuration.WEIGHTING_FORECASTER_WEIGHT;
	private static int actualWindowSize = Configuration.TIME_SERIES_WINDOW_SIZE;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void beforeClass() {
		Configuration.TIME_SERIES_WINDOW_SIZE = 15;
		enoughHistoryResponseTimes = new TreeMapLongDoubleIValue();
		enoughHistoryForecastResponseTimes = new TreeMapLongDoubleIValue();
		notEnoughHistoryResponseTimes = new TreeMapLongDoubleIValue();
		notEnoughHistoryForecastResponseTimes = new TreeMapLongDoubleIValue();
		nullValues = new TreeMapLongDoubleIValue();
		nullValues.put(1L, 3.4);
		oneValue = new TreeMapLongDoubleIValue();
		oneValue.put(0L, 2.3);
		oneValue.put(1L, 3.4);
	}

	@Before
	public void before() {
		for (int i = 1; i <= 21; i++) {
			enoughHistoryResponseTimes.put(new Long(i), i * 1.5);
		}

		for (int i = 1; i <= 21; i++) {
			enoughHistoryForecastResponseTimes.put(new Long(i), i * 1.0);
		}

		for (int i = 1; i <= 11; i++) {
			notEnoughHistoryResponseTimes.put(new Long(i), i * 1.0);
		}

		for (int i = 1; i <= 11; i++) {
			notEnoughHistoryForecastResponseTimes.put(new Long(i), i * 1.5);
		}
	}

	@Test
	public void testNaiveForecastWithEnoughValues() {
		Configuration.FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.NaiveForecaster";
		assertEquals(30, AbstractForecaster.forecast(enoughHistoryResponseTimes,
				enoughHistoryForecastResponseTimes), 0);
	}

	@Test
	public void testMovingAverageForecastWithEnoughValues() {
		Configuration.FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.MovingAverageForecaster";
		assertEquals(19.5, AbstractForecaster.forecast(enoughHistoryResponseTimes,
				enoughHistoryForecastResponseTimes), 0);
	}

	@Test
	public void testWeightedForecasterWithEnoughValues() {
		Configuration.WEIGHTING_FORECASTER_WEIGHT = "LOW";
		Configuration.FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.WeightedForecaster";
		assertEquals(21.14705, AbstractForecaster.forecast(enoughHistoryResponseTimes,
				enoughHistoryForecastResponseTimes), 0.00001);
	}

	@Test
	public void testFalsePathWithEnoughValues() {
		Configuration.FORECASTING_ALGORITHM = "this/is/a/false/path";
		thrown.expect(ForecasterNotFoundException.class);
		thrown.expectMessage("Forecaster not available. Check configuration!");
		AbstractForecaster.forecast(enoughHistoryResponseTimes, enoughHistoryForecastResponseTimes);
	}

	@Test
	public void testNaiveForecastWithNotEnoughValues() {
		Configuration.INIT_FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.NaiveForecaster";
		assertEquals(10, AbstractForecaster.forecast(notEnoughHistoryResponseTimes,
				notEnoughHistoryForecastResponseTimes), 0);
	}

	@Test
	public void testMovingAverageWithNotEnoughValues() {
		Configuration.INIT_FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.MovingAverageForecaster";
		assertEquals(5.5, AbstractForecaster.forecast(notEnoughHistoryResponseTimes,
				notEnoughHistoryForecastResponseTimes), 0);
	}

	@Test
	public void testWeightedForecasterWithNotEnoughValues() {
		Configuration.WEIGHTING_FORECASTER_WEIGHT = "LOW";
		Configuration.INIT_FORECASTING_ALGORITHM = "explorviz.plugin_server.anomalydetection.forecast.WeightedForecaster";
		assertEquals(6.06896, AbstractForecaster.forecast(notEnoughHistoryResponseTimes,
				notEnoughHistoryForecastResponseTimes), 0.00001);
	}

	@Test
	public void testForecastWithNullValues() {
		assertEquals(3.4,
				AbstractForecaster.forecast(nullValues, notEnoughHistoryForecastResponseTimes), 0);
	}

	@Test
	public void testForecastWithOneValue() {
		assertEquals(2.3,
				AbstractForecaster.forecast(oneValue, notEnoughHistoryForecastResponseTimes), 0);
	}

	@Test
	public void testFalseWithNotEnoughValues() {
		Configuration.INIT_FORECASTING_ALGORITHM = "this/is/a/false/path";
		thrown.expect(ForecasterNotFoundException.class);
		thrown.expectMessage("Forecaster not available as initialization-algorithm. Check configuration!");
		AbstractForecaster.forecast(notEnoughHistoryResponseTimes,
				notEnoughHistoryForecastResponseTimes);
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

		TreeMapLongDoubleIValue resultMapFromOPADx = AbstractForecaster.delimitTreeMap(map);

		assertEquals(resultMap.size(), resultMapFromOPADx.size());
		for (int i = resultMapFromOPADx.size() - 1; i > 2; i--) {
			assertEquals(resultMap.get(new Long(i)), resultMapFromOPADx.get(new Long(i)), 0);
		}
	}

	@AfterClass
	public static void afterClass() {
		Configuration.FORECASTING_ALGORITHM = actualForecaster;
		Configuration.INIT_FORECASTING_ALGORITHM = actualInitForecaster;
		Configuration.TIME_SERIES_WINDOW_SIZE = actualWindowSize;
		Configuration.WEIGHTING_FORECASTER_WEIGHT = actualWeight;
	}
}