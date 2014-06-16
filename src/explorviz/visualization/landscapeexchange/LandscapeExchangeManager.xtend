package explorviz.visualization.landscapeexchange

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.Timer
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.Logging
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeService
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeTimer

class LandscapeExchangeManager {
	val static DATA_EXCHANGE_INTERVALL_MILLIS = 10000
	
	var static LandscapeExchangeServiceAsync landscapeExchangeService
	var static Timer timer

	def static init() {
		landscapeExchangeService = createAsyncService()

		if(Experiment::tutorial){
			timer = new TutorialLandscapeExchangeTimer(landscapeExchangeService)
		}else{
			timer = new LandscapeExchangeTimer(landscapeExchangeService)
		}
		startAutomaticExchange()
	}

	def static startAutomaticExchange() {
		LandscapeConverter::reset()
		
		timer.run
		timer.scheduleRepeating(DATA_EXCHANGE_INTERVALL_MILLIS)
	}

	def static stopAutomaticExchange() {
		timer.cancel
	}
	
	def static fetchSpecificLandscape(String timestampInMillis) {
		landscapeExchangeService.getLandscape(Long.parseLong(timestampInMillis),new LandscapeConverter<Landscape>)
		if(Experiment::tutorial && Experiment::getStep.timeshift){
			Experiment::incStep()
		}
	}
	
	def static private createAsyncService() {
		if(Experiment::tutorial){
			Logging.log("tutorialLandscapeExchange created")
			val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(TutorialLandscapeExchangeService))
			val endpoint = landscapeExchangeService as ServiceDefTarget
			val moduleRelativeURL = GWT::getModuleBaseURL() + "tutoriallandscapeexchange"
			endpoint.serviceEntryPoint = moduleRelativeURL
			return landscapeExchangeService
		}else{
			val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
			val endpoint = landscapeExchangeService as ServiceDefTarget
			val moduleRelativeURL = GWT::getModuleBaseURL() + "landscapeexchange"
			endpoint.serviceEntryPoint = moduleRelativeURL
			return landscapeExchangeService		
		}
	}
}
