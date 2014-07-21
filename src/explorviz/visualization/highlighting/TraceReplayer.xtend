package explorviz.visualization.highlighting

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import explorviz.shared.model.Application
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.helper.CommunicationAppAccumulator
import java.util.ArrayList
import java.util.List
import com.google.gwt.user.client.Timer
import explorviz.shared.model.helper.EdgeState

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

		TraceReplayerJS::closeDialog()
		if (playTimer != null) {
			playTimer.cancel
		}
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
		var tableInformation = ""

		tableInformation +=
			"<tr><th>Position:</th><td style='text-align: left'>" + currentIndex + " of " + maxIndex + "</td></tr>"
		tableInformation += "<tr><th>Caller:</th><td style='text-align: left'>" +
			SafeHtmlUtils::htmlEscape(commu.source.name) + "</td></tr>"
		tableInformation += "<tr><th>Callee:</th><td style='text-align: left'>" +
			SafeHtmlUtils::htmlEscape(commu.target.name) + "</td></tr>"
		tableInformation += "<tr><th>Method:</th><td style='text-align: left'>" +
			SafeHtmlUtils::htmlEscape(commu.methodName) + "(..)</td></tr>"

		val runtime = commu.traceIdToRuntimeMap.get(traceId)

		//		tableInformation += "<tr><th>Requests:</th><td style='text-align: left'>" + runtime.requests + "</td></tr>"
		tableInformation += "<tr><th>Avg. Time:</th><td style='text-align: left'>" +
			convertToMilliSecondTime(runtime.averageResponseTime) + " ms</td></tr>"

		tableInformation
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
			belongingAppCommunication.state = EdgeState.SHOW_DIRECTION_OUT
		}

		for (belongingAppCommunication : belongingAppCommunications) {
			for (aggCommu : belongingAppCommunication.aggregatedCommunications) {
				val runtime = aggCommu.traceIdToRuntimeMap.get(traceId)
				if (runtime != null && runtime.orderIndexes.contains(index)) {
					belongingAppCommunication.state = EdgeState.REPLAY_HIGHLIGHT
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
			if (currentIndex < maxIndex) {
				next()
			} else {
				this.cancel
			}
		}
	}
}
