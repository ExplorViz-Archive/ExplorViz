package explorviz.plugin.capacitymanagement.scaling_strategies;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.plugin_server.capacitymanagement.scaling_strategies.ScalingStrategyPerformance;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

public class ScalingStrategyPerformanceTest {
	private Landscape landscape;
	private List<Application> applicationList;
	private Map<Application, Integer> planMapApplication;
	private ScalingGroupRepository scaleRepo;

	@Before
	public void before() {
		landscape = TestLandscapeBuilder.createStandardLandscape(0);
		applicationList = new ArrayList<Application>();
		planMapApplication = new HashMap<Application, Integer>();
		scaleRepo = new ScalingGroupRepository();

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
		for (Application application : applicationList) {
			planMapApplication.put(application, 1);
		}
	}

	@Test
	public void testAnalyzeApplications() {
		ScalingStrategyPerformance ssp = new ScalingStrategyPerformance();
		assertEquals(planMapApplication,
				ssp.analyzeApplications(landscape, applicationList, scaleRepo));

	}
}
