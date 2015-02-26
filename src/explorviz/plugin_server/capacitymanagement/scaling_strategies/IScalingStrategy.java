package explorviz.plugin_server.capacitymanagement.scaling_strategies;

import java.util.List;
import java.util.Map;

import explorviz.shared.model.Application;
import explorviz.shared.model.Landscape;

public interface IScalingStrategy {

	/**
	 * @author jgi, dtj Analyzes application.
	 * @param applicationsToBeAnalyzed
	 *            Applications to be analyzed.
	 * @return Analyzed map of applications with the information if the
	 *         application should be terminated or replicated in it.
	 */
	public Map<Application, Integer> analyzeApplications(final Landscape landscape,
			final List<Application> applicationsToBeAnalyzed);
}
