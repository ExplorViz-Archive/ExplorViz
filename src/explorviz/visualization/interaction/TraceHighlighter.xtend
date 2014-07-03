package explorviz.visualization.interaction

import explorviz.shared.model.helper.CommunicationAppAccumulator
import com.google.gwt.safehtml.shared.SafeHtmlUtils

class TraceHighlighter {
	def static void openTraceChooser(CommunicationAppAccumulator communication) {
		var selectOptions = ""

		for (child : communication.aggregatedCommunications) {
			for (traceId : child.traceIds) {
				val isSelected = if (selectOptions == "") "selected"
				val clazz = if (!child.methodName.startsWith("new ")) child.target.name + "." else ""
				
				val name = SafeHtmlUtils::htmlEscape(clazz + child.methodName + "(..)")
				val startClass = SafeHtmlUtils::htmlEscape("TODO")

				selectOptions += "<option " + isSelected + " value='" + traceId + "'>" + name +
					" (Current CallsEvents: " + child.requests + ", Id: " + traceId + ", Overall duration: " + -1 +
					" ms, Starts at: " + startClass + ")</option>"
			}
		}

		TraceHighlighterJS.openDialog(selectOptions)
	}

	def static void choosenOneTrace(String choosenTraceId) {
		val traceId = Long.parseLong(choosenTraceId)
	}
}
