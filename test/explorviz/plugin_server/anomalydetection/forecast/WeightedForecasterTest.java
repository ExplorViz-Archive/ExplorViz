package explorviz.plugin_server.anomalydetection.forecast;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.anomalydetection.Configuration;

public class WeightedForecasterTest {

	private static String actualWeight = Configuration.WEIGHTING_FORECASTER_WEIGHT;
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
	public void testLowWeighting() {
		Configuration.WEIGHTING_FORECASTER_WEIGHT = "LOW";
		assertEquals(9.09803921569, WeightedForecaster.forecast(historyResponseTimes), 0.00001);
	}

	@Test
	public void testMeanWeighting() {
		Configuration.WEIGHTING_FORECASTER_WEIGHT = "MEAN";
		assertEquals(10.33333, WeightedForecaster.forecast(historyResponseTimes), 0.00001);
	}

	@Test
	public void testStrongWeighting() {
		Configuration.WEIGHTING_FORECASTER_WEIGHT = "STRONG";
		assertEquals(14.00045, WeightedForecaster.forecast(historyResponseTimes), 0.00001);
	}

	@Test
	public void testFalseWeightinf() {
		Configuration.WEIGHTING_FORECASTER_WEIGHT = "FALSE_WEIGHTING_STRING";
		thrown.expect(FalseWeightInConfigurationException.class);
		thrown.expectMessage("False weight for WeightedForecaster. Check Configuration!");
		WeightedForecaster.forecast(historyResponseTimes);
	}

	@AfterClass
	public static void afterClass() {
		Configuration.WEIGHTING_FORECASTER_WEIGHT = actualWeight;
	}

}
