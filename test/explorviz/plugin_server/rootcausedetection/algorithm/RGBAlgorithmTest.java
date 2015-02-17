package explorviz.plugin_server.rootcausedetection.algorithm;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.plugin_server.rootcausedetection.algorithm.RGBAlgorithm.RGBTuple;

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

	@Test
	public void checkForCorrectOutputInLandscape() {
	}
}
