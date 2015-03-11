package explorviz.plugin_server.anomalydetection.forecast;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

public class WeightedForecasterTest {

	private static String actualWeight = Configuration.WEIGHTED_FORECASTER_WEIGHT;
	private static TreeMapLongDoubleIValue historyResponseTimes;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void beforeClass() {
		historyResponseTimes = new TreeMapLongDoubleIValue();
		for (int i = 1; i <= 15; i++) {
			historyResponseTimes.put(new Long(i), i * 1.0);
		}
	}

	@Test
	public void testLogarithmicWeighting() {
		Configuration.WEIGHTED_FORECASTER_WEIGHT = "LOGARITHMIC";
		assertEquals(9.07219, WeightedForecaster.forecast(historyResponseTimes), 0.00001);
	}

	@Test
	public void testLinearlyWeighting() {
		Configuration.WEIGHTED_FORECASTER_WEIGHT = "LINEARLY";
		assertEquals(10.33333, WeightedForecaster.forecast(historyResponseTimes), 0.00001);
	}

	@Test
	public void testExponetiallyWeighting() {
		Configuration.WEIGHTED_FORECASTER_WEIGHT = "EXPONENTIALLY";
		assertEquals(14.00045, WeightedForecaster.forecast(historyResponseTimes), 0.00001);
	}

	@Test
	public void testFalseWeightinf() {
		Configuration.WEIGHTED_FORECASTER_WEIGHT = "FALSE_WEIGHTING_STRING";
		thrown.expect(FalseWeightInConfigurationException.class);
		thrown.expectMessage("False weight for WeightedForecaster. Check Configuration!");
		WeightedForecaster.forecast(historyResponseTimes);
	}

	@AfterClass
	public static void afterClass() {
		Configuration.WEIGHTED_FORECASTER_WEIGHT = actualWeight;
	}

}
