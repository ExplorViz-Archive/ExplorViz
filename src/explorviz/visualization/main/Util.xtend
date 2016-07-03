package explorviz.visualization.main

import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync
import com.google.gwt.core.client.GWT
import explorviz.visualization.landscapeexchange.LandscapeExchangeService
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.experiment.services.JSONService

class Util {

	def static getLandscapeService() {
		val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
		val endpoint = landscapeExchangeService as ServiceDefTarget
		val moduleRelativeURL = GWT::getModuleBaseURL() + "landscapeexchange"
		endpoint.serviceEntryPoint = moduleRelativeURL
		return landscapeExchangeService
	}

	def static getJSONService() {
		val JSONServiceAsync jsonService = GWT::create(typeof(JSONService))
		val endpoint = jsonService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "jsonservice"
		return jsonService
	}

}
