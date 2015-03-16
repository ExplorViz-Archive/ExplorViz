package explorviz.visualization.performanceanalysis

import com.google.gwt.core.client.JsArrayMixed
import explorviz.shared.model.Application
import explorviz.shared.model.CommunicationClazz
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager

/**
 *
 * @author Daniel Jaehde
 *
 */
class PerformanceAnalysis {
	public static boolean performanceAnalysisMode = false
	
	def static void openDialog(String applicationName) {
		if (!LandscapeExchangeManager::isStopped()) {
			LandscapeExchangeManager::stopAutomaticExchange(System::currentTimeMillis().toString())
		}
		
		performanceAnalysisMode = true

		PerformanceAnalysisJS::showDialog(applicationName)
	}
	
	def static void setPerformanceAnalysisMode(boolean value) {
		performanceAnalysisMode = value
	}

	//shows communications that have a higher response time than the given value
	def static void showOnlyCommunicationsAboveXms(int responseTime) {
		val application = SceneDrawer::lastViewedApplication

		if (application != null) {
			for (commu : application.communications) {
				commu.hidden = true
				for (runtime : commu.traceIdToRuntimeMap.values) {
					if ((toMillis(runtime.getAverageResponseTimeInNanoSec) * runtime.requests * runtime.calledTimes) > responseTime) {
						commu.hidden = false
					}
				}
			}
			refreshView(application)
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
	def static JsArrayMixed pushToSearchArray(JsArrayMixed arr, CommunicationClazz cc) {
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
