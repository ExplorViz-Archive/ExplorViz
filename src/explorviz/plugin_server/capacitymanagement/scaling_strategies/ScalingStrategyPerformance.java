package explorviz.plugin_server.capacitymanagement.scaling_strategies;

import java.util.*;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration;
import explorviz.shared.model.*;

/**
 * If CPU utilization is too high, start new node, if it is too low shut down
 * newest node
 *
 * @author jgi, dtj
 *
 */
public class ScalingStrategyPerformance implements IScalingStrategy {

	// private final double lowThreshold;
	// private final double highThreshold;
	// private final double cpuBound;
	// Map containing Nodes to be handled
	// true = start Node [HighThreshold], false = terminate Node [LowThreshold]
	// private final Map<Node, Boolean> planMap = new HashMap<Node, Boolean>();
	// private static final Logger LOG =
	// LoggerFactory.getLogger(ScalingStrategyPerformance.class);

	/**
	 * @param scalingControl
	 *            scaling control that can start/shut down nodes
	 * @param configuration
	 *            configuration file
	 */
	public ScalingStrategyPerformance(final CapManConfiguration configuration) {
		// lowThreshold = configuration.getScalingLowCpuThreshold();
		// highThreshold = configuration.getScalingHighCpuThreshold();
		// cpuBound = configuration.getCpuBoundForApplications();

	}

	public ScalingStrategyPerformance() {
	}

	// @Override
	/*
	 * public Map<Node, Boolean> analyze(final Map<Node, Double>
	 * averageNodeCPUUtilizations) { final double overallAverage =
	 * calculateOverallAverage(averageNodeCPUUtilizations);
	 * 
	 * final Node firstNode =
	 * averageNodeCPUUtilizations.keySet().iterator().next();
	 * 
	 * final DecimalFormat doubleFormater = new DecimalFormat("0.000");
	 * LOG.info("Nodegroup: " + CapManUtil.getApplicationNames(firstNode) +
	 * ", active nodes: " + firstNode.getParent().getNodeCount() +
	 * ", average cpu: " + doubleFormater.format(overallAverage));
	 * 
	 * if (overallAverage >= highThreshold) { planMap.put(firstNode, true); }
	 * else if (overallAverage <= lowThreshold) {
	 * 
	 * shutdownLowestCPUUtilNode(averageNodeCPUUtilizations); } return planMap;
	 * }
	 */

	// Determine if application should be terminated (0), replicated (1)
	// Double is used to check what action should be executed.
	@Override
	public Map<Application, Integer> analyzeApplications(final Landscape landscape,
			final List<Application> applicationsToBeAnalyzed) {

		final Map<Application, Integer> planMapApplication = new HashMap<Application, Integer>();

		for (int i = 0; i < applicationsToBeAnalyzed.size(); i++) {
			final Application currentApplication = applicationsToBeAnalyzed.get(i);
			final Double rootCauseRating = currentApplication
					.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);

			// if root cause rating is negative -> application underload and
			// terminate if this is the last application of its type
			// if root cause rating is positive -> application overload and
			// should be replicated
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

	/*
	 * private double calculateOverallAverage(final Map<Node, Double>
	 * averageCPUUtilizations) { double overallAverage = 0.0; for (final Double
	 * cpuUtil : averageCPUUtilizations.values()) { overallAverage += cpuUtil; }
	 * overallAverage = overallAverage / averageCPUUtilizations.size(); return
	 * overallAverage; }
	 * 
	 * // TODO Analyse if the node is last node of its type? private void
	 * shutdownLowestCPUUtilNode(final Map<Node, Double> averageCPUUtilizations)
	 * { if (averageCPUUtilizations.size() <= 1) { return; } double
	 * minimumCPUUtil = 42; Node minimumNode = null;
	 * 
	 * for (final Entry<Node, Double> entry : averageCPUUtilizations.entrySet())
	 * { if (entry.getValue() < minimumCPUUtil) { minimumNode = entry.getKey();
	 * minimumCPUUtil = entry.getValue(); }
	 * 
	 * }
	 * 
	 * planMap.put(minimumNode, false); }
	 */

	// analysing nodegroup of application for other applications with the same
	// name
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
