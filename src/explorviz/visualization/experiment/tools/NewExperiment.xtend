package explorviz.visualization.experiment.tools

import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage

import static explorviz.visualization.experiment.tools.ExperimentTools.*
import explorviz.visualization.experiment.services.QuestionServiceAsync
import com.google.gwt.core.client.GWT
import explorviz.visualization.experiment.services.QuestionService
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.callbacks.VoidCallback
import java.util.List
import explorviz.visualization.experiment.tools.NewExperimentJS.OverlayJSObj
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync
import explorviz.visualization.landscapeexchange.LandscapeExchangeCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.landscapeexchange.ReplayNamesExchangeCallback
import org.eclipse.xtend.lib.annotations.Accessors
import com.google.gwt.core.client.JsArrayString
import explorviz.visualization.main.Util
import explorviz.visualization.experiment.TutorialJS

class NewExperiment implements IPage {
	private static PageControl pc;
	var static QuestionServiceAsync questionService
	var static JSONServiceAsync jsonService
	var static LandscapeExchangeServiceAsync landscapeService

	@Accessors private var static JsArrayString landscapeNames = null

	override render(PageControl pageControl) {
		questionService = getQuestionService()
		jsonService = Util::getJSONService()
		landscapeService = Util::getLandscapeService()
		pc = pageControl
		pc.setView("");

		// will call finishInit on success
		landscapeService.getReplayNames(new ReplayNamesExchangeCallback<List<String>>())
	}

	def static finishInit(List<String> names) {

		var JsArrayString jsArrayString = JsArrayString.createArray().cast();
		for (String s : names) {
			jsArrayString.push(s.split(".expl").get(0));
		}

		NewExperimentJS::init(jsArrayString)

		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
	}

	def static getQuestionService() {
		val QuestionServiceAsync questionService = GWT::create(typeof(QuestionService))
		val endpoint = questionService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "questionservice"
		return questionService
	}

	def static void saveToServer(String jsonForm) {

		jsonService.sendJSON(jsonForm, new VoidCallback())
	}

	def static void loadLandscape(String filename) {

		var parts = filename.split("-")

		var long timestamp = Long.parseLong(parts.get(0))
		var long activity = Long.parseLong(parts.get(1).split(".expl").get(0))

		landscapeService.getLandscapeByTimestampAndActivity(timestamp, activity,
			new LandscapeExchangeCallback<Landscape>(true))
	}

}
