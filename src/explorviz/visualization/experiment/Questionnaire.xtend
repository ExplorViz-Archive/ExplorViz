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
import explorviz.visualization.services.AuthorizationService
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.experiment.callbacks.VocabCallback

class Questionnaire {
	static int questionNr = 0
	static long timestampStart
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
		if(userID.equals("")){
			userID = "DummyUser"
		}
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
		html.append("<p>"+vocab.get(0)+"</p>")
		html.append("<form class='form' role='form' id='questionForm' data-toggle='validator'>
						<div class='form-group'>")
		//Age-input
		html.append("<label for='ageForm'>"+vocab.get(1)+"</label>
					    <div class='input-group' id='ageForm'>
					      <input type='number' class='form-control' placeholder='"+vocab.get(1)+"' name='age' required>
					      <span class='input-group-addon'>"+vocab.get(2)+"</span>
					    </div>")
		//Gender-choice
		html.append("<label for='genderForm'>"+vocab.get(3)+"</label>
					    <select class='form-control' id='genderForm' name='gender'>
					      <option>"+vocab.get(4)+"</option>
					      <option>"+vocab.get(5)+"</option>
					    </select>") //male female
		//Degree-choice
		html.append("<label for='degreeForm'>"+vocab.get(6)+"</label>
			    <select class='form-control' id='degreeForm' name='degree'>
			      <option>"+vocab.get(7)+"</option>
			      <option>"+vocab.get(8)+"</option>
				  <option>"+vocab.get(9)+"</option>
			      <option>"+vocab.get(10)+"</option>
			    </select>")
		//Experience ExplorViz
		html.append("<label for='exp1form'>"+vocab.get(11)+"</label>
			    <select class='form-control' id='exp1Form' name='exp1'>
			      <option>"+vocab.get(12)+"</option>
			      <option>"+vocab.get(13)+"</option>
				  <option>"+vocab.get(14)+"</option>
			      <option>"+vocab.get(15)+"</option>
			    </select>")
		html.append("<label for='exp2Form'>"+vocab.get(16)+"</label>
			    <select class='form-control' id='exp2Form' name='exp2'>
			      <option>"+vocab.get(17)+"</option>
			      <option>"+vocab.get(18)+"</option>
				  <option>"+vocab.get(19)+"</option>
			      <option>"+vocab.get(20)+"</option>
			    </select>")	
		html.append("</div></form>")
		return html.toString()
	}
	
	def static getCommentBox(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>
						<div class='form-group'>")
		html.append("<label for='difficultyForm'>"+commentVocab.get(0)+"</label>
					<select class='form-control' id='difficultyForm' name='difficulty'>
						<option>1</option>	
						<option>2</option>
						<option>3</option>
						<option>4</option>
						<option>5</option>
					</select>")
		html.append("<label for='tutHelpForm'>"+commentVocab.get(1)+"</label>
			<select class='form-control' id='tutHelpForm' name='tuthelp'>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>")
		html.append("<label for='tutCommentForm'>"+commentVocab.get(2)+"</label>
			<textarea class='form-control' id='tutCommentForm' name='tutComment' rows='3'></textarea>
		")
		html.append("<label for='questHelpForm'>"+commentVocab.get(3)+"</label>
			<select class='form-control' id='questHelpForm' name='questhelp'>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>")
		html.append("<label for='questCommentForm'>"+commentVocab.get(4)+"</label>
			<textarea class='form-control' id='questCommentForm' name='questComment' rows='3'></textarea>
		")
		html.append("<label for='otherCommentForm'>"+commentVocab.get(5)+"</label>
			<textarea class='form-control' id='otherCommentForm' name='otherComment' rows='3'></textarea>
		")
		
		html.append("</div></form>")
		return html.toString()
	}

	def static getQuestionBox(Question question){
		var StringBuilder html = new StringBuilder()
		html.append("<p>"+question.text+"</p>")
		html.append("<form class='form' role='form' id='questionForm'>")
		//html.append("<form class='form-inline' role='form' id='questionForm'>")
		
		var String[]  ans = question.answers
		if(question.type.equals("Free")){
			html.append("<div class='form-group'>")
			var i = 0
			while(i < question.freeAnswers){
	    		html.append("<label class='sr-only' for='answer'>Answer</label>
							   <input type='text' class='form-control' id='input' placeholder='Enter Answer' name='input'>")
				i = i + 1
  			}
  			html.append("</div>")
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
		html.append("</form>")
		return html.toString()
	}
	
	
	def static nextQuestion(String answer){
		var newTime = System.currentTimeMillis()
		var timeTaken = newTime-timestampStart
		var Answer ans = new Answer(questions.get(questionNr).questionID, answer, timeTaken, timestampStart, newTime, userID)
		answers.add(ans)
		questionService.writeAnswer(ans, new VoidCallback())
		
		if(questionNr == questions.size()-1){
			ExperimentJS::commentDialog(getCommentBox())
		}else{
			//if not last question
			questionNr = questionNr + 1
			var form = getQuestionBox(questions.get(questionNr))
			timestampStart = System.currentTimeMillis()
			ExperimentJS::changeQuestionDialog(form)
		}
	}
	
	def static savePersonalInformation(String answer){
		var StringBuilder answerString = new StringBuilder()
		var String[] answerList = answer.split("&")
		var String age = answerList.get(0) //only field that can be empty, check if empty
		if(age.length > 4){
			answerString.append(age.substring(4))
		}else{
			answerString.append("")
		}
		answerString.append(",")
		answerString.append(answerList.get(1).substring(7))
		answerString.append(",")
		answerString.append(answerList.get(2).substring(7).replace("+"," "))
		answerString.append(",")
		answerString.append(answerList.get(3).substring(5).replace("+"," "))
		answerString.append(",")
		answerString.append(answerList.get(4).substring(5).replace("+"," "))
		answerString.append("\n")
		questionService.writeString(answerString.toString(),userID, new VoidCallback())		
		
		//start questionnaire
		ExperimentJS::changeQuestionDialog(getQuestionBox(questions.get(0)))
	}
	
	def static saveComments(String answer){
		var StringBuilder answerString = new StringBuilder()
		var String[] answerList = answer.split("&")
		answerString.append(answerList.get(0).substring(11)) //difficulty
		answerString.append(",")
		answerString.append(answerList.get(1).substring(8)) //tutorial help
		answerString.append(",")
		var comment = answerList.get(2) //comments can be empty
		if(comment.length > 11){ //tutorial comment
			answerString.append(comment.substring(11).replace("+"," "))
		}else{
			answerString.append("")
		}
		answerString.append(",")
		answerString.append(answerList.get(3).substring(10)) //questionnaire help
		answerString.append(",")
		comment = answerList.get(4)
		if(comment.length > 13){ //questionnaire comment
			answerString.append(comment.substring(13).replace("+"," "))
		}else{
			answerString.append("")
		}
		answerString.append(",")
		comment = answerList.get(5)
		if(comment.length > 13){ //other comments
			answerString.append(comment.substring(13).replace("+"," "))
		}else{
			answerString.append("")
		}
		answerString.append("\n")
		questionService.writeString(answerString.toString(),userID, new VoidCallback())

		ExperimentJS::closeQuestionDialog()
		
		
	}
	

}