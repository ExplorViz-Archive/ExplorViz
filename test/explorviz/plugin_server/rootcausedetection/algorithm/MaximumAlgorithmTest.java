package explorviz.plugin_server.rootcausedetection.algorithm;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.*;

public class MaximumAlgorithmTest {

	/**
	 * This method tests if the MaximumAlgorithm correctly aggregates ratings to
	 * application level.
	 */
	@Test
	public void aggregateTest() {
		Landscape landscape = TestLandscapeBuilder.createStandardLandscape(0);
		RanCorrLandscape rcLandscape = new RanCorrLandscape(landscape);

		double current = 0.0d;
		Application maxRating = null;
		for (Clazz clazz : rcLandscape.getClasses()) {
			clazz.setRootCauseRating(current);
			current += 0.01d;
			maxRating = clazz.getParent().getBelongingApplication();
		}

		MaximumAlgorithm alg = new MaximumAlgorithm();
		alg.aggregate(rcLandscape);

		double sum = 0.0d;
		Application best = null;
		for (Application application : rcLandscape.getApplications()) {
			sum += application.getRootCauseRating();
			if ((best == null) || (best.getRootCauseRating() < application.getRootCauseRating())) {
				best = application;
			}
		}

		// ratings should be normalized
		assertTrue("expected = 1, currently = " + sum, withEpsilon(sum, 1.0d, 0.00001d));
		// ratings should be correctly raised to application level
		assertTrue(best == maxRating);
	}

	private boolean withEpsilon(double is, double should, double epsilon) {
		return (Math.abs(is - should) - epsilon) <= 0.0d;
	}

}
