package explorviz.visualization.experiment

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.ExperimentJS
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.experiment.services.QuestionServiceAsync
import explorviz.visualization.experiment.services.QuestionService
import explorviz.shared.experiment.Question

class EditQuestionsPage implements IPage {

	var static QuestionServiceAsync questionService 

	override render(PageControl pageControl) {
		questionService = getQuestionService()
		
		Navigation::deregisterWebGLKeys()
		JSHelpers::hideAllButtonsAndDialogs()
		JSHelpers::showElementById("overwriteQuestions")
		JSHelpers::showElementById("addQuestion")
		

		pageControl.setView(
			'''<div style="width:400px; margin:0 auto"><form style="display: inline-block; text-align: center;" class='form' role='form' id='editQuestionsForm'>
					<div class="form-group">
						<label for="textbox">Questiontext:</label>
						<textarea class="form-control" id="textbox" name="text" rows="3"></textarea>
						<label for="freeInput">How many free inputs: (not compatible with given Answers)</label>
						<input type="number" max="10" class="form-control" id="freeInput" placeholder="Free Answers" name="frees">
						<label for="answerbox">Given Answers: (seperate with ,)</label>
						<textarea class="form-control" id="answerbox" name="answers" rows="3"></textarea>
						<label for="correctbox">Correct Answers: (seperate with ,)</label>
						<textarea class="form-control" id="correctbox" name="corrects" rows="3"></textarea>
						<label for="timeInput">Timestamp of the furthest recording the replay may progress to.</label>
						<input type="number" class="form-control" id="timeInput" placeholder="Timestamp" name="time">
					</div></form></br>
						<button id="overwriteQuestions" type="button" class="btn btn-default btn-sm">
		<span class="glyphicon glyphicon-floppy-disk"></span> Overwrite Questions</button>
						<button id="addQuestion" type="button" class="btn btn-default btn-sm">
		<span class="glyphicon glyphicon-floppy-disk"></span> Add Question</button></div>'''.
				toString())
				
		ExperimentJS::initEditQuestions()

		Experiment::tutorial = false
	}

	def static getQuestionService(){
		val QuestionServiceAsync questionService = GWT::create(typeof(QuestionService))
		val endpoint = questionService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "questionservice"
		return questionService
	}


	static def saveQuestion(String question) {
		var q = parseQuestion(question)
		questionService.saveQuestion(q, new VoidCallback())
	}
	
	static def overwriteQuestions(String question){
		var q = parseQuestion(question)
		questionService.overwriteQuestions(q, new VoidCallback())
	}
	
	static def parseQuestion(String question){
		var cleaned = question.replace("+"," ") //whitespaces
		cleaned = cleaned.replace("%2C",",") //commata
		cleaned = cleaned.replace("%3F", "?") //questionmark
		var String[] questionList = cleaned.split("&")
		var text = questionList.get(0).substring(5) //text=
		var time = 0L
		if(questionList.get(4).substring(5) != ""){
			time = Long.parseLong(questionList.get(4).substring(5)) //time=
		}
		var frees = 0
		if(questionList.get(1).substring(6) != ""){
			frees = Integer.parseInt(questionList.get(1).substring(6)) //frees=
		}
		var correctString = questionList.get(3).substring(9) //corrects=
		var answerString = questionList.get(2).substring(8) //answers=
		var String[] corrects = correctString.split(",")
		var String[] answers = answerString.split(",")
		
		return new Question(0,text,answers,corrects,frees,time)
	}

}