package explorviz.visualization.interaction

import explorviz.shared.model.helper.CommunicationAppAccumulator

class TraceHighlighter {
	def static void openTraceChooser(CommunicationAppAccumulator communication) {
		TraceHighlighterJS.openDialog
	}
}