package explorviz.plugin.capacitymanagement.planning;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

public class CapManTest {

	private Landscape landscape;
	private double maxRootCauseRating;
	private CapManForTest capMan;
	private List<Application> applicationList;
	private int waitTimeForNewPlan;
	private long now;
	private int planId;

	@Before
	public void before() {

		landscape = TestLandscapeBuilder.createStandardLandscape(0);
		maxRootCauseRating = 0.7;
		capMan = new CapManForTest();
		applicationList = new ArrayList<Application>();
		waitTimeForNewPlan = 600;
		now = java.lang.System.currentTimeMillis();
		planId = 5;

		landscape.putGenericStringData(IPluginKeys.CAPMAN_NEW_PLAN_ID, "5");

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
		assertEquals("Test, if method gets highest RCR correctly", 0.7,
				capMan.initializeAndGetHighestRCR(landscape), 0);
	}

	@Test
	public void testGetApplicationsToBeAnalyzed() {
		assertEquals("Test, if method fetches the correct applications", applicationList,
				capMan.getApplicationsToBeAnalysed(landscape, maxRootCauseRating));
	}

	@Test
	public void testComputePlanId() {
		String localTestPlanId;

		// Testing side effect of new time stamp and computation of new ID
		landscape.putGenericLongData(IPluginKeys.CAPMAN_TIMESTAMP_LAST_PLAN, now - (500 * 1000));
		localTestPlanId = capMan.computePlanId(waitTimeForNewPlan, landscape, now, planId);

		assertEquals("Test, if time stamp stays on old value.", Long.valueOf(now - (500 * 1000)),
				landscape.getGenericLongData(IPluginKeys.CAPMAN_TIMESTAMP_LAST_PLAN));
		assertEquals("Test, if no new ID is given", "5", localTestPlanId);

		landscape.putGenericLongData(IPluginKeys.CAPMAN_TIMESTAMP_LAST_PLAN, now - (700 * 1000));
		localTestPlanId = capMan.computePlanId(waitTimeForNewPlan, landscape, now, planId);

		assertEquals("Test, if time stamp will be updated.", Long.valueOf(now),
				landscape.getGenericLongData(IPluginKeys.CAPMAN_TIMESTAMP_LAST_PLAN));
		assertEquals("Test, if new ID is given", "6", localTestPlanId);
	}
}
