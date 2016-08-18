package explorviz.visualization.main

import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync
import com.google.gwt.core.client.GWT
import explorviz.visualization.landscapeexchange.LandscapeExchangeService
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.experiment.services.JSONService
import explorviz.visualization.experiment.services.QuestionServiceAsync
import explorviz.visualization.experiment.services.QuestionService

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

	def static getQuestionService() {
		val QuestionServiceAsync questionService = GWT::create(typeof(QuestionService))
		val endpoint = questionService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "questionservice"
		return questionService
	}

}
