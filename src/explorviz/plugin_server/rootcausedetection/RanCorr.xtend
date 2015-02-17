package explorviz.plugin_server.rootcausedetection

import explorviz.plugin_server.interfaces.IRootCauseDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape

class RanCorr implements IRootCauseDetector {

	new() {
		PluginManagerServerSide::registerAsRootCauseDetector(this)
	}

	override doRootCauseDetection(Landscape landscape) {
		val ranCorrLandscape = new RanCorrLandscape(landscape)

		ranCorrLandscape.calculateRootCauseRatings(RanCorrConfiguration.ranCorrAlgorithm,
			RanCorrConfiguration.ranCorrAggregationAlgorithm)
		ranCorrLandscape.persistRootCauseRatings(RanCorrConfiguration.ranCorrPersistAlgorithm)
	}
}
