package explorviz.visualization.landscapeexchange

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import com.google.gwt.user.client.Timer
import explorviz.shared.model.Landscape
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeService

class LandscapeExchangeManager {
	val static DATA_EXCHANGE_INTERVALL_MILLIS = 10000
	
	var static LandscapeExchangeServiceAsync landscapeExchangeService
	var static Timer timer

	def static init() {
		landscapeExchangeService = createAsyncService()

		LandscapeConverter::reset()
		
		timer = new LandscapeExchangeTimer(landscapeExchangeService)
		startAutomaticExchange()
	}

	def static startAutomaticExchange() {
		timer.run
		timer.scheduleRepeating(DATA_EXCHANGE_INTERVALL_MILLIS)
	}

	def static stopAutomaticExchange() {
		timer.cancel
	}
	
	def static fetchSpecificLandscape(String timestampInMillis) {
		landscapeExchangeService.getLandscape(Long.parseLong(timestampInMillis),new LandscapeConverter<Landscape>)
	}
	
	def static private createAsyncService() {
		if(Experiment::tutorial){
			val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(TutorialLandscapeExchangeService))
			val endpoint = landscapeExchangeService as ServiceDefTarget
			val moduleRelativeURL = GWT::getModuleBaseURL() + "tutoriallandscapeexchange"
			endpoint.serviceEntryPoint = moduleRelativeURL
		}else{
			val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
			val endpoint = landscapeExchangeService as ServiceDefTarget
			val moduleRelativeURL = GWT::getModuleBaseURL() + "landscapeexchange"
			endpoint.serviceEntryPoint = moduleRelativeURL			
		}
		landscapeExchangeService
	}
}
