package explorviz.visualization.performanceanalysis

import explorviz.shared.model.Application
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import java.util.logging.Logger
import com.google.gwt.core.client.JsArrayMixed

class PerformanceAnalysis {
	private static final Logger log = Logger.getLogger( "Debug");
	
	def static void openDialog(String applicationName) {
		if (!LandscapeExchangeManager::isStopped()) {
			LandscapeExchangeManager::stopAutomaticExchange(System::currentTimeMillis().toString())
		}

		PerformanceAnalysisJS::showDialog(applicationName)
	}
	
	def static void showOnlyCommunicationsAboveXms(int responseTime) {
		val application = SceneDrawer::lastViewedApplication
		log.info("Given response time: "+responseTime.toString())
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
	
	//TODO 
	/*
	 * Idea: Create array with triplets of commu.methodname, commu.target and calledTimes
	 * Iterate through commus and if methodname and target match increase the calls by #calledTimes
	 * Get an array with all calls pressed into triplets
	 * ???
	 * Profit
	 * 
	 * Optional idea: Display these triples in a custom dialog/list
	 * If Item is clicked, show only the commus of the triplet
	 */
	//counts the calls of all methods and sums them up
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
	

	//this is a bit tricky, since we can't return java arrays to JSNI
	//method fills a JSarray and returns it
	def static JsArrayMixed searchMethod(String methodName) {
		val application = SceneDrawer::lastViewedApplication
		var JsArrayMixed jsArraySearch = JsArrayMixed.createArray().cast()
		
		if (application != null) {
			for (commu : application.communications) {
				var methodcommus = 0
				if (commu.methodName == methodName) {
					jsArraySearch.push(commu.source.fullQualifiedName);
					jsArraySearch.push(commu.target.fullQualifiedName);
					for (runtime : commu.traceIdToRuntimeMap.values) {
						methodcommus = methodcommus + runtime.calledTimes
					}
					jsArraySearch.push(methodcommus)
				} else {
					commu.hidden = true
				}			
			}
			//display all commus if no method is found
			if (!(jsArraySearch.length > 0)) {
				reset(application)
			}
			refreshView(application)
			return jsArraySearch;
		} else {
			log.info("application is null")
			return null
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
