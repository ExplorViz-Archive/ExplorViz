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
import explorviz.visualization.experiment.callbacks.ZipCallback
import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.login.LoginServiceAsync
import explorviz.visualization.login.LoginService
import explorviz.visualization.main.LogoutCallBack
import explorviz.visualization.experiment.callbacks.SkipCallback
import explorviz.visualization.experiment.callbacks.QuestionTimeCallback
import explorviz.visualization.main.ExplorViz

class Questionnaire {
	static int questionNr = 0
	static boolean answeredPersonal = false
	static long timestampStart
	public static List<String> personalVocab = new ArrayList<String>()
	public static List<String> commentVocab = new ArrayList<String>()
	public static List<Question> questions = new ArrayList<Question>()
	public static List<Answer> answers = new ArrayList<Answer>()
	static String userID
	var static QuestionServiceAsync questionService 
	static var formDiv = "<div class='form-group' id='form-group'>"
	static var closeDiv = "</div>"
	public static String language = "" 
	public static boolean allowSkip = false
	public static int questionMaxTime = 8
	public static QuestionTimer qTimer

	def static void startQuestions(){
		questionService = getQuestionService()
		if(questionNr == 0 && !answeredPersonal){
			//start new experiment
			questionService.getLanguage(new LanguageCallback())
			questionService.getVocabulary(new VocabCallback())
			questionService.getQuestionTime(new QuestionTimeCallback())
			questionService.getQuestions(new QuestionsCallback())
			questionService.allowSkip(new SkipCallback())
			userID = AuthorizationService.getCurrentUsername()
			qTimer = new QuestionTimer(questionMaxTime)
			if(userID.equals("")){
				userID = "DummyUser"
			}
		}else{
			//continue experiment
			var form = getQuestionBox(questions.get(questionNr))
			questionService.setMaxTimestamp(questions.get(questionNr).timeframeEnd, new VoidCallback())
			timestampStart = System.currentTimeMillis()
			var caption = "Question "+questionNr.toString + " of "+ questions.size()
			ExperimentJS::changeQuestionDialog(form, language, caption, allowSkip)
		}
		timestampStart = System.currentTimeMillis()
		if(ExplorViz.isExtravisEnabled()){
			ExperimentJS::showQuestionDialogExtraVis()
		}else{
			ExperimentJS::showQuestionDialog()
		}
	}

	def static getQuestionService(){
		val QuestionServiceAsync questionService = GWT::create(typeof(QuestionService))
		val endpoint = questionService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "questionservice"
		return questionService
	}
	
	def static showPersonalDataDialog(List<String> personalVocab) {
		ExperimentJS.personalDataDialog(Questionnaire::getPersonalInformationBox(personalVocab), language)
	}
	
	def static getPersonalInformationBox(List<String> vocab){
		var StringBuilder html = new StringBuilder()
		html.append("<p>"+vocab.get(0)+"</p>")
		html.append("<form class='form' style='width:300px;' role='form' id='questionForm'>")
		//Age-input
		html.append(formDiv+"<label for='ageForm'>"+vocab.get(1)+"</label>
					    <div class='input-group' id='ageForm'>
					       <input type='number' min='18' max='70' class='form-control' placeholder='"+vocab.get(1)+"' name='age' required>
					       <span class='input-group-addon'>"+vocab.get(2)+"</span></div>
					"+closeDiv)
		//Gender-choice
		html.append(formDiv+"<label for='genderForm'>"+vocab.get(3)+"</label>
					    <select class='form-control' id='genderForm' name='gender' required>
					      <option>"+vocab.get(4)+"</option>
					      <option>"+vocab.get(5)+"</option>
					    </select>"+closeDiv)
		//Degree-choice
		html.append(formDiv+"<label for='degreeForm'>"+vocab.get(6)+"</label>
			    <select class='form-control' id='degreeForm' name='degree' required>
			      <option>"+vocab.get(7)+"</option>
			      <option>"+vocab.get(8)+"</option>
				  <option>"+vocab.get(9)+"</option>
			      <option>"+vocab.get(10)+"</option>
				  <option>"+vocab.get(11)+"</option>
			    </select>"+closeDiv)
		//semester-input
		html.append(formDiv+"<label for='semesterInput'>"+vocab.get(28)+"</label>
					  <input type='number' id='semesterInput' min='0' max='50' class='form-control' name='semester' required>
					"+closeDiv)
		//Affiliation-choice
		html.append(formDiv+"<label for='affForm'>"+vocab.get(29)+"</label>
			    <select class='form-control' id='affForm' name='affiliation' required>
			      <option>"+vocab.get(30)+"</option>
			      <option>"+vocab.get(31)+"</option>
				  <option>"+vocab.get(32)+"</option>
			    </select>"+closeDiv)
		//Colourblindness
		html.append(formDiv+"<div id='radio' class='input-group'>")
		html.append("<p>"+vocab.get(25)+"</p>")
		html.append("<input type='radio' id='yes' name='colour' value='"+vocab.get(26)+"' style='margin-left:10px;' required>
						<label for='yes' style='margin-right:15px; margin-left:5px'>"+vocab.get(26)+"</label> ")
		html.append("<input type='radio' id='no' name='colour' value='"+vocab.get(27)+"' style='margin-left:10px;' required>
						<label for='no' style='margin-right:15px; margin-left:5px'>"+vocab.get(27)+"</label> ")
		html.append("</div>"+closeDiv)		
		html.append("</form>")
		return html.toString()
	}
	
	def static savePersonalInformation(String answer){
		var StringBuilder answerString = new StringBuilder()
		var String[] answerList = answer.split("&")
		var String s;
		for(var int i = 0; i < answerList.length; i++){
			s = answerList.get(i)
			s = cleanInput(s.substring(s.indexOf("=")+1))
			answerString.append(s)
			if(i + 1 == answerList.length){
				answerString.append("\n")
			}else{
				answerString.append(",")
			}
		}
		questionService.writeStringAnswer(answerString.toString(),userID, new VoidCallback())
		
		ExperimentJS::experienceDataDialog(getExperienceInformationBox(), language)
	}
	
	def static getExperienceInformationBox(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' style='width:300px;' role='form' id='questionForm'>")
		//Experience Java or similar OOP
		html.append(formDiv+"<label for='exp1form'>"+personalVocab.get(12)+"</label> 
				<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+personalVocab.get(13)+"'></span>
			    <select class='form-control' id='exp1Form' name='exp1' required>
			      <option>"+personalVocab.get(20)+"</option>
			      <option>"+personalVocab.get(21)+"</option>
				  <option>"+personalVocab.get(22)+"</option>
			      <option>"+personalVocab.get(23)+"</option>
				  <option>"+personalVocab.get(24)+"</option>
			    </select>"+closeDiv)
		//Experience with Dynamic Analysis
		html.append(formDiv+"<label for='exp2Form'>"+personalVocab.get(14)+"</label> 
				<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+personalVocab.get(15)+"'></span>
			    <select class='form-control' id='exp2Form' name='exp2' required>
			      <option>"+personalVocab.get(20)+"</option>
			      <option>"+personalVocab.get(21)+"</option>
				  <option>"+personalVocab.get(22)+"</option>
			      <option>"+personalVocab.get(23)+"</option>
				  <option>"+personalVocab.get(24)+"</option>
			    </select>"+closeDiv)	
		//Experience with ExplorViz/ExtraVis
		if(ExplorViz.isExtravisEnabled){
			personalVocab.set(16, "Experience with Extravis")
		}
		html.append(formDiv+"<label for='exp3Form'>"+personalVocab.get(16)+"</label> 
				<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+personalVocab.get(17)+"'></span>
			    <select class='form-control' id='exp3Form' name='exp3' required>
			      <option>"+personalVocab.get(20)+"</option>
			      <option>"+personalVocab.get(21)+"</option>
				  <option>"+personalVocab.get(22)+"</option>
			      <option>"+personalVocab.get(23)+"</option>
				  <option>"+personalVocab.get(24)+"</option>
			    </select>"+closeDiv)	
		//Experience with Program
		html.append(formDiv+"<label for='exp4Form'>"+personalVocab.get(18)+"</label> 
				<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+personalVocab.get(19)+"'></span>
			    <select class='form-control' id='exp4Form' name='exp4' required>
			      <option>"+personalVocab.get(20)+"</option>
			      <option>"+personalVocab.get(21)+"</option>
				  <option>"+personalVocab.get(22)+"</option>
			      <option>"+personalVocab.get(23)+"</option>
				  <option>"+personalVocab.get(24)+"</option>
			    </select>"+closeDiv)
		html.append("</form>")
		return html.toString()
	}
	
	def static saveExperienceInformation(String answer){
		var StringBuilder answerString = new StringBuilder()
		var String[] answerList = answer.split("&")
		var String s;
		for(var int i = 0; i < answerList.length; i++){
			s = answerList.get(i)
			s = cleanInput(s.substring(s.indexOf("=")+1))
			answerString.append(s)
			if(i + 1 == answerList.length){
				answerString.append("\n")
			}else{
				answerString.append(",")
			}
		}
		questionService.writeStringAnswer(answerString.toString(),userID, new VoidCallback())
		answeredPersonal = true
		//start questionnaire
		var caption = "Question "+(questionNr+1).toString + " of "+ questions.size()
		if(!ExplorViz.isExtravisEnabled()){
			questionService.setMaxTimestamp(questions.get(questionNr).timeframeEnd, new VoidCallback())
		}
		qTimer.setTime(System.currentTimeMillis())
		qTimer.scheduleRepeating(1000)		
		ExperimentJS::changeQuestionDialog(getQuestionBox(questions.get(questionNr)), language, caption, allowSkip)
	}
	
	def static getQuestionBox(Question question){
		var StringBuilder html = new StringBuilder()
		html.append("<p>"+question.text+"</p>")
		html.append("<form class='form' role='form' id='questionForm'>")	
		var String[]  ans = question.answers
		html.append(formDiv)
		if(question.type.equals("Free")){
			html.append("<label for='input'>Answer</label>")
			html.append("<div id='input' class='input-group'>")
			if(question.freeAnswers > 1){
				for(var i = 0; i < question.freeAnswers; i++){
		    		html.append("<input type='text' class='form-control' id='input"+i.toString()+"' placeholder='Enter Answer' name='input"+i.toString()+"' minlength='1' autocomplete='off' required>")
	  			}
  			}else{ //only one question gets a textbox
  				html.append("<textarea class='form-control questionTextarea' id='input1' name='input1' rows='2' required></textarea>")
  			}
  			html.append("</div>")
		}else if(question.type.equals("MC")){
			html.append("<div id='radio' class='input-group'>")
			var i = 0;
			while(i<ans.length){
				html.append("<input type='radio' id='radio"+i+"' name='radio' value='"+ans.get(i)+"' style='margin-left:10px;' required>
							<label for='radio"+i+"' style='margin-right:15px; margin-left:5px'>"+ans.get(i)+"</label> ")
				i = i + 1
			}
			html.append("</div>")
		}else if(question.type.equals("MMC")){
			html.append("<div id='check' class='input-group'>")
			var i = 0;
			while(i<ans.length){
				html.append("<input type='checkbox' id='check"+i+"' name='check' value='"+ans.get(i)+"' style='margin-left:10px;'>
							<label for='check"+i+"' style='margin-right:15px; margin-left:5px'>"+ans.get(i)+"</label> ")
			    i = i + 1
			}
			html.append("</div>")
		}
		html.append(closeDiv)
		html.append("</form>")
		return html.toString()
	}
	
	def static nextQuestion(String answer){
		var newTime = System.currentTimeMillis()
		var timeTaken = newTime-timestampStart
		var Answer ans = new Answer(questions.get(questionNr).questionID, cleanInput(answer), timeTaken, timestampStart, newTime, userID)
		answers.add(ans)
		questionService.writeAnswer(ans, new VoidCallback())
		
		if(questionNr == questions.size()-1){
			ExperimentJS::tutorialCommentDialog(getTutorialCommentBox(), language)
			qTimer.cancel()
			ExperimentJS::hideTimer()
			questionNr = 0
		}else{
			//if not last question
			questionNr = questionNr + 1
			var form = getQuestionBox(questions.get(questionNr))
			if(!ExplorViz.isExtravisEnabled()){
				questionService.setMaxTimestamp(questions.get(questionNr).timeframeEnd, new VoidCallback())
			}
			timestampStart = System.currentTimeMillis()
			qTimer.setTime(timestampStart)
			var caption = "Question "+(questionNr+1).toString + " of "+ questions.size()
			ExperimentJS::changeQuestionDialog(form, language, caption, allowSkip)
		}
	}
	
	def static getTutorialCommentBox(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		html.append(formDiv+"<label for='timeForm'>"+commentVocab.get(0)+"</label> 
					<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(1)+"'></span>
					<select class='form-control' id='timeForm' name='time' required>
						<option>1</option>	
						<option>2</option>
						<option>3</option>
						<option>4</option>
						<option>5</option>
					</select>"+closeDiv)
		html.append(formDiv+"<label for='speedForm'>"+commentVocab.get(2)+"</label> 
					<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(3)+"'></span>
					<select class='form-control' id='speedForm' name='speed' required>
						<option>1</option>	
						<option>2</option>
						<option>3</option>
						<option>4</option>
						<option>5</option>
					</select>"+closeDiv)
		html.append("<label for='toolForm'>"+commentVocab.get(4)+"</label>
			<textarea class='form-control closureTextarea' id='toolForm' name='tool'></textarea>
		")
		html.append(formDiv+"<label for='tutHelpForm'>"+commentVocab.get(5)+"</label> 
			<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(6)+"'></span>
			<select class='form-control' id='tutHelpForm' name='tuthelp' required>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>"+closeDiv)
		html.append(formDiv+"<label for='tutTimeForm'>"+commentVocab.get(7)+"</label> 
			<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(8)+"'></span>
			<select class='form-control' id='tutTimeForm' name='tuttime' required>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>"+closeDiv)
		html.append("<label for='tutCommentForm'>"+commentVocab.get(9)+"</label>
			<textarea class='form-control closureTextarea' id='tutCommentForm' name='tutComment'></textarea>
		")
		html.append("</form>")
		return html.toString()
	}
	
		def static saveTutorialComments(String answer){
		var StringBuilder answerString = new StringBuilder()
		var String[] answerList = answer.split("&")
		var String s;
		for(var int i = 0; i < answerList.length; i++){
			s = answerList.get(i)
			s = cleanInput(s.substring(s.indexOf("=")+1))
			answerString.append(s)
			if(i + 1 == answerList.length){
				answerString.append("\n")
			}else{
				answerString.append(",")
			}
		}
		questionService.writeStringAnswer(answerString.toString(),userID, new VoidCallback())
		ExperimentJS::explorvizCommentDialog(getExplorVizCommentBox(), language)
	}
	
	def static getExplorVizCommentBox(){
		var StringBuilder html = new StringBuilder()
		html.append("<p>"+commentVocab.get(10)+"</p>")
		html.append("<form class='form' role='form' id='questionForm'>")
		html.append(formDiv+"<label for='T1Form'>"+commentVocab.get(11)+"</label>
			<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(17)+"'></span>
			<select class='form-control' id='T1Form' name='T1' required>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>"+closeDiv)
		html.append(formDiv+"<label for='T2Form'>"+commentVocab.get(12)+"</label>
			<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(17)+"'></span>
			<select class='form-control' id='T2Form' name='T2' required>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>"+closeDiv)
		html.append(formDiv+"<label for='T3Form'>"+commentVocab.get(13)+"</label>
			<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(17)+"'></span>
			<select class='form-control' id='T3Form' name='T3' required>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>"+closeDiv)
		html.append(formDiv+"<label for='T4Form'>"+commentVocab.get(14)+"</label>
			<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(17)+"'></span>
			<select class='form-control' id='T4Form' name='T4' required>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>"+closeDiv)
		html.append(formDiv+"<label for='T5Form'>"+commentVocab.get(15)+"</label>
			<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(17)+"'></span>
			<select class='form-control' id='T5Form' name='T5' required>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>"+closeDiv)
		html.append(formDiv+"<label for='T6Form'>"+commentVocab.get(16)+"</label>
			<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+commentVocab.get(17)+"'></span>
			<select class='form-control' id='T6Form' name='T6' required>
				<option>1</option>	
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
			</select>"+closeDiv)
		html.append("</form>")
		return html.toString()
	}
	
	def static saveExplorVizComments(String answer){
		var StringBuilder answerString = new StringBuilder()
		var String[] answerList = answer.split("&")
		var String s;
		for(var int i = 0; i < answerList.length; i++){
			s = answerList.get(i)
			s = cleanInput(s.substring(s.indexOf("=")+1))
			answerString.append(s)
			if(i + 1 == answerList.length){
				answerString.append("\n")
			}else{
				answerString.append(",")
			}
		}
		questionService.writeStringAnswer(answerString.toString(),userID, new VoidCallback())
		ExperimentJS::finishQuestionnaireDialog("<p>"+commentVocab.get(19)+"</p>")
	}
	
	def static finishQuestionnaire(){
		ExperimentJS::closeQuestionDialog()
		val LoginServiceAsync loginService = GWT::create(typeof(LoginService))
		val endpoint = loginService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "loginservice"
		loginService.logout(new LogoutCallBack)
	}
	
	def static downloadAnswers() {
		if(questionService==null){
			questionService = getQuestionService()
		}
		questionService.downloadAnswers(new ZipCallback())
	}
	
	def static cleanInput(String s){
		var cleanS = s.replace("+"," ").replace("%40","@").replace("%0D%0A"," ") //+,@,enter
		cleanS = cleanS.replace("%2C",",").replace("%3B",";").replace("%3A",":")//,, ;, :,
		cleanS = cleanS.replace("%C3%A4","ä").replace("%C3%BC","ü").replace("%C3%B6","ö").replace("%C3%9F","ß")// ä, ü, ö, ß
		cleanS = cleanS.replace("%C3%84","Ä").replace("%C3%9C","Ü").replace("%C3%96","Ö")//Ä, Ü, Ö 
		cleanS = cleanS.replace("%26","&").replace("%3F","?").replace("%22","\"")// &, ? , " 
		cleanS = cleanS.replace("%7B","{").replace("%7D","}").replace("%2F","/")//{,},/
		cleanS = cleanS.replace("%5B","[").replace("%5D","]").replace("%5C","\\")// [, ], \
		cleanS = cleanS.replace("%23","#") // #
		return cleanS
	}
}

class LanguageCallback implements AsyncCallback<String> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(String result) {
		Questionnaire.language = result
	}
	
}