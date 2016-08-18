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
import explorviz.visualization.landscapeexchange.LandscapeExchangeCallback
import explorviz.shared.model.Landscape
import org.eclipse.xtend.lib.annotations.Accessors
import explorviz.visualization.main.Util
import explorviz.visualization.experiment.TutorialJS
import java.util.List
import com.google.gwt.core.client.JsArrayString
import explorviz.visualization.experiment.callbacks.StringListCallback

class ExperimentSlider implements IPage {
	private static PageControl pc;
	var static QuestionServiceAsync questionService
	var static JSONServiceAsync jsonService
	var static LandscapeExchangeServiceAsync landscapeService

	@Accessors var static String jsonExperiment = null

	override render(PageControl pageControl) {

		questionService = Util::getQuestionService()
		jsonService = Util::getJSONService()
		landscapeService = Util::getLandscapeService()

		pc = pageControl
		pc.setView("");

		landscapeService.getReplayNames(new StringListCallback<List<String>>([finishInit]))

	}

	def static finishInit(List<String> names) {

		var JsArrayString jsArrayString = JsArrayString.createArray().cast();
		for (String s : names) {
			jsArrayString.push(s.split(".expl").get(0));
		}

		SliderWrapperJS::showSliderForExp(jsArrayString, jsonExperiment)

		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
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
