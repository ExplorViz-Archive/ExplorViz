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
import explorviz.visualization.experiment.tools.ExperimentTools

/**
 * @author Santje Finke
 * 
 */
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
						<label for="freeInput">How many free inputs: (not compatible with given answers)</label>
						<select class='form-control' id='freeInput' name='frees' required>
							<option>0</option>	
							<option>1</option>	
							<option>2</option>
							<option>3</option>
							<option>4</option>
							<option>5</option>
							<option>6</option>
							<option>7</option>
							<option>8</option>
							<option>9</option>
							<option>10</option>
						</select>
						<label for="answerbox">Given Answers: (seperate with ,)</label>
						<textarea class="form-control" id="answerbox" name="answers" rows="3"></textarea>
						<label for="correctbox">Correct Answers: (seperate with ,)</label>
						<textarea class="form-control" id="correctbox" name="corrects" rows="3"></textarea>
						<label for='worktime'>Time for questions (minutes):</label>
						<input type="number" class="form-control" id="worktime" placeholder="Minutes" name="work">
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
		ExperimentTools::toolsMode = false
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
		var frees = 0
		if(questionList.get(1).substring(6) != ""){
			frees = Integer.parseInt(questionList.get(1).substring(6)) //frees=
		}
		var answerString = questionList.get(2).substring(8) //answers=
		var String[] answers = answerString.split(",")
		var correctString = questionList.get(3).substring(9) //corrects=
		var String[] corrects = correctString.split(",")
		var workTime = 8
		if(questionList.get(4).substring(5) != ""){
			workTime = Integer.parseInt(questionList.get(5).substring(5)) //work=
		} 
		var time = 0L
		if(questionList.get(5).substring(5) != ""){
			time = Long.parseLong(questionList.get(5).substring(5)) //time=
		}
		
		
		return new Question(0,text,answers,corrects,frees,workTime,time)
	}

}