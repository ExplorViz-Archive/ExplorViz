package explorviz.plugin_server.capacitymanagement.scaling_strategies;

import java.util.*;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroup;
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository;
import explorviz.shared.model.*;
import explorviz.shared.model.System;

/**
 * This class interprets the given applications with its root cause ratings and
 * determines whether they should be shut down or replicated.
 */
public class ScalingStrategyPerformance implements IScalingStrategy {

	/*
	 * (non-Javadoc) Determine if application should be terminated (0),
	 * replicated (1). Double is used to check what action should be executed.
	 * 
	 * @see explorviz.plugin_server.capacitymanagement.scaling_strategies.
	 * IScalingStrategy#analyzeApplications(explorviz.shared.model.Landscape,
	 * java.util.List)
	 */
	@Override
	public Map<Application, Integer> analyzeApplications(final Landscape landscape,
			final List<Application> applicationsToBeAnalyzed, final ScalingGroupRepository scaleRepo) {

		final Map<Application, Integer> planMapApplication = new HashMap<Application, Integer>();

		for (int i = 0; i < applicationsToBeAnalyzed.size(); i++) {
			final Application currentApplication = applicationsToBeAnalyzed.get(i);
			// final TreeMapLongDoubleIValue anomalyScoreFromAppMap =
			// (TreeMapLongDoubleIValue) currentApplication
			// .getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);

			// If anomaly score is negative -> application underload and
			// terminate if this is the last application of its type.
			// If anomaly score is positive -> application overload and
			// should be replicated.
			// double anomalyScoreFromApp = anomalyScoreFromAppMap.get(landscape
			// .getGenericLongData(IPluginKeys.ANOMALY_PRESENT_ON_TIMESTAMP));

			// java.lang.System.out.println("Vergangener AnomalyScore: " +
			// anomalyScoreFromApp);
			double highestAnomalyScoreFromApps = 0;
			for (System system : landscape.getSystems()) {
				for (NodeGroup nodeGroup : system.getNodeGroups()) {
					for (Node node : nodeGroup.getNodes()) {
						for (Application application : node.getApplications()) {
							final TreeMapLongDoubleIValue anomalyScoreFromAppMap = (TreeMapLongDoubleIValue) application
									.getGenericData(IPluginKeys.TIMESTAMP_TO_ANOMALY_SCORE);
							double appScore = anomalyScoreFromAppMap.get(landscape
									.getGenericLongData(IPluginKeys.ANOMALY_PRESENT_ON_TIMESTAMP));
							if (Math.abs(appScore) > Math.abs(highestAnomalyScoreFromApps)) {
								highestAnomalyScoreFromApps = appScore;
							}
						}
					}
				}
			}

			if (highestAnomalyScoreFromApps < 0) {
				// if (!isLast(scaleRepo, currentApplication)) {
				planMapApplication.put(currentApplication, 0);
				// }
			} else {
				planMapApplication.put(currentApplication, 1);
			}
		}
		return planMapApplication;
	}

	/**
	 * Analyzing ScalingGroup of application if the application is the last on
	 * of its type
	 *
	 * @param scaleRepo
	 *            ScalingGroupRepository to work on.
	 * @param currentApplication
	 *            Application to be analyzed.
	 * @return True if the application is the last of its type.
	 */
	private boolean isLast(final ScalingGroupRepository scaleRepo,
			final Application currentApplication) {
		ScalingGroup applicationScalingGroup = scaleRepo.getScalingGroupByName(currentApplication
				.getScalinggroupName());
		if (applicationScalingGroup.getAppCount() > 1) {
			return false;
		}
		return true;
	}
}