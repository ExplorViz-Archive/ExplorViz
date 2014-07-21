package explorviz.visualization.highlighting

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.visualization.engine.main.SceneDrawer
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set
import explorviz.shared.model.helper.EdgeState

class TraceHighlighter {
	static var Application application
	static var Long traceId = null

	def static void openTraceChooser(CommunicationAppAccumulator communication) {
		if (communication.requests == 0) {
			return
		}

		application = null

		if (communication.source instanceof Clazz) {
			val clazz = communication.source as Clazz
			application = clazz.parent.belongingApplication
		} else if (communication.source instanceof Component) {
			val compo = communication.source as Component
			application = compo.belongingApplication
		}

		var tableContent = "<thead><tr><th style='text-align: center !important;'>Path ID</th><th style='text-align: center !important;'>Overall Length</th><th style='text-align: center !important;'>Called Times</th><th style='text-align: center !important;'>Avg. Overall Duration in ms</th><th style='text-align: center !important;'>Starts at Class</th><th></th></tr></thead><tbody>"

		var Set<Long> alreadySeenTraces = new HashSet<Long>
		for (child : communication.aggregatedCommunications) {
			for (entry : child.traceIdToRuntimeMap.entrySet) {
				if (!alreadySeenTraces.contains(entry.key)) {
					val filteredTraceElements = getFilteredTraceElements(entry.key, communication)
					val startClass = seekStartClass(filteredTraceElements, entry.key)
					val startClassSafe = SafeHtmlUtils::htmlEscape(startClass)
					val calledTimes = filteredTraceElements.get(0).traceIdToRuntimeMap.get(entry.key).calledTimes
					val overallDuration = getOverallDuration(filteredTraceElements, startClass, entry.key)
					val overallRequests = getOverallRequests(filteredTraceElements, entry.key)

					val chooseButton = '<button id="choose-trace-button' + entry.key + '" type="button"
		class="btn btn-default btn-sm choose-trace-button" traceId="' + entry.key + '">
		<span class="glyphicon glyphicon-chevron-right"></span> Choose
	</button>'

					tableContent += "<tr><td align='right'>" + entry.key + "</td><td align='right'>" + overallRequests +
						"</td><td align='right'>" + calledTimes + "</td><td align='right'>" + overallDuration +
						"</td><td>" + startClassSafe + "</td><td>" + chooseButton + "</td></tr>"
					alreadySeenTraces.add(entry.key)
				}
			}
		}

		alreadySeenTraces.clear()

		TraceHighlighterJS.openDialog(tableContent + "</tbody>")
	}

	private def static List<CommunicationClazz> getFilteredTraceElements(Long traceId,
		CommunicationAppAccumulator commuAggregated) {
		val result = new ArrayList<CommunicationClazz>
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
				return convertToMilliSecondTime(runtime.overallTraceDuration)
			}
		}

		"?"
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

	protected def static void choosenOneTrace(String choosenTraceId) {
		traceId = Long.parseLong(choosenTraceId)

		NodeHighlighter::reset()
		TraceReplayer::replayInit(traceId, application)

		SceneDrawer::createObjectsFromApplication(application, true)
	}

	public def static void reset(boolean withObjectCreation) {
		traceId = null
		TraceReplayer::reset()

		if (application != null && withObjectCreation) {
			SceneDrawer::createObjectsFromApplication(application, true)
		}
	}

	public def static void applyHighlighting(Application applicationParam) {
		if (traceId != null) {
			applicationParam.communicationsAccumulated.forEach [
				var found = seekCommuWithTraceId(it)
				if (found) {
					it.state = EdgeState.SHOW_DIRECTION_OUT
				} else {
					it.state = EdgeState.TRANSPARENT
				}
			]
		}
	}

	private def static boolean seekCommuWithTraceId(CommunicationAppAccumulator commu) {
		for (aggCommu : commu.aggregatedCommunications) {
			if (aggCommu.traceIdToRuntimeMap.get(traceId) != null) {
				return true
			}
		}
		return false
	}
}
