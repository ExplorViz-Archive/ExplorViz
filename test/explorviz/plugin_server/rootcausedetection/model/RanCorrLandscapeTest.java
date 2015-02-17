package explorviz.plugin_server.rootcausedetection.model;

import static org.junit.Assert.*;

import org.junit.Test;

import explorviz.shared.model.*;
import explorviz.shared.model.System;

public class RanCorrLandscapeTest {

	/**
	 * This test checks if ExplorViz-Landscapes are correctly converted to
	 * RanCorrLandscapes.
	 */
	@Test
	public void checkLandscapeConversion() {
		Landscape landscape = TestLandscapeBuilder.createStandardLandscape(0);

		RanCorrLandscape rcLandscape = new RanCorrLandscape(landscape);

		assertTrue(rcLandscape.getApplications().size() == 20);
		assertTrue(rcLandscape.getClasses().size() == 20);
		assertTrue(rcLandscape.getPackages().size() == 40);
		assertTrue(rcLandscape.getOperations().size() == 20);

		for (System system : landscape.getSystems()) {
			for (NodeGroup nodeGroup : system.getNodeGroups()) {
				for (Node node : nodeGroup.getNodes()) {
					for (Application application : node.getApplications()) {
						assertTrue(rcLandscape.getApplications().contains(application));

						for (CommunicationClazz operation : application.getCommunications()) {
							assertTrue(rcLandscape.getOperations().contains(operation));
						}

						for (Component component : application.getComponents()) {
							traverseComponents(component, rcLandscape);
						}
					}
				}
			}
		}
	}

	private void traverseComponents(Component c, RanCorrLandscape lscp) {
		assertTrue(lscp.getPackages().contains(c));

		for (Component subcomponent : c.getChildren()) {
			traverseComponents(subcomponent, lscp);
		}
	}

}
