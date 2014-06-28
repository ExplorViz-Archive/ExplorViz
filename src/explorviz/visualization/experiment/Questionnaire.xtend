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
import explorviz.visualization.services.AuthorizationService
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.experiment.callbacks.VocabCallback

class Questionnaire {
	static int questionNr = 0
	static long timestampStart
	public static List<String> personalVocab = new ArrayList<String>()
	public static List<String> commentVocab = new ArrayList<String>()
	public static List<Question> questions = new ArrayList<Question>()
	public static List<Answer> answers = new ArrayList<Answer>()
	static String userID
	var static QuestionServiceAsync questionService 

	def static startQuestions(){
		questionService = getQuestionService()
		questionService.getVocabulary(new VocabCallback())
		questionService.getQuestions(new QuestionsCallback())
		userID = AuthorizationService.getCurrentUsername()
		timestampStart = System.currentTimeMillis()
		ExperimentJS::showQuestionDialog()
	}

	def static getQuestionService(){
		val QuestionServiceAsync questionService = GWT::create(typeof(QuestionService))
		val endpoint = questionService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "questionservice"
		return questionService
	}
	
	def static getPersonalInformationBox(List<String> vocab){
		var StringBuilder html = new StringBuilder()
		
		
		
		return html.toString()
	}

	def static getQuestionBox(Question question){
		var StringBuilder html = new StringBuilder()
		html.append("<p>"+question.text+"</p>")
		html.append("<form class='form-inline' role='form' id='questionForm'>")
		var String[]  ans = question.answers
		if(question.type.equals("Free")){
			html.append("<div class='form-group'>")
			var i = 0
			while(i < question.freeAnswers){
				Logging.log("building inputs")
	    		html.append("<label class='sr-only' for='answer'>Answer</label>
							   <input type='text' class='form-control' id='input' placeholder='Enter Answer' name='input'>")
				i = i + 1
  			}
  			html.append("</div>")
  			Logging.log("finished inputs")
		}else if(question.type.equals("MC")){
			html.append("<div id='radio' class='input-group'>")
			var i = 0;
			while(i<ans.length){
				html.append("<input type='radio' id='radio"+i+"' name='radio' value='"+ans.get(i)+"' style='margin-left:10px;'>
							<label for='radio"+i+"' style='margin-right:10px;'>"+ans.get(i)+"</label> ")
				i = i + 1
			}
			html.append("</div>")
		}else if(question.type.equals("MMC")){
			html.append("<div id='check' class='input-group'>")
			var i = 0;
			while(i<ans.length){
				html.append("<input type='checkbox' id='check"+i+"' name='check' value='"+ans.get(i)+"' style='margin-left:10px;'>
							<label for='check"+i+"' style='margin-right:10px;'>"+ans.get(i)+"</label> ")
			    i = i + 1
			}
			html.append("</div>")
		}
		//Buttons added in JS
		html.append("</form>")
		Logging.log("Questionhtml constructed")
		return html.toString()
	}
	
	
	def static nextQuestion(String answer){
		var timeTaken = System.currentTimeMillis()-timestampStart
		var Answer ans = new Answer(questions.get(questionNr).questionID, answer, timeTaken, userID)
		answers.add(ans)
		questionService.writeAnswer(ans, new VoidCallback())
		
		if(questionNr == questions.size()-1){
			ExperimentJS::commentDialog("Todo, load other dialog to new method, end experiment, thank you")			
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
		//save answer
		
		
		//start questionnaire
		ExperimentJS::changeQuestionDialog(getQuestionBox(questions.get(0)))
	}
	
	def static saveComments(String answer){
		//TODO save answer
		ExperimentJS::closeQuestionDialog()
		
		
	}
	

}