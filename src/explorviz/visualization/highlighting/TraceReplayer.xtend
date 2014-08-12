package explorviz.visualization.highlighting

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import com.google.gwt.user.client.Timer
import explorviz.shared.model.CommunicationClazz
import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.engine.math.Matrix44f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.renderer.ApplicationRenderer
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map

import static explorviz.visualization.highlighting.TraceReplayer.*
//import explorviz.visualization.engine.Logging

class TraceReplayer {
	static val PLAYBACK_SPEED_IN_MS = 2400

	public static var Long traceId
	public static var Map<Integer, CommunicationClazz> orderIdToCommunicationMap = new HashMap<Integer, CommunicationClazz>

	public static var CommunicationClazz currentlyHighlightedCommu
	public static var int currentIndex = 0
	static var int maxIndex = 0

	static TraceReplayer.PlayTimer playTimer

	static boolean animation = true
	static TraceReplayer.CameraFlyTimer cameraFly

	public def static reset() {
		currentlyHighlightedCommu = null
		orderIdToCommunicationMap.clear()
		currentIndex = 0

		TraceReplayerJS::closeDialog()
		if (playTimer != null) {
			playTimer.cancel
		}

		if (cameraFly != null) {
			cameraFly.cancel
		}
	}

	def static replayInit(Long traceIdP, int orderId) {
		reset()
		traceId = traceIdP
		currentIndex = orderId - 1
		fillBelongingAppCommunications(false)

		val firstCommu = findNextCommu(true)
		val tableInfos = createTableInformation(firstCommu)
		
		var tutorial = Experiment::tutorial && Experiment::getStep().startanalysis
		
		TraceReplayerJS::openDialog(traceId.toString(), tableInfos, currentIndex, maxIndex, tutorial)

		val application = SceneDrawer::lastViewedApplication
		if (application != null) {
			SceneDrawer::createObjectsFromApplication(application, true)
		}
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

		tableInformation += "<tr><th>Avg. Time:</th><td style='text-align: left'>" +
			convertToMilliSecondTime(runtime.averageResponseTime) + " ms</td></tr>"

		if (animation) {
			doFlyAnimation(commu)
		}
		currentlyHighlightedCommu = commu

		tableInformation
	}

	def static doFlyAnimation(CommunicationClazz commu) {
		var modelView = new Matrix44f();
		modelView = Matrix44f.rotationX(33).mult(modelView)
		modelView = Matrix44f.rotationY(45).mult(modelView)

		val viewCenterPoint = ApplicationRenderer::viewCenterPoint
		val rotatedSourceCenter = modelView.mult(new Vector4f(commu.source.centerPoint.sub(viewCenterPoint), 1f))
		val rotatedTargetCenter = modelView.mult(new Vector4f(commu.target.centerPoint.sub(viewCenterPoint), 1f))

		val nextCommu = findNextCommu(false)

		var flyBack = false

		if (nextCommu != null && nextCommu.source != commu.target) {
			flyBack = true
		}

		if (cameraFly != null) {
			cameraFly.cancel
		}

		cameraFly = new TraceReplayer.CameraFlyTimer(
			new Vector3f(rotatedSourceCenter.x * -1, rotatedSourceCenter.y * -1, -45f),
			new Vector3f(rotatedTargetCenter.x * -1, rotatedTargetCenter.y * -1, -45f), flyBack)
		cameraFly.scheduleRepeating(Math.round(1000f / 30))
	}

	static class CameraFlyTimer extends Timer {
		val fps = 30
		val Vector3f source
		val Vector3f target
		val Vector3f oneStepDistance
		var int currentStep = 0
		val boolean flyBack

		new(Vector3f source, Vector3f target, boolean flyBack) {
			this.source = source
			this.target = target
			val distance = target.sub(source)
			this.flyBack = flyBack

			if (distance.length > 0) {
				this.oneStepDistance = distance.scaleToLength(distance.length / fps)
			} else {
				this.oneStepDistance = new Vector3f()
			}
		}

		override run() {
			if (currentStep < fps) {
				Camera::focus(source.add(oneStepDistance.mult(currentStep)))
				currentStep++
			} else {
				cancel
				if (flyBack) {
					cameraFly = new TraceReplayer.CameraFlyTimer(target, source, false)
					cameraFly.scheduleRepeating(Math.round(1000f / 30))
				}
			}
		}
	}

	def static fillBelongingAppCommunications(boolean withSelfEdges) {
		var maxOrderIndex = 0
		val application = SceneDrawer::lastViewedApplication
		for (commu : application.communicationsAccumulated) {
			val aggCommus = seekCommusWithTraceId(commu, withSelfEdges)
			for (aggCommu : aggCommus) {
				val runtime = aggCommu.traceIdToRuntimeMap.get(traceId)
				if (runtime != null) {
					for (orderIndex : runtime.orderIndexes) {
						orderIdToCommunicationMap.put(orderIndex, aggCommu)
						if (orderIndex > maxOrderIndex) {
							maxOrderIndex = orderIndex
						}
					}
				}
			}
		}

		maxIndex = maxOrderIndex
	}

	private def static List<CommunicationClazz> seekCommusWithTraceId(CommunicationAppAccumulator commu,
		boolean withSelfEdges) {
		val result = new ArrayList<CommunicationClazz>
		for (aggCommu : commu.aggregatedCommunications) {
			val runtime = aggCommu.traceIdToRuntimeMap.get(traceId)
			if (runtime != null) {
				if (withSelfEdges || (aggCommu.source != aggCommu.target)) {
					result.add(aggCommu)
				}
			}
		}
		result
	}

	def static CommunicationClazz findCommuWithIndex(int index) {
		orderIdToCommunicationMap.get(index)
	}

	private def static String convertToMilliSecondTime(float x) {
		val result = (x / (1000 * 1000)).toString()

		result.substring(0, Math.min(result.indexOf('.') + 3, result.length - 1))
	}

	def static play() {
		if (!Experiment::tutorial || Experiment.getStep.startanalysis) {
			if (playTimer != null)
				playTimer.cancel
			playTimer = new TraceReplayer.PlayTimer()
			playTimer.scheduleRepeating(PLAYBACK_SPEED_IN_MS)
			if (Experiment::tutorial && Experiment.getStep.startanalysis) {
				Experiment.incStep()
//				if(Experiment.getStep.nextanalysis){
//					ExperimentJS.showNextHighlightArrow()
//				}else if(Experiment.getStep.pauseanalysis){
//					ExperimentJS.showPlayPauseHighlightArrow()
//				}
			}
		}

	}

	def static pause() {
		if (!Experiment::tutorial || Experiment.getStep.pauseanalysis) {
			if (Experiment::tutorial && Experiment.getStep.pauseanalysis) {
				Experiment.incStep()
//				if(Experiment.getStep.nextanalysis){
//					ExperimentJS.showNextHighlightArrow()
//				}else if(Experiment.getStep.startanalysis){
//					ExperimentJS.showPlayPauseHighlightArrow()
//				}
			}
			if (playTimer != null)
				playTimer.cancel
		}
	}

	def static void previous() {
		if (!Experiment::tutorial) {
			val commu = findPreviousCommu()
			if (commu != null) {
				val tableInfos = createTableInformation(commu)
				TraceReplayerJS::updateInformation(tableInfos, currentIndex)

				val application = SceneDrawer::lastViewedApplication
				if (application != null) {
					SceneDrawer::createObjectsFromApplication(application, true)
				}
			}
		}
	}

	def static CommunicationClazz findPreviousCommu() {
		while (currentIndex > 0) {
			currentIndex--
			val commu = findCommuWithIndex(currentIndex)
			if (commu != null) {
				return commu
			}
		}

		return null
	}

	def static void next() {
		if (!Experiment::tutorial || Experiment.getStep.nextanalysis || Experiment.getStep.startanalysis || Experiment.getLastStep().startanalysis) {
			if (Experiment::tutorial && Experiment.getStep.nextanalysis) {
				Experiment.incStep()
//				if(Experiment.getStep.pauseanalysis || Experiment.getStep.startanalysis){
//					ExperimentJS.showPlayPauseHighlightArrow()
//				}
			}
//			if(Experiment.getLastStep.startanalysis || Experiment.getStep.startanalysis){
//				Logging.log("play")
//			}
			val commu = findNextCommu(true)
			if (commu != null) {
				val tableInfos = createTableInformation(commu)
				TraceReplayerJS::updateInformation(tableInfos, currentIndex)

				val application = SceneDrawer::lastViewedApplication
				if (application != null) {
					SceneDrawer::createObjectsFromApplication(application, true)
				}
			}
		}
	}

	def static CommunicationClazz findNextCommu(boolean withIndexIncrease) {
		var index = currentIndex
		while (currentIndex < maxIndex) {
			index++
			val commu = findCommuWithIndex(index)
			if (commu != null) {
				if (withIndexIncrease)
					currentIndex = index
				return commu
			}
		}
		if (withIndexIncrease)
			currentIndex = index
		return null
	}

	def static void showSelfEdges() {
		orderIdToCommunicationMap.clear
		fillBelongingAppCommunications(true)
	}

	def static void hideSelfEdges() {
		orderIdToCommunicationMap.clear
		fillBelongingAppCommunications(false)
	}

	def static void showAnimation() {
		animation = true
	}

	def static void hideAnimation() {
		animation = false
	}

	def static void stepToEvent(String value) {
		val intValue = Integer.parseInt(value)
		currentIndex = intValue
		var commu = orderIdToCommunicationMap.get(intValue)
		if (commu == null) {
			commu = findNextCommu(true)
		}

		if (commu != null) {
			val tableInfos = createTableInformation(commu)
			TraceReplayerJS::updateInformation(tableInfos, currentIndex)

			val application = SceneDrawer::lastViewedApplication
			if (application != null) {
				SceneDrawer::createObjectsFromApplication(application, true)
			}
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
