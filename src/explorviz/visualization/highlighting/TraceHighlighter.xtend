package explorviz.visualization.highlighting

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import explorviz.shared.model.Application
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.RuntimeInformation
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.interaction.Usertracking
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import java.util.Collection

class TraceHighlighter {
	static var Long traceId = null

	def static void openTraceChooser(CommunicationAppAccumulator communication) {
		if (communication.requests == 0) {
			return
		}

		if (!LandscapeExchangeManager::isStopped()) {
			LandscapeExchangeManager::stopAutomaticExchange(System::currentTimeMillis().toString())
		}

		var tableContent = "<thead><tr><th style='text-align: center !important;'>Path ID</th><th style='text-align: center !important;'>Method</th><th style='text-align: center !important;'>First Trace Position</th><th style='text-align: center !important;'>Called Times</th><th style='text-align: center !important;'>Avg. Method Duration in ms</th><th style='text-align: center !important;'>Overall Length</th><th style='text-align: center !important;'>Avg. Overall Duration in ms</th><th style='text-align: center !important;'>Starts at Class</th><th></th></tr></thead><tbody>"

		for (child : communication.aggregatedCommunications) {
			var Set<String> alreadySeenMethodes = new HashSet<String>

			val methodNameSafe = SafeHtmlUtils::htmlEscape(
				if (child.methodName.startsWith("new "))
					child.methodName + "(..)"
				else
					child.target.name + "." + child.methodName + "(..)")

			if (!alreadySeenMethodes.contains(methodNameSafe)) {
				val methodDuration = getMethodDuration(child.traceIdToRuntimeMap.values)
				for (entry : child.traceIdToRuntimeMap.entrySet) {

					val filteredTraceElements = getFilteredTraceElements(entry.key, communication)
					val startClass = seekStartClass(filteredTraceElements, entry.key)
					val startClassSafe = SafeHtmlUtils::htmlEscape(startClass)

					val position = getFirstOrderIndex(entry)

					val calledTimes = filteredTraceElements.get(0).traceIdToRuntimeMap.get(entry.key).calledTimes
					val overallDuration = getOverallDuration(filteredTraceElements, startClass, entry.key)
					val overallRequests = getOverallRequests(filteredTraceElements, entry.key)

					val chooseButton = '<button id="choose-trace-button' + entry.key + '" type="button"
		class="btn btn-default btn-sm choose-trace-button" traceId="' + entry.key + '" orderId="' + position + '">
		<span class="glyphicon glyphicon-chevron-right"></span> Choose
	</button>'

					tableContent += "<tr><td align='right'>" + entry.key + "</td><td align='left'>" + methodNameSafe +
						"</td><td align='right'>" + position + "</td><td align='right'>" + calledTimes +
						"</td><td align='right'>" + methodDuration + "</td><td align='right'>" + overallRequests +
						"</td><td align='right'>" + overallDuration + "</td><td>" + startClassSafe + "</td><td>" +
						chooseButton + "</td></tr>"

					alreadySeenMethodes.add(methodNameSafe)
				}
			}
		}

		var tutorial = Experiment::tutorial && Experiment::getStep().choosetrace
		TraceHighlighterJS.openDialog(tableContent + "</tbody>", tutorial)
	}

	private def static int getFirstOrderIndex(Map.Entry<Long, RuntimeInformation> entry) {
		var firstOrderIndex = -1
		for (orderIndex : entry.value.orderIndexes) {
			if (firstOrderIndex == -1) {
				return orderIndex
			}
		}
		firstOrderIndex
	}

	private def static List<CommunicationClazz> getFilteredTraceElements(Long traceId,
		CommunicationAppAccumulator commuAggregated) {
		val result = new ArrayList<CommunicationClazz>
		val application = SceneDrawer::lastViewedApplication
		if (application == null) {
			return result
		}

		for (communication : application.communications) {
			val runtime = communication.traceIdToRuntimeMap.get(traceId)
			if (runtime != null) {
				result.add(communication)
			}
		}

		result
	}

	private def static String seekStartClass(List<CommunicationClazz> filteredTraceElements, Long traceId) {
		for (communication : filteredTraceElements) {
			if (communication.traceIdToRuntimeMap.get(traceId).orderIndexes.contains(1)) {
				return communication.source.fullQualifiedName
			}
		}

		"UNKNOWN"
	}

	private def static String getOverallDuration(List<CommunicationClazz> filteredTraceElements, String startClass,
		Long traceId) {
		for (communication : filteredTraceElements) {
			val runtime = communication.traceIdToRuntimeMap.get(traceId)
			if (runtime.orderIndexes.contains(1)) {
				return convertToMilliSecondTime(runtime.getOverallTraceDurationInNanoSec)
			}
		}

		"?"
	}

	private def static String getMethodDuration(Collection<RuntimeInformation> runtimes) {
		if (runtimes.empty) {
			return "?"
		}

		var accum = 0f
		for (runtime : runtimes) {
			accum += runtime.getAverageResponseTimeInNanoSec
		}

		convertToMilliSecondTime(accum / runtimes.size)
	}

	private def static String convertToMilliSecondTime(float x) {
		val result = (x / (1000 * 1000)).toString()

		result.substring(0, Math.min(result.indexOf('.') + 3, result.length - 1))
	}

	private def static float getOverallRequests(List<CommunicationClazz> traceElements, Long traceId) {
		var requests = 0

		for (traceElement : traceElements) {
			requests += traceElement.traceIdToRuntimeMap.get(traceId).requests
		}

		return requests
	}

	protected def static void choosenOneTrace(String choosenTraceId, String choosenOrderId) {
		if (Experiment::tutorial && Experiment.getStep.choosetrace) {
			Experiment.incStep()
		}
		traceId = Long.parseLong(choosenTraceId)
		val orderId = Integer.parseInt(choosenOrderId)

		NodeHighlighter::reset()
		Usertracking::trackComponentOpenAll()
		val application = SceneDrawer::lastViewedApplication
		application.openAllComponents()
		SceneDrawer::createObjectsFromApplication(application, true)

		TraceReplayer::replayInit(traceId, orderId)
	}

	public def static void reset(boolean withObjectCreation) {
		traceId = null
		TraceReplayer::reset()
		val application = SceneDrawer::lastViewedApplication
		if (application != null && withObjectCreation) {
			SceneDrawer::createObjectsFromApplication(application, true)
		}
	}

	public def static void applyHighlighting(Application applicationParam) {
		if (traceId != null) {
			applicationParam.communicationsAccumulated.forEach [
				var foundAtLeastOne = false
				var requestsForCommu = 0
				for (aggCommu : it.aggregatedCommunications) {
					val runtime = aggCommu.traceIdToRuntimeMap.get(traceId)
					if (runtime != null) {
						foundAtLeastOne = true
						requestsForCommu += aggCommu.requests
						if (runtime.orderIndexes.contains(TraceReplayer::currentIndex)) {
							it.state = EdgeState.REPLAY_HIGHLIGHT
						} else {
							if (it.state != EdgeState.REPLAY_HIGHLIGHT)
								it.state = EdgeState.SHOW_DIRECTION_OUT
						}
					}
				}
				if (!foundAtLeastOne) {
					it.state = EdgeState.HIDDEN
				} else {
					it.requests = requestsForCommu
				}
			]
			ApplicationLayoutInterface::calculatePipeSizeFromQuantiles(applicationParam)
		}
	}

	def static isCurrentlyHighlighting() {
		traceId != null
	}

}
