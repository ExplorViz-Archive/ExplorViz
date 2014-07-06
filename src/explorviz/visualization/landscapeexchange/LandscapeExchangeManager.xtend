package explorviz.visualization.landscapeexchange

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.Timer
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.shared.model.Landscape
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeService
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeTimer
import com.google.gwt.user.client.ui.RootPanel
import java.util.Date
import com.google.gwt.i18n.client.DateTimeFormat

class LandscapeExchangeManager {
	val static DATA_EXCHANGE_INTERVALL_MILLIS = 15000

	var static LandscapeExchangeServiceAsync landscapeExchangeService
	var static Timer timer
	public static boolean timeshiftStopped = false

	static val startAndStopTimeshiftLabelId = "startStopLabel"
	static val startAndStopTimeshiftButtonId = "startStopBtn"

	def static init() {
		if (timer != null) {
			timer.cancel()
		}
		
		LandscapeExchangeCallback.firstExchange = true
		
		landscapeExchangeService = createAsyncService()

		if (Experiment::tutorial) {
			timer = new TutorialLandscapeExchangeTimer(landscapeExchangeService)
		} else {
			timer = new LandscapeExchangeTimer(landscapeExchangeService)
		}
		startAutomaticExchange()
	}

	def static startAutomaticExchange() {
		LandscapeExchangeCallback::reset()
		timeshiftStopped = false

		val startAndStopTimeshift = RootPanel::get(startAndStopTimeshiftButtonId)
		startAndStopTimeshift.element.innerHTML = "<span class='glyphicon glyphicon glyphicon-pause'></span> Pause"

		val startAndStopTimeshiftLabel = RootPanel::get(startAndStopTimeshiftLabelId)
		startAndStopTimeshiftLabel.element.innerHTML = ""

		timer.run
		timer.scheduleRepeating(DATA_EXCHANGE_INTERVALL_MILLIS)
	}

	def static stopAutomaticExchange(String timestampInMillis) {
		timeshiftStopped = true

		val startAndStopTimeshift = RootPanel::get(startAndStopTimeshiftButtonId)
		startAndStopTimeshift.element.innerHTML = "<span class='glyphicon glyphicon glyphicon-play'></span> Continue"

		val startAndStopTimeshiftLabel = RootPanel::get(startAndStopTimeshiftLabelId)
		startAndStopTimeshiftLabel.element.innerHTML = "Paused at: " +
			DateTimeFormat.getFormat("HH:mm:ss").format(new Date(Long.parseLong(timestampInMillis)))

		timer.cancel
	}

	def static fetchSpecificLandscape(String timestampInMillis) {
		landscapeExchangeService.getLandscape(Long.parseLong(timestampInMillis), new LandscapeExchangeCallback<Landscape>)
		if (Experiment::tutorial && Experiment::getStep.timeshift) {
			Experiment::incStep()
		}
	}

	def static private createAsyncService() {
		if (Experiment::tutorial) {
			val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(
				typeof(TutorialLandscapeExchangeService))
			val endpoint = landscapeExchangeService as ServiceDefTarget
			val moduleRelativeURL = GWT::getModuleBaseURL() + "tutoriallandscapeexchange"
			endpoint.serviceEntryPoint = moduleRelativeURL
			return landscapeExchangeService
		} else {
			val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
			val endpoint = landscapeExchangeService as ServiceDefTarget
			val moduleRelativeURL = GWT::getModuleBaseURL() + "landscapeexchange"
			endpoint.serviceEntryPoint = moduleRelativeURL
			return landscapeExchangeService
		}
	}
}
