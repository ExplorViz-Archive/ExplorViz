package explorviz.plugin_server.rootcausedetection.algorithm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin_server.rootcausedetection.RanCorrConfiguration;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.Clazz;

public class LocalAlgorithmTest {

	private RanCorrLandscape rcLandscape = null;

	/**
	 * This methods sets up a very specific test landscape.
	 */
	@Before
	public void setUpLandscape() {
		rcLandscape = new RanCorrLandscape(RCDTestLandscapeBuilder.getLocalAlgorithmLandscape());
	}

	/**
	 * This method tests the LocalAlgorithm for various amounts of threads.
	 */
	@Test
	public void localAlgorithmTest() {
		// correct landscape?
		assertTrue(rcLandscape.getOperations().size() == 4);
		assertTrue("value=" + rcLandscape.getClasses().size(), rcLandscape.getClasses().size() == 3);
		assertTrue(rcLandscape.getPackages().size() == 2);
		assertTrue(rcLandscape.getApplications().size() == 2);

		// 1 Thread
		AbstractRanCorrAlgorithm alg = new LocalAlgorithm();
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
		boolean cl3Done = false;
		for (Clazz clazz : rcLandscape.getClasses()) {
			if (withEpsilon(clazz.getRootCauseRating(), 0.5125d, 0.01d)) {
				cl1Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.3d, 0.01d)) {
				cl2Done = true;
			} else if (withEpsilon(clazz.getRootCauseRating(), 0.1d, 0.01d)) {
				cl3Done = true;
			} else {
				fail("Failed: RCR=" + clazz.getRootCauseRating());
			}
		}

		assertTrue(cl1Done && cl2Done && cl3Done);
	}

	private boolean withEpsilon(double is, double should, double epsilon) {
		return (Math.abs(is - should) - epsilon) <= 0.0d;
	}

}
