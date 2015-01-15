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
	
	//TODO test 
	/*
	 * Idea: Create array with triplets of commu.methodname, commu.target and calledTimes
	 * Iterate through commus and if methodname and target match increase the calls by #calledTimes
	 * Get an array with all calls pressed into triplets
	 * ???
	 * Profit
	 * 
	 * Optional extra idea: Display these triples in a custom dialog/list
	 * If Item is clicked, show only the commus of the triplet
	 */
	//counts the calls of all methods and sums them up
	def static JsArrayMixed getCallingCardinalityForMethods() {
		val application = SceneDrawer::lastViewedApplication
		var JsArrayMixed jsArrayMethodCalls = JsArrayMixed.createArray().cast()
		var methodAlreadyInArray = false
		
		if (application != null) {
			for (commu : application.communications) {
				//iterating +3 because we have triplets
				for (var i = 0; i < jsArrayMethodCalls.length; i += 3) {
					//compare commu to method-names and targets of array
					if(jsArrayMethodCalls.getString(i).equalsIgnoreCase(commu.methodName) && 
						jsArrayMethodCalls.getString(i + 1).equalsIgnoreCase(commu.target.fullQualifiedName)) {
							methodAlreadyInArray = true
							var currentCallValue = jsArrayMethodCalls.getNumber(i + 2)
							for (runtime : commu.traceIdToRuntimeMap.values) {
								jsArrayMethodCalls.set(i + 2, currentCallValue + runtime.calledTimes)
							}
						}
				}
				//push non-existing commu into array
				if(!methodAlreadyInArray) {
					jsArrayMethodCalls.push(commu.methodName)
					jsArrayMethodCalls.push(commu.target.fullQualifiedName)
					var calls = 0
					for (runtime : commu.traceIdToRuntimeMap.values) {
						calls += runtime.calledTimes
					}
					jsArrayMethodCalls.push(calls)				
				}
				methodAlreadyInArray = false
			}
			return jsArrayMethodCalls;
		} else {
			log.info("application is null");
			return null;
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
						methodcommus += runtime.calledTimes
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
