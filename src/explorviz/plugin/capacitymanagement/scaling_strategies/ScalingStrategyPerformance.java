package explorviz.plugin.capacitymanagement.scaling_strategies;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorviz.plugin.capacitymanagement.configuration.Configuration;
import explorviz.plugin.capacitymanagement.node.repository.Node;

/**
 * If CPU utilization is too high, start new node, if it is too low shut down
 * newest node
 *
 * @author jgi, dtj
 *
 */
public class ScalingStrategyPerformance implements IScalingStrategy {

	private final IScalingControl scalingControl;
	private final double lowThreshold;
	private final double highThreshold;

	private static final Logger LOG = LoggerFactory.getLogger(ScalingStrategyPerformance.class);

	/**
	 * @param scalingControl
	 *            scaling control that can start/shut down nodes
	 * @param configuration
	 *            configuration file
	 */
	public ScalingStrategyPerformance(final IScalingControl scalingControl,
			final Configuration configuration) {
		this.scalingControl = scalingControl;
		lowThreshold = configuration.getScalingLowCpuThreshold();
		highThreshold = configuration.getScalingHighCpuThreshold();
	}

	@Override
	public void analyze(final Map<Node, Double> averageNodeCPUUtilizations) {
		final double overallAverage = calculateOverallAverage(averageNodeCPUUtilizations);

		final Node firstNode = averageNodeCPUUtilizations.keySet().iterator().next();

		final DecimalFormat doubleFormater = new DecimalFormat("0.000");
		LOG.info("Scalinggroup: " + firstNode.getScalingGroup().getApplicationFolder()
				+ ", active nodes: " + firstNode.getScalingGroup().getActiveNodesCount()
				+ ", average cpu: " + doubleFormater.format(overallAverage));

		if (overallAverage >= highThreshold) {
			scalingControl.startNode(firstNode.getScalingGroup());
		} else if (overallAverage <= lowThreshold) {
			shutdownLowestCPUUtilNode(averageNodeCPUUtilizations);
		}
	}

	private double calculateOverallAverage(final Map<Node, Double> averageCPUUtilizations) {
		double overallAverage = 0.0;
		for (final Double cpuUtil : averageCPUUtilizations.values()) {
			overallAverage += cpuUtil;
		}
		overallAverage = overallAverage / averageCPUUtilizations.size();
		return overallAverage;
	}

	private void shutdownLowestCPUUtilNode(final Map<Node, Double> averageCPUUtilizations) {
		if (averageCPUUtilizations.size() <= 1) {
			return;
		}
		// TODO: das ist ein dummy value
		double minimumCPUUtil = 42;
		Node minimumNode = null;

		for (final Entry<Node, Double> entry : averageCPUUtilizations.entrySet()) {
			if (entry.getValue() < minimumCPUUtil) {
				minimumNode = entry.getKey();
				minimumCPUUtil = entry.getValue();
			}
		}

		scalingControl.shutDownNode(minimumNode);
	}
}
