package explorviz.visualization.experiment

import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage

import static explorviz.visualization.experiment.tools.ExperimentTools.*
import explorviz.shared.experiment.Question
import explorviz.visualization.experiment.services.QuestionServiceAsync
import com.google.gwt.core.client.GWT
import explorviz.visualization.experiment.services.QuestionService
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.callbacks.VoidCallback
import java.util.List
import com.google.gwt.core.client.JavaScriptObject
import explorviz.visualization.engine.Logging
import com.google.gwt.core.client.JsonUtils
import explorviz.visualization.experiment.NewExperimentJS.OverlayJSObj
import java.util.ArrayList
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.experiment.services.JSONService

class NewExperiment implements IPage {
	private static PageControl pc;
	var static QuestionServiceAsync questionService
	var static JSONServiceAsync jsonService

	override render(PageControl pageControl) {
		questionService = getQuestionService()
		jsonService = getJSONService()
		pc = pageControl
		pageControl.setView("");

		NewExperimentJS::init()

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
	
		def static getJSONService() {
		val JSONServiceAsync jsonService = GWT::create(typeof(JSONService))
		val endpoint = jsonService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "jsonservice"
		return jsonService
	}

	def static void saveToServer(OverlayJSObj formValues) {

		val keys = formValues.keys
		val values = formValues.values

		Logging::log(keys.get(0))
		Logging::log(values.get(0))

		val length = keys.length

		// question text
		val text = values.get(0)

		val workingTime = Integer.parseInt(values.get(1))

		val freeAnswers = Integer.parseInt(values.get(2))

		// parse correct answers
		var List<String> correctList = new ArrayList<String>();

		for (var i = 3; i < length; i++) {
			correctList.add(values.get(i))
		}
		var temp = correctList.size
		var String[] correct = newArrayOfSize(temp)
		correct = correctList.toArray(correct)

		val String[] answer = #[]

		// create object and send to server
		var Question newquestion = new Question(1, text, answer, correct, freeAnswers, workingTime, 1402)
		// questionService.updateOrSaveQuestion(newquestion, new VoidCallback())
		questionService.saveQuestion(newquestion, new VoidCallback())
		
		jsonService.sendJSON("test", new VoidCallback())

	}

}
