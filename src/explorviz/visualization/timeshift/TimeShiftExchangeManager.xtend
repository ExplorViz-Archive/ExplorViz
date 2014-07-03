package explorviz.visualization.timeshift

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.landscapeexchange.TutorialTimeShiftExchangeService
import explorviz.visualization.experiment.landscapeexchange.TutorialTimeShiftExchangeServiceAsync
import java.util.Map

class TimeShiftExchangeManager {
	static var TimeShiftExchangeServiceAsync timeshiftExchangeService

	def static init() {
		TimeShiftJS.init()

		timeshiftExchangeService = createAsyncService()
	}

	def static void updateTimeShiftGraph() {
		if (Experiment::tutorial) {
			val tutorialExchange = timeshiftExchangeService as TutorialTimeShiftExchangeServiceAsync
			tutorialExchange.getAvailableLandscapes(new TimeShiftCallback<Map<Long, Long>>())
		} else {
			timeshiftExchangeService.getAvailableLandscapes(new TimeShiftCallback<Map<Long, Long>>())
		}
	}

	def static private createAsyncService() {
		if (Experiment::tutorial) {
			createAsyncServiceHelper(true, "tutorialtimeshiftexchange")
		} else {
			createAsyncServiceHelper(false, "timeshiftexchange")
		}
	}

	def static private createAsyncServiceHelper(boolean tutorial, String endpointURL) {
		var TimeShiftExchangeServiceAsync timeshiftExchangeService = if (tutorial) {
				GWT::create(
					typeof(TutorialTimeShiftExchangeService)
				)
			} else {
				GWT::create(
					typeof(TimeShiftExchangeService)
				)
			}
			
		val endpoint = timeshiftExchangeService as ServiceDefTarget
		val moduleRelativeURL = GWT::getModuleBaseURL() + endpointURL
		endpoint.serviceEntryPoint = moduleRelativeURL

		return timeshiftExchangeService
	}
}
