package explorviz.plugin_server.rootcausedetection;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_server.rootcausedetection.algorithm.*;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.*;

public class RootCauseDetectionTest {

	/**
	 * This method tests the complete RootCauseDetection using the local
	 * algorithm and multiple threading configurations.
	 */
	@Test
	public void LocalAlgorithmComponentTest() {
		RanCorrConfiguration.numberOfThreads = 1;
		RanCorrConfiguration.ranCorrAlgorithm = new LocalAlgorithm();
		calculateLocalAlgorithm();

		RanCorrConfiguration.numberOfThreads = 2;
		for (int i = 0; i < 100; i++) {
			calculateLocalAlgorithm();
		}

		RanCorrConfiguration.numberOfThreads = 8;
		for (int i = 0; i < 100; i++) {
			calculateLocalAlgorithm();
		}
	}

	/**
	 * This method tests the complete RootCauseDetection using the neighbour
	 * algorithm and multiple threading configurations.
	 */
	@Test
	public void NeighbourAlgorithmComponentTest() {
		RanCorrConfiguration.numberOfThreads = 1;
		RanCorrConfiguration.ranCorrAlgorithm = new NeighbourAlgorithm();
		calculateNeighbourAlgorithm();

		RanCorrConfiguration.numberOfThreads = 2;
		for (int i = 0; i < 100; i++) {
			calculateNeighbourAlgorithm();
		}

		RanCorrConfiguration.numberOfThreads = 8;
		for (int i = 0; i < 100; i++) {
			calculateNeighbourAlgorithm();
		}
	}

	/**
	 * This method tests the complete RootCauseDetection using the mesh
	 * algorithm and multiple threading configurations.
	 */
	@Test
	public void MeshAlgorithmComponentTest() {
		RanCorrConfiguration.numberOfThreads = 1;
		RanCorrConfiguration.ranCorrAlgorithm = new MeshAlgorithm();
		calculateMeshAlgorithm();

		RanCorrConfiguration.numberOfThreads = 2;
		for (int i = 0; i < 100; i++) {
			calculateMeshAlgorithm();
		}

		RanCorrConfiguration.numberOfThreads = 8;
		for (int i = 0; i < 100; i++) {
			calculateMeshAlgorithm();
		}
	}

	/**
	 * This method tests the complete RootCauseDetection using the refinedmesh
	 * algorithm and multiple threading configurations.
	 */
	@Test
	public void RefinedMeshAlgorithmComponentTest() {
		RanCorrConfiguration.numberOfThreads = 1;
		RanCorrConfiguration.ranCorrAlgorithm = new RefinedMeshAlgorithm();
		calculateRefinedMeshAlgorithm();

		RanCorrConfiguration.numberOfThreads = 2;
		for (int i = 0; i < 100; i++) {
			calculateRefinedMeshAlgorithm();
		}

		RanCorrConfiguration.numberOfThreads = 8;
		for (int i = 0; i < 100; i++) {
			calculateRefinedMeshAlgorithm();
		}
	}

	private void calculateRefinedMeshAlgorithm() {
		Landscape landscape = RCDTestLandscapeBuilder.getRefinedMeshAlgorithmLandscape();
		Clazz c1 = new Clazz();
		Clazz c2 = new Clazz();
		Clazz c3 = new Clazz();
		Clazz c4 = new Clazz();
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
		.get(0).getComponents().get(0).getClazzes().add(c1);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
		.get(0).getComponents().get(0).getClazzes().add(c2);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
		.get(0).getComponents().get(0).getClazzes().add(c3);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
		.get(0).getComponents().get(0).getClazzes().add(c4);
		RanCorr rancorr = new RanCorr();
		rancorr.doRootCauseDetection(landscape);

		RanCorrLandscape rcLandscape = new RanCorrLandscape(landscape);
		assertTrue(rcLandscape.getApplications().size() == 1);

		// for (Application application : rcLandscape.getApplications()) {
		// String rgb =
		// application.getGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR);
		// double rcr = application
		// .getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);
		//
		// if (!(rgb.equals("255,0,0") && withEpsilon(rcr, 1.0, 0.001d))) {
		// fail();
		// }
		// }
	}

	private void calculateMeshAlgorithm() {
		Landscape landscape = RCDTestLandscapeBuilder.getMeshAlgorithmLandscape();
		Clazz c1 = new Clazz();
		Clazz c2 = new Clazz();
		Clazz c3 = new Clazz();
		Clazz c4 = new Clazz();
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
		.get(0).getComponents().get(0).getClazzes().add(c1);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
		.get(0).getComponents().get(0).getClazzes().add(c2);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
		.get(0).getComponents().get(0).getClazzes().add(c3);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
		.get(0).getComponents().get(0).getClazzes().add(c4);
		RanCorr rancorr = new RanCorr();
		rancorr.doRootCauseDetection(landscape);

		RanCorrLandscape rcLandscape = new RanCorrLandscape(landscape);
		assertTrue(rcLandscape.getApplications().size() == 1);

		for (Application application : rcLandscape.getApplications()) {
			String rgb = application.getGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR);
			double rcr = application
					.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);

			if (!(rgb.equals("255,0,0") && withEpsilon(rcr, 1.0, 0.001d))) {
				fail();
			}
		}
	}

	private void calculateNeighbourAlgorithm() {
		Landscape landscape = RCDTestLandscapeBuilder.getNeighbourAlgorithmLandscape();
		Clazz c1 = new Clazz();
		Clazz c2 = new Clazz();
		Clazz c3 = new Clazz();
		Clazz c4 = new Clazz();
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
				.get(0).getComponents().get(0).getClazzes().add(c1);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
				.get(0).getComponents().get(0).getClazzes().add(c2);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
				.get(0).getComponents().get(0).getClazzes().add(c3);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
				.get(0).getComponents().get(0).getClazzes().add(c4);
		RanCorr rancorr = new RanCorr();
		rancorr.doRootCauseDetection(landscape);

		RanCorrLandscape rcLandscape = new RanCorrLandscape(landscape);
		assertTrue(rcLandscape.getApplications().size() == 1);

		for (Application application : rcLandscape.getApplications()) {
			String rgb = application.getGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR);
			double rcr = application
					.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);

			if (!(rgb.equals("255,0,0") && withEpsilon(rcr, -1.0, 0.001d))) {
				fail();
			}
		}
	}

	private void calculateLocalAlgorithm() {
		Landscape landscape = RCDTestLandscapeBuilder.getLocalAlgorithmLandscape();
		Clazz c1 = new Clazz();
		Clazz c2 = new Clazz();
		Clazz c3 = new Clazz();
		Clazz c4 = new Clazz();
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
				.get(0).getComponents().get(0).getClazzes().add(c1);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
				.get(0).getComponents().get(0).getClazzes().add(c2);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
				.get(0).getComponents().get(0).getClazzes().add(c3);
		landscape.getSystems().get(0).getNodeGroups().get(0).getNodes().get(0).getApplications()
				.get(0).getComponents().get(0).getClazzes().add(c4);
		RanCorr rancorr = new RanCorr();
		rancorr.doRootCauseDetection(landscape);

		RanCorrLandscape rcLandscape = new RanCorrLandscape(landscape);
		assertTrue(rcLandscape.getApplications().size() == 2);

		boolean app1ok = false;
		boolean app2ok = false;
		for (Application application : rcLandscape.getApplications()) {
			String rgb = application.getGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR);
			double rcr = application
					.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);

			if (rgb.equals("188,255,0") && withEpsilon(rcr, -0.369d, 0.001d)) {
				app1ok = true;
			} else if (rgb.equals("255,189,0") && withEpsilon(rcr, 0.630d, 0.001d)) {
				app2ok = true;
			}
		}

		assertTrue(app1ok && app2ok);
	}

	private boolean withEpsilon(double is, double should, double epsilon) {
		return (Math.abs(is - should) - epsilon) <= 0.0d;
	}

}
