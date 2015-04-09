package explorviz.visualization.export

import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Landscape
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.List

class RunnableLandscapeExporter {
	private static val String JAVA_EXECUTABLE = '"java"'
	private static val String ASPECTJ_WEAVER = 'explorviz-monitoring.jar'
	private static val String MAIN_JAR = 'light-RPC-application.jar'
	private static val String MAX_MEMORY = ' -Xmx256m'
	private static val int SERVER_RUNNING_FOR_SECONDS = 600

	private static int nextServerPort = 21001
	private static val applicationToServerPort = new HashMap<Application, Integer>()
	private static val threadsToJoin = new ArrayList<String>()
	private static val applicationCode = new HashMap<Application, String>()
	private static val applicationOrder = new ArrayList<Application>()

	def static String exportAsRunnableLandscapeRubyExport(Landscape landscape) {
		nextServerPort = 21001
		applicationToServerPort.clear
		threadsToJoin.clear
		applicationCode.clear
		applicationOrder.clear

		val commus = duplicateList(landscape.applicationCommunication)

		while (!commus.empty) {
			val applicationSinks = seekCommunicationSinks(commus)

			for (var int i = 0; i < applicationSinks.size; i++) {
				val applicationSink = applicationSinks.get(i)

				val iterCommus = commus.iterator
				while (iterCommus.hasNext) {
					val commu = iterCommus.next
					if (commu.target == applicationSink) {
						iterCommus.remove
					}
				}

				applicationOrder.add(applicationSink)
				applicationCode.put(applicationSink, createApplicationCode(applicationSink))
			}

			applicationOrder.add(null)
		}

		val applicationStarts = seekCommunicationStarts(landscape.applicationCommunication)

		for (applicationStart : applicationStarts) {
			applicationOrder.add(applicationStart)
			applicationCode.put(applicationStart, createApplicationCode(applicationStart))
		}

		for (commu : landscape.applicationCommunication) {
			addCommunicationToClient(commu.source, commu.target, commu.requests)
		}

		var result = ""

		for (var int i = 0; i < applicationOrder.size; i++) {
			val app = applicationOrder.get(i)
			if (app != null) {
				result = result + applicationCode.get(app)
			} else {
				result = result + "sleep(10.0)\n\n"
			}
		}

		for (threadToJoin : threadsToJoin) {
			result = result + threadToJoin + ".join\n"
		}

		result
	}

	private def static void addCommunicationToClient(Application client, Application target, int callAmount) {
		val additionalParams = ' -serverPortsToConnect ' + applicationToServerPort.get(target) + ' -callsToServerCount ' +
			callAmount
		var code = applicationCode.get(client)
		val systemCallEndCode = '\')\nend\n\n'.length
		code = code.substring(0, code.length - systemCallEndCode) + additionalParams +
			code.substring(code.length - systemCallEndCode, code.length)
		applicationCode.put(client, code)
	}

	private def static duplicateList(List<Communication> commus) {
		val clone = new ArrayList<Communication>
		for (commu : commus) {
			clone.add(commu)
		}
		clone
	}

	private def static List<Application> seekCommunicationSinks(List<Communication> commus) {
		val potentialApplications = new HashSet<Application>()

		for (commu : commus) {
			potentialApplications.add(commu.target)
		}

		val result = new ArrayList<Application>()

		for (app : potentialApplications) {
			if (!hasApplicationOutgoingCommu(app, commus)) {
				result.add(app)
			}
		}

		if (!commus.empty && result.empty) {
			throw new Exception("Cycle detected")
		}

		result
	}

	private def static boolean hasApplicationOutgoingCommu(Application application, List<Communication> communications) {
		for (commu : communications) {
			if (commu.source == application) {
				return true
			}
		}
		false
	}

	private def static List<Application> seekCommunicationStarts(List<Communication> commus) {
		val potentialApplications = new HashSet<Application>()

		for (commu : commus) {
			potentialApplications.add(commu.source)
		}

		val result = new ArrayList<Application>()

		for (app : potentialApplications) {
			if (!hasApplicationIncomingCommu(app, commus)) {
				result.add(app)
			}
		}

		result
	}

	private def static boolean hasApplicationIncomingCommu(Application application, List<Communication> communications) {
		for (commu : communications) {
			if (commu.target == application) {
				return true
			}
		}
		false
	}

	private def static String createApplicationCode(Application applicationSink) {
		val serverPort = nextServerPort++
		applicationToServerPort.put(applicationSink, serverPort)
		val hostname = applicationSink.parent.name ?: applicationSink.parent.ipAddress

		val cmdApplicationStart = JAVA_EXECUTABLE + MAX_MEMORY + ' -javaagent:' + ASPECTJ_WEAVER +
			' -Dexplorviz.live_trace_processing.system_name="' + applicationSink.parent.parent.parent.name +
			'" -Dexplorviz.live_trace_processing.ip_address="' + applicationSink.parent.ipAddress +
			'" -Dexplorviz.live_trace_processing.host_name="' + hostname +
			'" -Dexplorviz.live_trace_processing.application_name="' + applicationSink.name +
			'" -Dexplorviz.live_trace_processing.programming_language="' + applicationSink.programmingLanguage.toString + '" -jar ' + MAIN_JAR +
			' -serverPort ' + serverPort + ' -secondsToRunServer ' + SERVER_RUNNING_FOR_SECONDS

		createApplicationCallCode(cmdApplicationStart, hostname, applicationSink.name, serverPort)
	}

	private def static String createApplicationCallCode(String cmd, String nodeName, String appName, int serverPort) {
		val threadName = "serverPortAppThread" + serverPort
		threadsToJoin.add(threadName)

		threadName + ' = Thread.new do\n\tputs("Trying to start ' + appName + ' on ' + nodeName + '")\n\tsystem(\'' + cmd +
			'\')\nend\n\n'
	}

}
