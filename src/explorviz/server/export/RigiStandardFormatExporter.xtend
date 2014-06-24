package explorviz.server.export

import explorviz.live_trace_processing.record.trace.Trace
import explorviz.live_trace_processing.record.event.AbstractBeforeEventRecord

class RigiStandardFormatExporter {
	val static String HEADER = '"LevelSeparator" "."
"ElmType" "0" "Class"
"ElmType" "1" "Package"
"RelType" "0" "CallDynamicChronologic" "int:Increment" "sig:Signature" "string:Method"'
	val static int parentChildRelations = 1
	val static int signatures = 1
	val static int relations = 1
	val static int root = 0

	val static hierarchyRoot = new TreeNode("root")
	var static hierarchyId = 0

	private def static String constructFullHeader() {
		reset()
		
		HEADER + "\n" + attributeToRSF("HierarchyDepth", hierarchyRoot.maxHierarchyDepth) +
			attributeToRSF("HierarchyElements", hierarchyId + 1) +
			attributeToRSF("ParentChildRelations", parentChildRelations) + attributeToRSF("Signatures", signatures) +
			attributeToRSF("Relations", relations) + attributeToRSF("Root", root)
	}
	
	def static reset() {
		hierarchyId = 0
	}

	private def static attributeToRSF(String name, int attribute) {
		'"' + name + '" ' + '"' + attribute + '"\n'
	}

	private def static String constructHierarchy() {
		var result = ""

		for (child : hierarchyRoot.children) {
			result = result + hierarchyToRSF(child.name, true)
			result = result + constructHierarchyHelper(child, "")
		}

		result
	}

	private def static String constructHierarchyHelper(TreeNode node, String previousNames) {
		var result = ""
		val thisName = node.getName()

		for (child : node.children) {
			val childName = previousNames + thisName + "." + child.name

			if (child.children.empty)
				result = result + hierarchyToRSF(childName, false)
			else {
				result = result + hierarchyToRSF(childName, true)
				result = result + constructHierarchyHelper(child, previousNames + thisName + ".")
			}

		}

		result
	}

	private def static hierarchyToRSF(String name, boolean isPackage) {
		val packageId = if (isPackage) 1 else 0

		val result = '"H" "' + hierarchyId + '" "' + name + '" "' + packageId + '"' + "\n"
		hierarchyId = hierarchyId + 1
		result
	}

	def static void write() {
	}

	def static void insertTrace(Trace trace) {
		trace.traceEvents.forEach [
			if (it instanceof AbstractBeforeEventRecord) {
				val beforeEvent = it as AbstractBeforeEventRecord
				hierarchyRoot.insertIntoHierarchy(beforeEvent.clazz.split("\\."))
			}
		]
	}

	def static String getDebugString() {
		return constructFullHeader() + constructHierarchy()
	}
}
