package explorviz.plugin.capacitymanagement.planning;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_server.capacitymanagement.CapMan;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

public class CapManTest {

	private Landscape landscape;
	private double maxRootCauseRating;
	private CapMan capMan;
	private List<Application> applicationList;

	@Before
	public void before() {
		landscape = TestLandscapeBuilder.createStandardLandscape(0);
		maxRootCauseRating = 0.7;
		capMan = new CapMan("test");
		applicationList = new ArrayList<Application>();

		for (final System system : landscape.getSystems()) {
			for (final NodeGroup nodeGroup : system.getNodeGroups()) {
				for (final Node node : nodeGroup.getNodes()) {
					for (int i = 0; i < node.getApplications().size(); i++) {
						// manipulate RCRs
						Application currentApplication = node.getApplications().get(i);
						currentApplication.putGenericDoubleData(
								IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY, 0.7 - (0.05 * i));
						if ((0.7 - (0.05 * i)) >= 0.6) {
							applicationList.add(currentApplication);
						}
					}
				}
			}
		}
	}

	@Test
	public void testInitializeAndGetHighestRCR() {
		assertEquals(0.7, capMan.initializeAndGetHighestRCR(landscape), 0);
	}

	@Test
	public void testGetApplicationsToBeAnalyzed() {
		assertEquals(applicationList,
				capMan.getApplicationsToBeAnalysed(landscape, maxRootCauseRating));
	}

}
