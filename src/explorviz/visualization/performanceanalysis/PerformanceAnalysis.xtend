package explorviz.visualization.performanceanalysis

import explorviz.shared.model.Application
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import java.util.logging.Logger
import com.google.gwt.core.client.JsArrayMixed
import explorviz.shared.model.CommunicationClazz

class PerformanceAnalysis {
	private static final Logger log = Logger.getLogger( "Debug");
	
	def static void openDialog(String applicationName) {
		if (!LandscapeExchangeManager::isStopped()) {
			LandscapeExchangeManager::stopAutomaticExchange(System::currentTimeMillis().toString())
		}

		PerformanceAnalysisJS::showDialog(applicationName)
	}
	
	//shows communications that have a higher response time than the given value
	def static void showOnlyCommunicationsAboveXms(int responseTime) {
		val application = SceneDrawer::lastViewedApplication
		
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
	
	/*
	 * Idea: Create array with triplets of commu.methodname, commu.target and calledTimes
	 * Iterate through commus and if methodname and target match increase the calls by #calledTimes
	 * Get an array with all calls pressed into triplets
	 * 
	 * Optional extra idea: Display these triples in a custom table
	 * If Item is clicked, show only the commus of the triplet
	 */
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
							//method already exists in array
							methodAlreadyInArray = true
							//update calls value
							var currentCallValue = jsArrayMethodCalls.getNumber(i + 2)
							for (runtime : commu.traceIdToRuntimeMap.values) {
								jsArrayMethodCalls.set(i + 2, currentCallValue + runtime.calledTimes)
							}
						}
				}
				//push non-existing commu into array
				if(!methodAlreadyInArray) {
					pushToCallsArray(jsArrayMethodCalls, commu)				
				}
				//reset boolean for next commu
				methodAlreadyInArray = false
			}
			return jsArrayMethodCalls;
		} else {
			log.info("application is null");
			return null;
		}
	}
	

	//The search is a bit tricky, since we can't return java arrays to JSNI
	//method fills a JSarray and returns it
	def static JsArrayMixed searchMethod(String methodName) {
		val application = SceneDrawer::lastViewedApplication
		var JsArrayMixed jsArraySearch = JsArrayMixed.createArray().cast()
		
		if (application != null) {
			for (commu : application.communications) {
				//the array is slightly different to the getCalling... array
				if (commu.methodName.equalsIgnoreCase(methodName)) {
					pushToSearchArray(jsArraySearch, commu)
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
	
	//inserting name of method, name of class and number of calls
	def static JsArrayMixed pushToCallsArray(JsArrayMixed arr, CommunicationClazz cc) {
		arr.push(cc.methodName)
		arr.push(cc.target.fullQualifiedName)
		arr.push(sumUpCalls(cc))
		return arr
	}
	
	//inserting name of calling class, called class and number of calls
	def static JsArrayMixed pushToSearchArray(JsArrayMixed arr,CommunicationClazz cc) {
		arr.push(cc.source.fullQualifiedName);
		arr.push(cc.target.fullQualifiedName);
		arr.push(sumUpCalls(cc))
		return arr
	}
	
	def static int sumUpCalls(CommunicationClazz cc) {
		var calls = 0
		for (runtime : cc.traceIdToRuntimeMap.values) {
			calls += runtime.calledTimes * runtime.requests
		}
		return calls
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
