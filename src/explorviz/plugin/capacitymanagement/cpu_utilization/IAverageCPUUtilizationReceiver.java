package explorviz.plugin.capacitymanagement.cpu_utilization;

import java.util.Map;

import explorviz.plugin.capacitymanagement.node.repository.Node;

public interface IAverageCPUUtilizationReceiver {

	/**
	 * Calculates cpu utilization average
	 * 
	 * @param averageCPUUtilizations
	 */
	void newCPUUtilizationAverage(Map<Node, Double> averageCPUUtilizations);

}
