package explorviz.plugin.capacitymanagement.planning;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_client.capacitymanagement.CapManExecutionStates;
import explorviz.plugin_client.capacitymanagement.CapManStates;
import explorviz.shared.model.*;

public class CapManForTest {

	/*
	 * This class is a copy of the xtend-gen of CapMan without its constructor
	 * due some errors with filepath of the settingsfile of the
	 * capman-configuration Since we only want to test the methods of CapMan and
	 * the setup doesn't matter for the functionality this one is a workaround,
	 * but it works.
	 */

	/**
	 * @author jgi, dtj Find the highest RootCauseRating and return it to be
	 *         able to filter the applications to be analyzed.
	 * @param landscape
	 *            Landscape to work on.
	 */
	public double initializeAndGetHighestRCR(final Landscape landscape) {
		double maxRootCauseRating = 0;
		List<explorviz.shared.model.System> _systems = landscape.getSystems();
		for (final explorviz.shared.model.System system : _systems) {
			List<NodeGroup> _nodeGroups = system.getNodeGroups();
			for (final NodeGroup nodeGroup : _nodeGroups) {
				List<Node> _nodes = nodeGroup.getNodes();
				for (final Node node : _nodes) {
					{
						node.putGenericData(IPluginKeys.CAPMAN_STATE, CapManStates.NONE);
						List<Application> _applications = node.getApplications();
						for (final Application application : _applications) {
							{
								boolean _isGenericDataPresent = application
										.isGenericDataPresent(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);
								if (_isGenericDataPresent) {
									Double rating = application
											.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);
									double _abs = Math.abs((rating).doubleValue());
									double _abs_1 = Math.abs(maxRootCauseRating);
									boolean _greaterThan = (_abs > _abs_1);
									if (_greaterThan) {
										maxRootCauseRating = (rating).doubleValue();
									}
								}
								application.putGenericData(IPluginKeys.CAPMAN_STATE,
										CapManStates.NONE);
								application.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
										CapManExecutionStates.NONE);
							}
						}
					}
				}
			}
		}
		return maxRootCauseRating;
	}

	/**
	 * @author jgi, dtj Collect all the applications that are down to 10% below
	 *         the maximum rating.
	 * @param landscape
	 *            Landscape to work on.
	 * @param rootCauseRating
	 *            RootCauseRating for application given by RootCauseDetection.
	 */
	public List<Application> getApplicationsToBeAnalysed(final Landscape landscape,
			final double rootCauseRating) {
		List<Application> applicationGroup = new ArrayList<Application>();
		List<explorviz.shared.model.System> _systems = landscape.getSystems();
		for (final explorviz.shared.model.System system : _systems) {
			List<NodeGroup> _nodeGroups = system.getNodeGroups();
			for (final NodeGroup nodeGroup : _nodeGroups) {
				List<Node> _nodes = nodeGroup.getNodes();
				for (final Node node : _nodes) {
					List<Application> _applications = node.getApplications();
					for (final Application application : _applications) {
						boolean _isGenericDataPresent = application
								.isGenericDataPresent(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);
						if (_isGenericDataPresent) {
							Double _genericDoubleData = application
									.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY);
							double _abs = Math.abs((_genericDoubleData).doubleValue());
							double _abs_1 = Math.abs(rootCauseRating);
							double _minus = (_abs_1 - 0.1);
							boolean _greaterEqualsThan = (_abs >= _minus);
							if (_greaterEqualsThan) {
								applicationGroup.add(application);
							}
						}
					}
				}
			}
		}
		return applicationGroup;
	}

	public String computePlanId(final int waitTimeForNewPlan, final Landscape landscape,
			final long now, final Integer planId) {
		int newPlanId = (planId).intValue();
		Long _genericLongData = landscape
				.getGenericLongData(IPluginKeys.CAPMAN_TIMESTAMP_LAST_PLAN);
		boolean _lessThan = ((_genericLongData).longValue() < (now - (1000 * waitTimeForNewPlan)));
		if (_lessThan) {
			boolean _isGenericDataPresent = landscape
					.isGenericDataPresent(IPluginKeys.CAPMAN_NEW_PLAN_ID);
			if (_isGenericDataPresent) {
				int _newPlanId = newPlanId;
				newPlanId = (_newPlanId + 1);
			}
			landscape.putGenericLongData(IPluginKeys.CAPMAN_TIMESTAMP_LAST_PLAN, Long.valueOf(now));
		}
		return Integer.valueOf(newPlanId).toString();
	}

}
