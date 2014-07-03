package explorviz.visualization.interaction

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.helper.CommunicationAppAccumulator
import java.util.HashSet
import java.util.Set
import explorviz.shared.model.CommunicationClazz
import java.util.List
import java.util.ArrayList

class TraceHighlighter {
	def static void openTraceChooser(CommunicationAppAccumulator communication) {
		var selectOptions = ""

		for (child : communication.aggregatedCommunications) {
			for (entry : child.traceIdToRuntimeMap.entrySet) {
				val isSelected = if (selectOptions == "") "selected"
				val clazz = if (!child.methodName.startsWith("new ")) child.target.name + "." else ""

				val name = SafeHtmlUtils::htmlEscape(clazz + child.methodName + "(..)")

				val filteredTraceElements = getFilteredTraceElements(entry.key, communication)
				val startClass = seekStartClass(filteredTraceElements)
				val startClassSafe = SafeHtmlUtils::htmlEscape(startClass)
				val overallDuration = getOverallDuration(filteredTraceElements, startClass, entry.key)
				val overallRequests = getOverallRequests(filteredTraceElements, entry.key)

				selectOptions += "<option " + isSelected + " value='" + entry.key + "'>" + name +
					" (Overall requests: " + overallRequests + ", Id: " + entry.key + ", Overall duration: " +
					overallDuration + " ms, Starts at: " + startClassSafe + ")</option>"
			}
		}

		TraceHighlighterJS.openDialog(selectOptions)
	}

	def static float getOverallRequests(List<CommunicationClazz> traceElements, Long traceId) {
		var requests = 0
		
		for (traceElement : traceElements) {
			requests += traceElement.traceIdToRuntimeMap.get(traceId).requests
		}
		
		return requests
	}
	
	def static float getOverallDuration(List<CommunicationClazz> traceElements, String startClass, Long traceId) {
		for (traceElement : traceElements) {
			if (traceElement.source.fullQualifiedName == startClass) {
				return traceElement.traceIdToRuntimeMap.get(traceId).averageResponseTime / 1000f // TODO is this ms?
			}
		}
		
		return -1
	}

	def static List<CommunicationClazz> getFilteredTraceElements(Long traceId,
		CommunicationAppAccumulator commuAggregated) {
		var Application application = null

		if (commuAggregated.source instanceof Clazz) {
			val clazz = commuAggregated.source as Clazz
			application = clazz.parent.belongingApplication
		} else if (commuAggregated.source instanceof Component) {
			val compo = commuAggregated.source as Component
			application = compo.belongingApplication
		}

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

	def static String seekStartClass(List<CommunicationClazz> filteredTraceElements) {
		val Set<String> startCandidates = new HashSet<String>

		for (communication : filteredTraceElements) {
			startCandidates.add(communication.source.fullQualifiedName)
		}

		for (communication : filteredTraceElements) {
			if (communication.source != communication.target) {
				startCandidates.remove(communication.target.fullQualifiedName)
			}
		}

		if (startCandidates.empty) {
			"UNKNOWN"
		} else {
			for (candi : startCandidates) {
				return candi
			}
		}
	}

	def static void choosenOneTrace(String choosenTraceId) {
		val traceId = Long.parseLong(choosenTraceId)
	}
}
