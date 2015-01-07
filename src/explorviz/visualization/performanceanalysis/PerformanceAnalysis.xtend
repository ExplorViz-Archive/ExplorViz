package explorviz.visualization.performanceanalysis

import explorviz.shared.model.Application
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import java.util.logging.Logger

class PerformanceAnalysis {
	private static final Logger log = Logger.getLogger( "Debug");
	
	def static void openDialog(String applicationName) {
		if (!LandscapeExchangeManager::isStopped()) {
			LandscapeExchangeManager::stopAutomaticExchange(System::currentTimeMillis().toString())
		}

		PerformanceAnalysisJS::showDialog(applicationName)
	}
	
	def static int getCallingCardinalityForMethods() {
		val application = SceneDrawer::lastViewedApplication
		
		if (application != null) {
			var allcommus = 0;
			for (commu : application.communications) {
				var componentcommus = 0;
				for (runtime : commu.traceIdToRuntimeMap.values) {
					allcommus = allcommus + runtime.calledTimes;
					componentcommus = componentcommus + runtime.calledTimes;
				}
				log.info(commu.methodName+" has "+componentcommus+" calls");
			}
			return allcommus;
		} else {
			log.info("application is null");
			return 0;
		}
	}
	
	def static void showOnlyCommunicationsAboveXms(int responseTime) {
		val application = SceneDrawer::lastViewedApplication
		log.info("Given response time: "+responseTime.toString());
		if (application != null) {
			for (commu : application.communications) {
				commu.hidden = true
				for (runtime : commu.traceIdToRuntimeMap.values) {
					if (toMillis(runtime.getAverageResponseTimeInNanoSec) > responseTime) {
						commu.hidden = false
					}
				}
			}
			refreshView(application)
		}
	}
	
	
	def static float toMillis(float f) {
		f / (1000 * 1000)
	}

	def static refreshView(Application application) {
		if (application != null)
			SceneDrawer::createObjectsFromApplication(application, true)
	}

	def static void showAllCommunications() {
		val application = SceneDrawer::lastViewedApplication
		
		reset(application)
		refreshView(application)
	}

	def static reset(Application application) {
		if (application != null) {
			for (commu : application.communications) {
				commu.hidden = false
			}
		}
	}
}
