package explorviz.plugin_server.anomalydetection

import explorviz.plugin_server.interfaces.IAnomalyDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape

class OPADx implements IAnomalyDetector {

	new() {
		PluginManagerServerSide::registerAsAnomalyDetector(this)
	}

	override doAnomalyDetection(Landscape landscape) {
		val annotator = new AnnotateTimeSeriesAndAnomalyScore();
		annotator.doAnomalyDetection(landscape);
	}

}
