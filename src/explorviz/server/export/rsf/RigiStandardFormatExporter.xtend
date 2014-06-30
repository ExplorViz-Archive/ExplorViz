package explorviz.server.export.rsf

import explorviz.live_trace_processing.record.trace.Trace
import explorviz.live_trace_processing.record.event.AbstractBeforeEventRecord
import java.util.ArrayList
import java.util.Stack
import explorviz.live_trace_processing.record.event.AbstractAfterEventRecord
import explorviz.live_trace_processing.record.event.AbstractAfterFailedEventRecord
import java.io.FileOutputStream
import java.io.FileNotFoundException
import explorviz.server.main.FileSystemHelper
import java.io.File

class RigiStandardFormatExporter {
	val static String HEADER = '"LevelSeparator" "."
"ElmType" "0" "Class"
"ElmType" "1" "Package"
"RelType" "0" "CallDynamicChronologic" "int:Increment" "sig:Signature" "string:Method"'

	val static hierarchyRoot = new RSFTreeNode("root")
	var static hierarchyId = 0

	var static parentChildRelationsCount = 0

	val static signatures = new ArrayList<RSFSignature>
	var static signatureId = 0

	val static relations = new ArrayList<RSFCall>
	var static relationId = 0

	static String FOLDER

	def static reset() {
		hierarchyRoot.getChildren.clear
		hierarchyId = 0

		parentChildRelationsCount = 0

		signatures.clear
		signatureId = 0

		relations.clear
		relationId = 0
	}

	def static void insertTrace(Trace trace) {
		var RSFTreeNode caller = null
		val Stack<RSFTreeNode> callerHistory = new Stack<RSFTreeNode>()

		reset()
		
		for (event : trace.traceEvents) {
			if (event instanceof AbstractBeforeEventRecord) {
				val callee = hierarchyRoot.insertIntoHierarchy(event.clazz.split("\\."))
				val signature = seekOrCreateSignature(event.operationSignature)

				if (caller != null) {
					val rsfCall = new RSFCall()
					rsfCall.caller = caller
					rsfCall.callee = callee
					rsfCall.signature = signature
					relations.add(rsfCall)
				}

				caller = callee
				callerHistory.push(callee);
			} else if ((event instanceof AbstractAfterEventRecord) || (event instanceof AbstractAfterFailedEventRecord)) {
				if (!callerHistory.isEmpty()) {
					callerHistory.pop();
				}
				if (!callerHistory.isEmpty()) {
					caller = callerHistory.peek();
				}

			}
		}

		if (!trace.traceEvents.empty && !relations.empty) {
			var appName = trace.traceEvents.get(0).hostApplicationMetadata.application
			appName = appName.replaceAll("<","")
			appName = appName.replaceAll(">","")
			
			var hostname = trace.traceEvents.get(0).hostApplicationMetadata.hostname
			hostname = hostname.replaceAll("<","")
			hostname = hostname.replaceAll(">","")
			
			write(trace.traceEvents.get(0).traceId, appName, hostname)
		}
	}

	def static RSFSignature seekOrCreateSignature(String sigSeeked) {
		for (signature : signatures) {
			if (signature.signature == sigSeeked) {
				return signature
			}
		}

		val newSig = new RSFSignature()
		newSig.signature = sigSeeked
		newSig.id = signatureId
		signatureId = signatureId + 1
		signatures.add(newSig)

		return newSig
	}

	private def static String constructFullHeader() {
		HEADER + "\n" + attributeToRSF("HierarchyDepth", hierarchyRoot.maxHierarchyDepth + 1) +
			attributeToRSF("HierarchyElements", hierarchyId) +
			attributeToRSF("ParentChildRelations", parentChildRelationsCount) +
			attributeToRSF("Signatures", signatures.size) + attributeToRSF("Relations", relations.size) +
			attributeToRSF("Root", 0)
	}

	private def static attributeToRSF(String name, int attribute) {
		'"' + name + '" ' + '"' + attribute + '"\n'
	}

	private def static String constructHierarchy() {
		hierarchyToRSF(hierarchyRoot, hierarchyRoot.getName, true) + constructHierarchyHelper(hierarchyRoot, "")
	}

	private def static String constructHierarchyHelper(RSFTreeNode node, String previousNames) {
		var result = ""
		val thisName = node.getName()

		for (child : node.getChildren) {
			val childName = previousNames + thisName + "." + child.getName

			if (child.getChildren.empty)
				result = result + hierarchyToRSF(child, childName, false)
			else {
				result = result + hierarchyToRSF(child, childName, true)
				result = result + constructHierarchyHelper(child, previousNames + thisName + ".")
			}

		}

		result
	}

	private def static hierarchyToRSF(RSFTreeNode node, String name, boolean isPackage) {
		val packageId = if (isPackage) 1 else 0

		val result = '"H" "' + hierarchyId + '" "' + name + '" "' + packageId + '"' + "\n"
		node.id = hierarchyId
		hierarchyId = hierarchyId + 1
		result
	}

	private def static String constructParentChildRelation() {
		var result = ""

		result = result + constructParentChildRelationHelper(hierarchyRoot)

		result
	}

	private def static String constructParentChildRelationHelper(RSFTreeNode node) {
		if (node.getChildren.empty) return "";

		var result = ""

		for (child : node.getChildren) {
			result = result + parentChildRelationToRSF(node, child)
		}

		for (child : node.getChildren) {
			result = result + constructParentChildRelationHelper(child)
		}

		result
	}

	private def static parentChildRelationToRSF(RSFTreeNode parent, RSFTreeNode child) {
		parentChildRelationsCount = parentChildRelationsCount + 1

		'"PCR" "' + parent.getId + '" "' + child.getId + '"' + "\n"
	}

	private def static String constructSignature() {
		var result = ""

		for (signature : signatures) {
			result = result + signatureToRSF(signature)
		}

		result
	}

	private def static signatureToRSF(RSFSignature signature) {
		'"S" "' + signature.id + '" "' + signature.signature + '" "' + "?" + '" "' + "?" + '"' + "\n"
	}

	private def static String constructRelation() {
		var result = ""

		for (relation : relations) {
			result = result + relationToRSF(relation.caller.getId, relation.callee.getId, relation.signature.id)
		}

		result
	}

	private def static relationToRSF(int callerId, int calleeId, int signatureId) {
		val result = '"R" "' + callerId + '" "' + calleeId + '" "' + "0" + '" "' + relationId + '" "' + signatureId +
			'" "' + "A" + '"' + "\n"
		relationId = relationId + 1
		result
	}

	def static void write(long traceId, String application, String hostname) {
		val toWriteRSF = buildString()

		if (FOLDER == null) {
			FOLDER = FileSystemHelper::getExplorVizDirectory() + "/" + "rsfExport"
			new File(FOLDER).mkdir()
		}

		var FileOutputStream output = null
		try {
			output = new FileOutputStream(FOLDER + "/" + application + "_" + hostname + "_" + traceId + ".initial.rsf")
			output.write(toWriteRSF.getBytes())
			output.flush()
			output.close()
		} catch (FileNotFoundException e) {
			e.printStackTrace()
		} finally {
			if (output != null) {
				output.close()
			}
		}
	}

	private def static String buildString() {
		val withoutHeader = constructHierarchy() + constructParentChildRelation() + constructSignature() +
			constructRelation()

		constructFullHeader() + withoutHeader
	}
}
