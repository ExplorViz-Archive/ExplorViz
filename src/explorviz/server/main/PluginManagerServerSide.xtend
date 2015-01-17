package explorviz.server.main

import explorviz.plugin_server.interfaces.IAnomalyDetector
import explorviz.plugin_server.interfaces.ICapacityManager
import explorviz.plugin_server.interfaces.IRootCauseDetector
import explorviz.shared.model.Landscape
import java.util.ArrayList
import java.util.List

class PluginManagerServerSide {
	private static List<IAnomalyDetector> anomalyDetectors = new ArrayList<IAnomalyDetector>()
	private static List<IRootCauseDetector> rootCauseDetectors = new ArrayList<IRootCauseDetector>()
	private static List<ICapacityManager> capacityManagers = new ArrayList<ICapacityManager>()

	def static void registerAsAnomalyDetector(IAnomalyDetector plugin) {
		anomalyDetectors.add(plugin)
	}

	def static void registerAsRootCauseDetector(IRootCauseDetector plugin) {
		rootCauseDetectors.add(plugin)
	}

	def static void registerAsCapacityManager(ICapacityManager plugin) {
		capacityManagers.add(plugin)
	}

	def static void landscapeModelBeforePublish(Landscape landscape) {
		for (anomalyDetector : anomalyDetectors) {
			anomalyDetector.doAnomalyDetection(landscape)
		}

		for (rootCauseDetector : rootCauseDetectors) {
			rootCauseDetector.doRootCauseDetection(landscape)
		}

		for (capacityManager : capacityManagers) {
			capacityManager.doCapacityManagement(landscape)
		}
	}
	
	def static void receivedFinalCapacityAdaptationPlan(Landscape landscape) {
		for (capacityManager : capacityManagers) {
			capacityManager.receivedFinalCapacityAdaptationPlan(landscape)
		}
	}
}
