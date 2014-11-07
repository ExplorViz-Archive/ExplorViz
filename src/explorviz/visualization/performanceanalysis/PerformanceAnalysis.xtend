package explorviz.visualization.performanceanalysis

import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager

class PerformanceAnalysis {
	def static void openDialog(String applicationName) {
		if (!LandscapeExchangeManager::isStopped()) {
			LandscapeExchangeManager::stopAutomaticExchange(System::currentTimeMillis().toString())
		}

		PerformanceAnalysisJS::showDialog(applicationName)
	}

	def static void showOnlyCommunicationsAbove100msec() {
		val application = SceneDrawer::lastViewedApplication

//		application.communications.hide = true

		if (application != null) {
			SceneDrawer::createObjectsFromApplication(application, true)
		}
	}

	def static void showAllCommunications() {
	}
}
