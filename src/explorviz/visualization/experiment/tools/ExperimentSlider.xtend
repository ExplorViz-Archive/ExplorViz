package explorviz.visualization.experiment.tools

import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage

import static explorviz.visualization.experiment.tools.ExperimentTools.*
import explorviz.visualization.experiment.services.QuestionServiceAsync
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync
import explorviz.shared.model.Landscape
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.visualization.main.Util
import explorviz.visualization.experiment.TutorialJS
import java.util.List
import com.google.gwt.core.client.JsArrayString
import explorviz.visualization.experiment.callbacks.StringListCallback
import elemental.json.Json
import elemental.json.JsonObject
import explorviz.visualization.experiment.callbacks.GenericFuncCallback
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.visualization.engine.Logging

class ExperimentSlider implements IPage {
	private static PageControl pc;
	var static QuestionServiceAsync questionService
	var static JSONServiceAsync jsonService
	var static LandscapeExchangeServiceAsync landscapeService

	@Accessors var static String jsonQuestionnaire = null
	@Accessors var static String filename = null
	@Accessors var static boolean isWelcome = false

	override render(PageControl pageControl) {

		questionService = Util::getQuestionService()
		jsonService = Util::getJSONService()
		landscapeService = Util::getLandscapeService()
		
		WebGLStart::disable()
		JSHelpers::hideAllButtonsAndDialogs()

		pc = pageControl
		pc.setView("");
		
		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
				
		LandscapeExchangeManager::stopAutomaticExchange("0")
		
		JSHelpers::hideElementById("startStopBtn")
		JSHelpers::hideElementById("timeshiftChartDiv")
		JSHelpers::hideElementById("startStopLabel")
		

		landscapeService.getReplayNames(new StringListCallback<List<String>>([finishInit]))
	}

	def static finishInit(List<String> names) {

		var JsArrayString jsArrayString = JsArrayString.createArray().cast();
		for (String s : names) {
			jsArrayString.push(s.split(".expl").get(0));
		}
		
		ExperimentSliderJS::showSliderForExp(jsArrayString, jsonQuestionnaire, isWelcome)
	}

	def static void saveToServer(String jsonForm) {		
		var JsonObject data = Json.createObject		
		data.put(filename, jsonForm)
		jsonService.saveQuestionnaireServer(data.toJson, new VoidCallback())
	}
	
	def static getMaybeApplication() {		
		if(SceneDrawer::lastViewedApplication != null)
			return SceneDrawer::lastViewedApplication.name
			
		return null;
	}

	def static void loadLandscape(String filename, String maybeLandscape) {
		var parts = filename.split("-")
		
		Logging::log(maybeLandscape)

		var long timestamp = Long.parseLong(parts.get(0))
		var long activity = Long.parseLong(parts.get(1).split(".expl").get(0))

		landscapeService.getLandscape(timestamp, activity,
			new GenericFuncCallback<Landscape>(
				[					
					Landscape l | 
					SceneDrawer::createObjectsFromLandscape(l, false)
				]
			))
	}

}
