package explorviz.visualization.interaction

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import explorviz.shared.model.Application
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.helper.CommunicationAppAccumulator
import java.util.ArrayList
import java.util.List
import com.google.gwt.user.client.Timer
import explorviz.visualization.interaction.TraceReplayer.PlayTimer

class TraceReplayer {
	static val PLAYBACK_SPEED_IN_MS = 1400

	static var Application application
	static var Long traceId
	static var List<CommunicationAppAccumulator> belongingAppCommunications = new ArrayList<CommunicationAppAccumulator>

	static var int currentIndex = 1
	static var int maxIndex = 1

	static PlayTimer playTimer

	public def static reset() {
		application = null
		belongingAppCommunications.clear()
		currentIndex = 1
	}

	def static replayInit(Long traceIdP, Application applicationP) {
		reset()

		application = applicationP
		traceId = traceIdP
		fillBelongingAppCommunications()

		val firstCommu = findCommuWithIndex(currentIndex)
		val tableInfos = createTableInformation(firstCommu)

		TraceReplayerJS::openDialog(traceId.toString(), tableInfos)
	}

	def static String createTableInformation(CommunicationClazz commu) {
		var tableInformation = "<tbody>"

		tableInformation += "<tr><td>Position:</td><td>" + currentIndex + " of " + maxIndex + "</td></tr>"
		tableInformation += "<tr><td>Caller:</td><td>" + SafeHtmlUtils::htmlEscape(commu.source.name) + "</td></tr>"
		tableInformation += "<tr><td>Callee:</td><td>" + SafeHtmlUtils::htmlEscape(commu.target.name) + "</td></tr>"
		tableInformation +=
			"<tr><td>Method:</td><td>" + SafeHtmlUtils::htmlEscape(commu.methodName) + "(..)</td></tr>"

		val runtime = commu.traceIdToRuntimeMap.get(traceId)

		//		tableInformation += "<tr><td>Requests:</td><td>" + runtime.requests + "</td></tr>"
		tableInformation +=
			"<tr><td>Avg. Time:</td><td>" + convertToMilliSecondTime(runtime.averageResponseTime) + " ms</td></tr>"

		tableInformation += "</tbody>"
	}

	def static fillBelongingAppCommunications() {
		var maxOrderIndex = 1
		for (commu : application.communicationsAccumulated) {
			val runtime = seekCommuWithTraceId(commu)
			if (runtime != null) {
				belongingAppCommunications.add(commu)
				for (orderIndex : runtime.orderIndexes) {
					if (orderIndex > maxOrderIndex) {
						maxOrderIndex = orderIndex
					}
				}
			}
		}

		maxIndex = maxOrderIndex
	}

	private def static seekCommuWithTraceId(CommunicationAppAccumulator commu) {
		for (aggCommu : commu.aggregatedCommunications) {
			val runtime = aggCommu.traceIdToRuntimeMap.get(traceId)
			if (runtime != null) {
				return runtime
			}
		}
		null
	}

	def static CommunicationClazz findCommuWithIndex(int index) {
		for (belongingAppCommunication : belongingAppCommunications) {
			for (aggCommu : belongingAppCommunication.aggregatedCommunications) {
				val runtime = aggCommu.traceIdToRuntimeMap.get(traceId)
				if (runtime != null && runtime.orderIndexes.contains(index)) {
					return aggCommu
				}
			}
		}
		return null
	}

	private def static String convertToMilliSecondTime(float x) {
		val result = (x / (1000 * 1000)).toString()

		result.substring(0, Math.min(result.indexOf('.') + 3, result.length - 1))
	}

	def static play() {
		if (playTimer != null)
			playTimer.cancel
		playTimer = new PlayTimer()
		playTimer.scheduleRepeating(PLAYBACK_SPEED_IN_MS)
	}

	def static pause() {
		if (playTimer != null)
			playTimer.cancel
	}

	def static void previous() {
		if (currentIndex > 0) {
			currentIndex--
			val tableInfos = createTableInformation(findCommuWithIndex(currentIndex))
			TraceReplayerJS::updateInformation(tableInfos)
		}
	}

	def static void next() {
		if (currentIndex < maxIndex) {
			currentIndex++
			val tableInfos = createTableInformation(findCommuWithIndex(currentIndex))
			TraceReplayerJS::updateInformation(tableInfos)
		}
	}

	static class PlayTimer extends Timer {
		override run() {
			next()
		}
	}
}
