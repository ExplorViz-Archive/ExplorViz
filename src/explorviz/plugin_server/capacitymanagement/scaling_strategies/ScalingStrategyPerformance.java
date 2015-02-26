package explorviz.plugin_server.capacitymanagement.scaling_strategies;

import java.util.*;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.shared.model.*;

/**
 * @author jgi, dtj If CPU utilization is too high, start new node, if it is too
 *         low shut down newest node.
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
			final List<Application> applicationsToBeAnalyzed) {

		final Map<Application, Integer> planMapApplication = new HashMap<Application, Integer>();

		for (int i = 0; i < applicationsToBeAnalyzed.size(); i++) {
			final Application currentApplication = applicationsToBeAnalyzed.get(i);
			final Double rootCauseRating = currentApplication
					.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);

			// If root cause rating is negative -> application underload and
			// terminate if this is the last application of its type.
			// If root cause rating is positive -> application overload and
			// should be replicated.
			if (rootCauseRating < 0) {
				if (!isLast(landscape, currentApplication)) {
					planMapApplication.put(currentApplication, 0);
				}
			} else {
				planMapApplication.put(currentApplication, 1);
			}
		}
		return planMapApplication;
	}

	/**
	 * @author jgi, dtj Analyzing nodegroup of application for other
	 *         applications with the same name.
	 * @param landscape
	 *            Landscape to work on.
	 * @param currentApplication
	 *            Application to be analyzed.
	 * @return True if the application is the last of its type.
	 */
	private boolean isLast(final Landscape landscape, final Application currentApplication) {
		for (final Node node : currentApplication.getParent().getParent().getNodes()) {
			for (final Application application : node.getApplications()) {
				if (application.getName().equalsIgnoreCase(currentApplication.getName())) {
					return false;
				}
			}
		}
		return true;
	}
}