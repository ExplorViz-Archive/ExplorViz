package explorviz.server.export.rsf

import explorviz.live_trace_processing.record.event.AbstractAfterEventRecord
import explorviz.live_trace_processing.record.event.AbstractAfterFailedEventRecord
import explorviz.live_trace_processing.record.event.AbstractBeforeOperationEventRecord
import explorviz.live_trace_processing.record.event.constructor.BeforeConstructorEventRecord
import explorviz.live_trace_processing.record.trace.Trace
import explorviz.server.main.Configuration
import explorviz.server.main.FileSystemHelper
import explorviz.server.repository.InsertionRepositoryPart
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.Random
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class RigiStandardFormatExporter {
	val static threadPool = Executors.newCachedThreadPool()
	var static exportBuffer = new ConcurrentHashMap<Long, RigiStandardFormatExporter>()

	val static String HEADER = '"LevelSeparator" "."
"ElmType" "0" "Class"
"ElmType" "1" "Package"
"RelType" "0" "CallDynamicChronologic" "int:Increment" "sig:Signature" "string:Method"'

	val hierarchyRoot = new RSFTreeNode("root")
	var hierarchyId = 0

	var parentChildRelationsCount = 0

	val signatures = new ArrayList<RSFSignature>
	var signatureId = 0

	val relations = new ArrayList<RSFCall>
	var relationId = 0

	long traceId
	String appName
	String hostname
	var volatile lastInsertTimestamp = 0L

	def static void insertTrace(Trace trace) {
		if (trace.traceEvents.empty) return

		val traceId = trace.traceEvents.get(0).traceId

		var bufferedExporter = exportBuffer.get(traceId)
		if (bufferedExporter == null) {
			bufferedExporter = new RigiStandardFormatExporter()
			exportBuffer.put(traceId, bufferedExporter)
		}

		runInThread(bufferedExporter, trace)
	}

	private def static void runInThread(RigiStandardFormatExporter exporter, Trace trace) {
		threadPool.execute(
			new Runnable() {
				override run() {
					exporter.insertTraceThreaded(trace)
				}
			})
	}

	protected def void insertTraceThreaded(Trace trace) {
		synchronized (this) {
			val firstEntry = trace.traceEvents.get(0)

			for (hostMeta : firstEntry.hostApplicationMetadataList) {
				var RSFTreeNode caller = null
				val Stack<RSFTreeNode> callerHistory = new Stack<RSFTreeNode>()

				lastInsertTimestamp = System.currentTimeMillis()
				appName = hostMeta.application.replaceAll("<", "").replaceAll(">", "")
				hostname = hostMeta.hostname.replaceAll("<", "").replaceAll(">", "")
				traceId = firstEntry.traceId

				for (event : trace.traceEvents) {
					if (event instanceof AbstractBeforeOperationEventRecord) {
						val clazzname = InsertionRepositoryPart.getClazzName(event)
						val callee = hierarchyRoot.insertIntoHierarchy(clazzname.split("\\."))

						if (caller != null) {
							val isConstructor = event instanceof BeforeConstructorEventRecord
							var methodName = InsertionRepositoryPart.getMethodName(event.getOperationSignature(),
								isConstructor)

							var isAbstractConstructor = false;

							if (isConstructor) {
								val constructor = event as BeforeConstructorEventRecord;
								val constructorClass = constructor.getClazz().substring(
									constructor.getClazz().lastIndexOf('.') + 1);
								val constructorClassFromOperation = methodName.substring(4);

								isAbstractConstructor = !constructorClass.equalsIgnoreCase(constructorClassFromOperation);
							}

							if (!isAbstractConstructor) {
								val signature = seekOrCreateSignature(event.getOperationSignature(), clazzname)

								val rsfCall = new RSFCall()
								rsfCall.caller = caller
								rsfCall.callee = callee
								rsfCall.signature = signature
								relations.add(rsfCall)
							}
						}

						caller = callee
						callerHistory.push(callee);
					} else if ((event instanceof AbstractAfterEventRecord) ||
						(event instanceof AbstractAfterFailedEventRecord)) {
						if (!callerHistory.isEmpty()) {
							callerHistory.pop();
						}
						if (!callerHistory.isEmpty()) {
							caller = callerHistory.peek();
						}

					}
				}
			}
		}

		val waitIntervalInMs = (Configuration::outputIntervalSeconds + 60) * 1000
		Thread.sleep(waitIntervalInMs)
		if (System.currentTimeMillis() - waitIntervalInMs > lastInsertTimestamp) {
			finishAndWrite()
		}
	}

	protected def finishAndWrite() {
		synchronized (this) {
			if (!relations.empty) {
				write(traceId, appName, hostname)
				relations.clear()
				exportBuffer.remove(traceId)
			}
		}
	}

	def RSFSignature seekOrCreateSignature(String sigSeeked, String classname) {
		for (signature : signatures) {
			if (signature.signature == sigSeeked) {
				return signature
			}
		}

		val newSig = new RSFSignature()
		newSig.signature = sigSeeked
		newSig.classname = classname
		newSig.id = signatureId
		signatureId = signatureId + 1
		signatures.add(newSig)

		return newSig
	}

	private def String constructFullHeader() {
		HEADER + "\n" + attributeToRSF("HierarchyDepth", hierarchyRoot.maxHierarchyDepth + 1) +
			attributeToRSF("HierarchyElements", hierarchyId) +
			attributeToRSF("ParentChildRelations", parentChildRelationsCount) +
			attributeToRSF("Signatures", signatures.size) + attributeToRSF("Relations", relations.size) +
			attributeToRSF("Root", 0)
	}

	private def attributeToRSF(String name, int attribute) {
		'"' + name + '" ' + '"' + attribute + '"\n'
	}

	private def void constructHierarchy(StringBuilder sb) {
		hierarchyToRSF(hierarchyRoot, hierarchyRoot.getName, true, sb)
		constructHierarchyHelper(hierarchyRoot, "", sb)
	}

	private def void constructHierarchyHelper(RSFTreeNode node, String previousNames, StringBuilder sb) {
		val thisName = node.getName()

		for (child : node.getChildren) {
			val childName = previousNames + thisName + "." + child.getName

			if (child.getChildren.empty)
				hierarchyToRSF(child, childName, false, sb)
			else {
				hierarchyToRSF(child, childName, true, sb)
				constructHierarchyHelper(child, previousNames + thisName + ".", sb)
			}

		}
	}

	private def void hierarchyToRSF(RSFTreeNode node, String name, boolean isPackage, StringBuilder sb) {
		val packageId = if (isPackage) 1 else 0

		sb.append('"H" "').append(hierarchyId).append('" "').append(name).append('" "').append(packageId).append('"').
			append('\n')
		node.id = hierarchyId
		hierarchyId = hierarchyId + 1
	}

	private def void constructParentChildRelation(StringBuilder sb) {
		constructParentChildRelationHelper(hierarchyRoot, sb)
	}

	private def void constructParentChildRelationHelper(RSFTreeNode node, StringBuilder sb) {
		if (node.getChildren.empty) return;

		for (child : node.getChildren) {
			parentChildRelationToRSF(node, child, sb)
		}

		for (child : node.getChildren) {
			constructParentChildRelationHelper(child, sb)
		}
	}

	private def void parentChildRelationToRSF(RSFTreeNode parent, RSFTreeNode child, StringBuilder sb) {
		parentChildRelationsCount = parentChildRelationsCount + 1

		sb.append('"PCR" "').append(parent.getId).append('" "').append(child.getId).append('"').append('\n')
	}

	private def void constructSignature(StringBuilder sb) {
		for (signature : signatures) {
			signatureToRSF(signature, sb)
		}
	}

	private def void signatureToRSF(RSFSignature signature, StringBuilder sb) {
		val filename = signature.classname.replaceAll("\\.", "/") + ".java"

		sb.append('"S" "').append(signature.id).append('" "').append(signature.signature).append('" "').append(filename).
			append('" "').append('1').append('"').append('\n')
	}

	private def void constructRelation(StringBuilder sb) {
		for (relation : relations) {
			relationToRSF(relation.caller.getId, relation.callee.getId, relation.signature.id, sb)
		}
	}

	private def void relationToRSF(int callerId, int calleeId, int signatureId, StringBuilder sb) {
		sb.append('"R" "').append(callerId).append('" "').append(calleeId).append('" "').append('0').append('" "').
			append(relationId).append('" "').append(signatureId).append('" "').append('A').append('"').append('\n')
		relationId = relationId + 1
	}

	def void write(long traceId, String application, String hostname) {
		val toWriteRSF = buildString()

		val FOLDER = FileSystemHelper::getExplorVizDirectory() + "/" + "rsfExport"
		new File(FOLDER).mkdir()

		var FileOutputStream output = null
		try {
			var file = new File(FOLDER + "/" + application + "_" + hostname + "_" + traceId + ".initial.rsf")
			if (file.exists) {
				file = new File(
					FOLDER + "/" + application + "_" + hostname + "_" + traceId + "_" + new Random().nextInt +
						".initial.rsf")
			}
			output = new FileOutputStream(file)
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

	private def String buildString() {
		val sb = new StringBuilder(8 * 1024 * 1024)
		constructHierarchy(sb)
		constructParentChildRelation(sb)
		constructSignature(sb)
		constructRelation(sb)

		sb.insert(0, constructFullHeader())
		sb.toString()
	}
}
