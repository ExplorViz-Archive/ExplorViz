package explorviz.plugin_server.rootcausedetection

import explorviz.plugin_server.interfaces.IRootCauseDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape

/**
 * This class implements a RootCauseDetector based on the implementation of RanCorr.
 */
class RanCorr implements IRootCauseDetector {

	/**
	 * Creates a new Root Cause Detector and registers it.
	 */
	new() {
		PluginManagerServerSide::registerAsRootCauseDetector(this)
	}

	/**
	 * Calculates RootCauseRatings for a specified landscape.
	 * 
	 * @param landscape specified landscape
	 */
	override doRootCauseDetection(Landscape landscape) {
		try {
			val ranCorrLandscape = new RanCorrLandscape(landscape)

			ranCorrLandscape.calculateRootCauseRatings(RanCorrConfiguration.ranCorrAlgorithm,
				RanCorrConfiguration.ranCorrAggregationAlgorithm)
			ranCorrLandscape.persistRootCauseRatings(RanCorrConfiguration.ranCorrPersistAlgorithm)
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
