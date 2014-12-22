package explorviz.plugin.capacitymanagement.scaling_strategies;

import java.util.Map;

import explorviz.shared.model.Node;

public interface IScalingStrategy {
	/**
	 * Gets nodes and their utilizations values and analyzes them
	 *
	 * @param averageCPUUtilizations
	 *            Map of nodes with their CPU utilization values
	 */
	public void analyze(Map<Node, Double> averageCPUUtilizations);

}
