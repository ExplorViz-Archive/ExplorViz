package explorviz.server.export

import explorviz.live_trace_processing.record.trace.Trace
import explorviz.live_trace_processing.record.event.AbstractBeforeEventRecord

class RigiStandardFormatExporter {
	val static String HEADER = '"LevelSeparator" "."
"ElmType" "0" "Class"
"ElmType" "1" "Package"
"RelType" "0" "CallDynamicChronologic" "int:Increment" "sig:Signature" "string:Method"'
	val static int hierarchyDepth = 1
	val static int hierarchyElements = 1
	val static int parentChildRelations = 1
	val static int signatures = 1
	val static int relations = 1
	val static int root = 0
	
	val static hierarchy = new TreeNode("root")

	private def static String constructFullHeader() {
		HEADER + attributeToRSF("HierarchyDepth", hierarchyDepth) +
			attributeToRSF("HierarchyElements", hierarchyElements) +
			attributeToRSF("ParentChildRelations", parentChildRelations)
			attributeToRSF("Signatures", signatures) +
			attributeToRSF("Relations", relations)
			attributeToRSF("Root", root)
	}

	private def static attributeToRSF(String name, int attribute) {
		'"' + name + '"' + '"' + attribute + '"'
	}

	def static void write() {
	}

	def static void insertTrace(Trace trace) {
		trace.traceEvents.forEach [
			if (it instanceof AbstractBeforeEventRecord) {
				val beforeEvent = it as AbstractBeforeEventRecord
			}
		]
	}

	def static String getDebugString() {
		return constructFullHeader() + " "
	}
}
