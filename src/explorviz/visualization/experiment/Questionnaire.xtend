package explorviz.visualization.experiment

import java.util.ArrayList
import explorviz.shared.experiment.Question
import explorviz.shared.experiment.Answer
import java.util.List
import explorviz.visualization.experiment.services.QuestionServiceAsync
import com.google.gwt.core.client.GWT
import explorviz.visualization.experiment.services.QuestionService
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.callbacks.QuestionsCallback
import explorviz.visualization.engine.Logging

class Questionnaire {
	static int questionNr = 0
	static long timestampStart
	public static List<Question> questions = new ArrayList<Question>()
	public static List<Answer> answers = new ArrayList<Answer>()


	def static startQuestions(){
		Questionnaire::loadQuestions()
		timestampStart = System.currentTimeMillis()
		ExperimentJS::showQuestionDialog()
	}

	def static loadQuestions(){
		val QuestionServiceAsync questionService = GWT::create(typeof(QuestionService))
		val endpoint = questionService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "questionservice"
		questionService.getQuestions(new QuestionsCallback())
	}
	
	

	def static getQuestionBox(Question question){
		var html = "<p>"+question.text+"</p>"
		html = html + "<form class='form-inline' role='form' id='questionForm'>"
		var String[]  ans = question.answers
		if(question.type.equals("Free")){
			html = html + "<div class='form-group'>
							<label class='sr-only' for='answer'>Answer</label>"
    		html = html + "<input type='text' class='form-control' id='input' placeholder='Enter Answer' name='input'>"
  			html = html + "</div>"
		}else if(question.type.equals("MC")){
			html = html+"<div id='radio'>"
			var i = 0;
			while(i<ans.length){
				html=html+"<input type='radio' id='radio"+i+"' name='"+ans.get(i)+"' style='margin-left:10px;'>
							<label for='radio"+i+"' style='margin-right:10px;'>"+ans.get(i)+"</label> "
				i = i + 1
			}
			html = html+"</div>"
		}else if(question.type.equals("MMC")){
			html = html+"<div id='check'>"
			var i = 0;
			while(i<ans.length){
				html = html+"<input type='checkbox' id='check"+i+"' name='"+ans.get(i)+"' style='margin-left:10px;'>
							<label for='check"+i+"' style='margin-right:10px;'>"+ans.get(i)+"</label> "
			    i = i + 1
			}
			html = html+"</div>"
		}
		//Buttons added in JS
		html = html+"</form>"
		return html
	}
	
	
	def static nextQuestion(String answer){
		var timeTaken = System.currentTimeMillis()-timestampStart
		var Answer ans = new Answer(questions.get(questionNr).questionID, answer, timeTaken)
		answers.add(ans)
		
		if(questionNr == questions.size()-1){
			ExperimentJS::commentDialog("Todo, load other dialog to new method, end experiment, thank you")
			//ExperimentJS::changeQuestionDialog("Todo, load other dialog to new method, end experiment, thank you")
			
		}else{
			//if not last question
			Logging.log("Goto next question")
			questionNr = questionNr + 1
			var form = getQuestionBox(questions.get(questionNr))
			timestampStart = System.currentTimeMillis()
			ExperimentJS::changeQuestionDialog(form)
		}
	}
	
	def static savePersonalInformation(String answer){
		
	}
	
	def static saveComments(String answer){
		//TODO save answer
		ExperimentJS::closeQuestionDialog()
		
		
	}
	

}