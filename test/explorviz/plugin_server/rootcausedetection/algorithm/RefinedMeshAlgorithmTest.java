package explorviz.plugin_server.rootcausedetection.algorithm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.Clazz;

public class RefinedMeshAlgorithmTest {

	private RanCorrLandscape rcLandscape = null;

	/**
	 * This methods sets up a very specific test landscape.
	 */
	@Before
	public void setUpLandscape() {
		rcLandscape = new RanCorrLandscape(
				RCDTestLandscapeBuilder.getRefinedMeshAlgorithmLandscape());
	}

	/**
	 * This method tests the LocalAlgorithm for various amounts of threads.
	 */
	@Test
	public void refinedMeshAlgorithmTest() {
		// correct landscape?
		assertTrue(rcLandscape.getOperations().size() == 8);
		assertTrue("value=" + rcLandscape.getClasses().size(), rcLandscape.getClasses().size() == 6);
		assertTrue(rcLandscape.getPackages().size() == 2);
		assertTrue(rcLandscape.getApplications().size() == 1);

		// 1 Thread
		AbstractRanCorrAlgorithm alg = new RefinedMeshAlgorithm();
		RanCorrConfiguration.numberOfThreads = 1;
		doAlgorithm(alg);

		// 2 Threads
		RanCorrConfiguration.numberOfThreads = 2;
		for (int i = 0; i < 100; i++) {
			doAlgorithm(alg);
		}

		// 8 Threads
		RanCorrConfiguration.numberOfThreads = 8;
		for (int i = 0; i < 100; i++) {
			doAlgorithm(alg);
		}
	}

	private void doAlgorithm(AbstractRanCorrAlgorithm alg) {
		alg.calculate(rcLandscape);

		boolean cl1Done = false;
		boolean cl2Done = false;
		boolean cl4Done = false;
		boolean cl5Done = false;
		boolean cl6Done = false;
		boolean cl7Done = false;

		for (Clazz clazz : rcLandscape.getClasses()) {
			if (withEpsilon(clazz.getRootCauseRating(), 0.5d, 0.01d)) {
				cl1Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.26d, 0.01d)) {
				cl2Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.3d, 0.01d)) {
				cl4Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.357d, 0.01d)) {
				cl5Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.47d, 0.01d)) {
				cl6Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.321d, 0.01d)) {
				cl7Done = true;
			} else {
				fail("Failed: RCR=" + clazz.getRootCauseRating() + clazz.getName());
			}
		}
		assertTrue("cl1NotDone", cl1Done);
		assertTrue("cl2NotDone", cl2Done);
		assertTrue("cl4NotDone", cl4Done);
		assertTrue("cl5NotDone", cl5Done);
		assertTrue("cl6NotDone", cl6Done);
		assertTrue("cl7NotDone", cl7Done);
	}

	private boolean withEpsilon(double is, double should, double epsilon) {
		return (Math.abs(is - should) - epsilon) <= 0.0d;
	}
}
