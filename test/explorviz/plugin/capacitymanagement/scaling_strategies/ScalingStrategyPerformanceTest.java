package explorviz.plugin.capacitymanagement.scaling_strategies;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.plugin_server.capacitymanagement.scaling_strategies.ScalingStrategyPerformance;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

public class ScalingStrategyPerformanceTest {
	private Landscape landscape;
	private List<Application> applicationListP;
	private List<Application> applicationListN;
	private Map<Application, Integer> planMapApplicationP;
	private Map<Application, Integer> planMapApplicationN;
	private ScalingGroupRepository scaleRepo;

	@Before
	public void before() {
		landscape = TestLandscapeBuilder.createStandardLandscape(0);
		applicationListP = new ArrayList<Application>();
		applicationListN = new ArrayList<Application>();
		planMapApplicationP = new HashMap<Application, Integer>();
		planMapApplicationN = new HashMap<Application, Integer>();
		scaleRepo = new ScalingGroupRepository();
		scaleRepo.addScalingGroup("test", "testFolder", 100);
		ScalingGroup scaleG = scaleRepo.getScalingGroupByName("test");

		for (final System system : landscape.getSystems()) {
			for (final NodeGroup nodeGroup : system.getNodeGroups()) {
				for (final Node node : nodeGroup.getNodes()) {
					for (int i = 0; i < node.getApplications().size(); i++) {
						// manipulate RCRs
						Application currentApplication = node.getApplications().get(i);
						currentApplication.setScalinggroupName("test");
						scaleG.addApplication(currentApplication);
						// generate list with positive values
						currentApplication.putGenericDoubleData(
								IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY, 0.7 - (0.05 * i));
						if ((0.7 - (0.05 * i)) >= 0.6) {
							applicationListP.add(currentApplication);
						}

					}
				}
			}
		}

		for (Application application : applicationListP) {
			planMapApplicationP.put(application, 1);
		}

	}

	@Test
	public void testAnalyzeApplicationsPositiveRCR() {
		ScalingStrategyPerformance ssp = new ScalingStrategyPerformance();
		assertEquals(planMapApplicationP,
				ssp.analyzeApplications(landscape, applicationListP, scaleRepo));

	}

	@Test
	public void testAnalyzeApplicationsNegativeRCR() {
		ScalingStrategyPerformance ssp = new ScalingStrategyPerformance();

		for (final System system : landscape.getSystems()) {
			for (final NodeGroup nodeGroup : system.getNodeGroups()) {
				for (final Node node : nodeGroup.getNodes()) {
					for (int i = 0; i < node.getApplications().size(); i++) {
						// manipulate RCRs
						Application currentApplication = node.getApplications().get(i);
						// generate list with negative values
						currentApplication.putGenericDoubleData(
								IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY, -0.7 + (0.05 * i));
						if ((-0.7 + (0.05 * i)) <= -0.6) {
							applicationListN.add(currentApplication);
						}

					}
				}
			}
		}

		for (Application application : applicationListN) {
			planMapApplicationN.put(application, 0);
		}

		assertEquals(planMapApplicationN,
				ssp.analyzeApplications(landscape, applicationListN, scaleRepo));

	}
}
