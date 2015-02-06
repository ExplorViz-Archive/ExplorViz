package explorviz.visualization.export

import explorviz.shared.model.Application
import explorviz.shared.model.Communication
import explorviz.shared.model.Landscape
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map

class RunnableLandscapeExporter {
	private static val String JAVA_EXECUTABLE = '"C:\\Program Files\\Java\\jdk1.7.0_75\\jre\\bin\\java"'
	private static val String ASPECTJ_WEAVER = 'aspectjweaver-1.8.5.jar'
	private static val String MAIN_JAR = 'light-RPC-application.jar'
	private static val int SERVER_RUNNING_FOR_SECONDS = 60

	private static int nextServerPort = 21000
	private static val Map<Application, Integer> applicationToServerPort = new HashMap<Application, Integer>()
	private static val Map<Application, Integer> applicationToIndexInResult = new HashMap<Application, Integer>()
	private static val List<String> threadsToJoin = new ArrayList<String>()

	def static String exportAsRunnableLandscapeRubyExport(Landscape landscape) {
		applicationToServerPort.clear
		applicationToIndexInResult.clear
		threadsToJoin.clear
		var result = ""

		val applicationSinks = seekCommunicationSinks(landscape)

		for (applicationSink : applicationSinks) {
			result = result + createApplicationSinkCode(applicationSink, result.length)
		}

		for (applicationSink : applicationSinks) {
			result = result + followCommunicationBackward(landscape, applicationSink)
		}

		for (threadToJoin : threadsToJoin) {
			result = result + threadToJoin + ".join\n"
		}

		result
	}

	def static String followCommunicationBackward(Landscape landscape, Application targetApplication) {
		var result = ""

		val sourceApplications = seekCommunicationBackwards(landscape, targetApplication)
		for (sourceApplication : sourceApplications) {
			if (applicationToIndexInResult.get(sourceApplication) != null) {
//				val applicationScriptIndex = applicationToIndexInResult.get(sourceApplication)
//				val paramsToAdd = ' -serverPortsToConnect ' + applicationToServerPort.get(targetApplication)
//
//				result = result.substring(0, applicationScriptIndex) + paramsToAdd + result.substring(applicationScriptIndex, result.length)
//				applicationToIndexInResult.put(sourceApplication, applicationScriptIndex + paramsToAdd.length)
			} else {
				result = result + createApplicationStageCode(sourceApplication,
					applicationToServerPort.get(targetApplication), 100, result.length)
			}
			result = result + followCommunicationBackward(landscape, sourceApplication)
		}

		result
	}

	private def static List<Application> seekCommunicationBackwards(Landscape landscape, Application target) {
		val results = new ArrayList<Application>()

		for (commu : landscape.applicationCommunication) {
			if (commu.target == target) {
				results.add(commu.source)
			}
		}

		results
	}

	private def static List<Application> seekCommunicationSinks(Landscape landscape) {
		val results = new ArrayList<Application>()

		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						if (!hasApplicationOutgoingCommu(application, landscape.applicationCommunication)) {
							results.add(application)
						}
					}
				}
			}
		}

		results
	}

	private def static boolean hasApplicationOutgoingCommu(Application application, List<Communication> communications) {
		for (commu : communications) {
			if (commu.source == application) {
				return true
			}
		}
		false
	}

	def static String createApplicationStageCode(Application applicationStage, int targetPort, int callsToServerCount,
		int scriptIndex) {
		createGenericApplicationCode(applicationStage, scriptIndex, ' -serverPortsToConnect ' + targetPort + ' -callsToServerCount ' + callsToServerCount)
	}

	def static String createApplicationSinkCode(Application applicationSink, int scriptIndex) {
		createGenericApplicationCode(applicationSink, scriptIndex, '')
	}
	
	def static createGenericApplicationCode(Application applicationSink, int scriptIndex, String additionalParameters) {
		val serverPort = nextServerPort++
		applicationToServerPort.put(applicationSink, serverPort)
		val hostname = applicationSink.parent.name ?: applicationSink.parent.ipAddress
		
		val cmdApplicationStart = JAVA_EXECUTABLE + '-Xmx64m -javaagent:' + ASPECTJ_WEAVER +
			' -Dexplorviz.live_trace_processing.system_name="' + applicationSink.parent.parent.parent.name +
			'" -Dexplorviz.live_trace_processing.ip_address="' + applicationSink.parent.ipAddress +
			'" -Dexplorviz.live_trace_processing.host_name="' + hostname +
			'" -Dexplorviz.live_trace_processing.application_name="' + applicationSink.name + '" -jar ' + MAIN_JAR +
			' -serverPort ' + serverPort + ' -secondsToRunServer ' + SERVER_RUNNING_FOR_SECONDS + additionalParameters
		
		val result = createApplicationCallCode(cmdApplicationStart, hostname, applicationSink.name, serverPort)
		
		applicationToIndexInResult.put(applicationSink, scriptIndex + result.length - '\')\nend\n\n'.length)
		
		result
	}

	def static String createApplicationCallCode(String cmd, String nodeName, String appName, int serverPort) {
		val threadName = "serverPortAppThread" + serverPort
		threadsToJoin.add(threadName)

		threadName + ' = Thread.new do\n\tputs("Started ' + appName + ' on ' + nodeName + '")\n\tsystem(\'' + cmd +
			'\')\nend\n\n'
	}

}
