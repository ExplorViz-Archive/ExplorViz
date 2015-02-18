package explorviz.plugin_server.rootcausedetection.algorithm;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_server.rootcausedetection.algorithm.RGBAlgorithm.RGBTuple;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.*;

public class RGBAlgorithmTest {

	/**
	 * Tests the method {@link RGBAlgorithm#calculateColorFromRCR
	 * calculateColorFromRCR(final double)}
	 */
	@Test
	public void calculateColorFromRCRTest() {
		final RGBAlgorithm alg = new RGBAlgorithm();

		double rating = 0.0d;
		RGBTuple tuple = alg.calculateColorFromRCR(rating);

		assertTrue("expected: 0,255,0; returned: " + tuple.toString(),
				tuple.toString().equals("0,255,0"));

		rating = 0.5d;
		tuple = alg.calculateColorFromRCR(rating);

		assertTrue("expected: 255,255,0; returned: " + tuple.toString(),
				tuple.toString().equals("255,255,0"));

		rating = 1.0d;
		tuple = alg.calculateColorFromRCR(rating);

		assertTrue("expected: 255,0,0; returned: " + tuple.toString(),
				tuple.toString().equals("255,0,0"));
	}

	/**
	 * This methods tests if the RGBAlgorithm writes correct data for the use of
	 * later components.
	 */
	@Test
	public void checkForCorrectOutputInLandscape() {
		// Test 1
		Landscape landscape = TestLandscapeBuilder.createStandardLandscape(0);
		RanCorrLandscape rcLandscape = new RanCorrLandscape(landscape);

		for (Application app : rcLandscape.getApplications()) {
			app.setRootCauseRating(1.0d);
			app.setIsRankingPositive(true);
		}

		AbstractPersistAlgorithm alg = new RGBAlgorithm();
		alg.persist(rcLandscape);

		for (Application app : rcLandscape.getApplications()) {
			assertTrue(app.getGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR).equals(
					"255,0,0"));
			assertTrue(app.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY) == 1.0d);
		}

		// Test 2
		landscape = TestLandscapeBuilder.createStandardLandscape(0);
		rcLandscape = new RanCorrLandscape(landscape);

		for (Application app : rcLandscape.getApplications()) {
			app.setRootCauseRating(1.0d);
			app.setIsRankingPositive(false);
		}

		alg = new RGBAlgorithm();
		alg.persist(rcLandscape);

		for (Application app : rcLandscape.getApplications()) {
			assertTrue(app.getGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR).equals(
					"255,0,0"));
			assertTrue(
					"value: " + app.getRootCauseRating(),
					app.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY) == -1.0d);
		}

		// Test 3
		landscape = TestLandscapeBuilder.createStandardLandscape(0);
		rcLandscape = new RanCorrLandscape(landscape);

		for (Application app : rcLandscape.getApplications()) {
			app.setRootCauseRating(0.0d);
			app.setIsRankingPositive(false);
		}

		alg = new RGBAlgorithm();
		alg.persist(rcLandscape);

		for (Application app : rcLandscape.getApplications()) {
			assertTrue(app.getGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR).equals(
					"0,255,0"));
			assertTrue(app.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY) == 0.0d);
		}

		// Test 4
		landscape = TestLandscapeBuilder.createStandardLandscape(0);
		rcLandscape = new RanCorrLandscape(landscape);

		for (Application app : rcLandscape.getApplications()) {
			app.setRootCauseRating(0.0d);
			app.setIsRankingPositive(true);
		}

		alg = new RGBAlgorithm();
		alg.persist(rcLandscape);

		for (Application app : rcLandscape.getApplications()) {
			assertTrue(app.getGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR).equals(
					"0,255,0"));
			assertTrue(app.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY) == 0.0d);
		}
	}
}
