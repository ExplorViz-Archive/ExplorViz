package explorviz.plugin_server.capacitymanagement.scaling_strategies;

import java.util.List;
import java.util.Map;

import explorviz.shared.model.Application;
import explorviz.shared.model.Landscape;

public interface IScalingStrategy {
	/**
	 * Gets nodes and their utilizations values and analyzes them
	 *
	 * @param averageCPUUtilizations
	 *            Map of nodes with their CPU utilization values
	 * @return
	 */
	// public Map<Node, Boolean> analyze(Map<Node, Double>
	// averageCPUUtilizations);

	/**
	 * analyses application
	 *
	 * @param applicationsToBeAnalyzed
	 *            applicationsToBeAnalyzed
	 * @return
	 */
	public Map<Application, Integer> analyzeApplications(final Landscape landscape,
			final List<Application> applicationsToBeAnalyzed);

}
