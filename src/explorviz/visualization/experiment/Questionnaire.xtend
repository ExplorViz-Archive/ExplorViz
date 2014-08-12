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
import explorviz.visualization.experiment.callbacks.DialogCallback
import explorviz.visualization.experiment.callbacks.ZipCallback
import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.login.LoginServiceAsync
import explorviz.visualization.login.LoginService
import explorviz.visualization.main.LogoutCallBack
import explorviz.visualization.experiment.callbacks.SkipCallback
import explorviz.visualization.main.ExplorViz
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.shared.experiment.StatisticQuestion
import explorviz.visualization.experiment.callbacks.EmptyLandscapeCallback

class Questionnaire {
	static int questionNr = 0
	static boolean answeredPersonal = false
	static long timestampStart
	static List<StatisticQuestion> dialog1 = new ArrayList<StatisticQuestion>()
	static List<StatisticQuestion> dialog2 = new ArrayList<StatisticQuestion>()
	static List<StatisticQuestion> dialog3 = new ArrayList<StatisticQuestion>()
	static List<StatisticQuestion> dialog4 = new ArrayList<StatisticQuestion>()
	static List<StatisticQuestion> dialog5 = new ArrayList<StatisticQuestion>()
	static List<StatisticQuestion> dialog6 = new ArrayList<StatisticQuestion>()
	public static List<Question> questions = new ArrayList<Question>()
	public static List<Answer> answers = new ArrayList<Answer>()
	static String userID
	var static QuestionServiceAsync questionService 
	public static String language = "" 
	public static boolean allowSkip = false
	public static QuestionTimer qTimer

	def static void startQuestions(){
		questionService = getQuestionService()
		if(questionNr == 0 && !answeredPersonal){
			//start new experiment
			questionService.getLanguage(new LanguageCallback())
			
			if(ExplorViz::isExtravisEnabled){
				questionService.getExtravisVocabulary(new DialogCallback())
			}else{
				questionService.getVocabulary(new DialogCallback())
			}
			questionService.getQuestions(new QuestionsCallback())
			questionService.allowSkip(new SkipCallback())
			userID = AuthorizationService.getCurrentUsername()
			qTimer = new QuestionTimer(8)
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
	
	def static showFirstDialog(List<StatisticQuestion> d1, List<StatisticQuestion> d2, List<StatisticQuestion> d3,
		List<StatisticQuestion> d4, List<StatisticQuestion> d5, List<StatisticQuestion> d6) {
		dialog1 = d1
		dialog2 = d2
		dialog3 = d3
		dialog4 = d4
		dialog5 = d5
		dialog6 = d6
		ExperimentJS.showFirstDialog(Questionnaire::getFirstForm(), language)
	}
	
	def static getFirstForm(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		for(var i = 0; i<dialog1.size(); i++){
			html.append(dialog1.get(i).getHTML())
		}	
		html.append("</form>")
		return html.toString()
	}
	
	def static saveFirstForm(String answer){
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
		
		ExperimentJS::showSecondDialog(getSecondForm(), language)
	}
	
	def static getSecondForm(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		for(var i = 0; i<dialog2.size(); i++){
			html.append(dialog2.get(i).getHTML())
		}	
		html.append("</form>")
		return html.toString()
	}
	
	def static saveSecondForm(String answer){
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
		if(!ExplorViz::isExtravisEnabled){
			LandscapeExchangeManager::fetchSpecificLandscape(questions.get(0).timeframeEnd.toString())
		}
		questionService.writeStringAnswer(answerString.toString(),userID, new VoidCallback())
		answeredPersonal = true		
		ExperimentJS::showThirdDialog(getThirdForm())
	}
	
	def static getThirdForm(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		for(var i = 0; i<dialog3.size(); i++){
			html.append(dialog3.get(i).getHTML())
		}	
		html.append("</form>")
		return html.toString()
	}
	
	def static introQuestionnaire(){
		//start questionnaire
		var caption = "Question "+(questionNr+1).toString + " of "+ questions.size()
		if(!ExplorViz.isExtravisEnabled()){
			questionService.setMaxTimestamp(questions.get(questionNr).timeframeEnd, new VoidCallback())
		}
		qTimer.setTime(System.currentTimeMillis())
		qTimer.setMaxTime(questions.get(questionNr).worktime)
		qTimer.scheduleRepeating(1000)		
		ExperimentJS::changeQuestionDialog(getQuestionBox(questions.get(questionNr)), language, caption, allowSkip)
	}
	
	def static getQuestionBox(Question question){
		var StringBuilder html = new StringBuilder()
		html.append("<p>"+question.text+"</p>")
		html.append("<form class='form' role='form' id='questionForm'>")	
		var String[]  ans = question.answers
		html.append("<div class='form-group' id='form-group'>")
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
		html.append("</div>")
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
			SceneDrawer::lastViewedApplication = null
			questionService.getEmptyLandscape(new EmptyLandscapeCallback())
			ExperimentJS::showForthDialog(getForthForm(), language)
			qTimer.cancel()
			ExperimentJS::hideTimer()
			questionNr = 0
		}else{
			//if not last question
			ExperimentJS::hideTimer()
			questionNr = questionNr + 1
			var form = getQuestionBox(questions.get(questionNr))
			if(!ExplorViz.isExtravisEnabled()){
				questionService.setMaxTimestamp(questions.get(questionNr).timeframeEnd, new VoidCallback())
			}
			timestampStart = System.currentTimeMillis()
			qTimer.setTime(timestampStart)
			qTimer.setMaxTime(questions.get(questionNr).worktime)
			var caption = "Question "+(questionNr+1).toString + " of "+ questions.size()
			ExperimentJS::changeQuestionDialog(form, language, caption, allowSkip)
		}
	}
	
	def static getForthForm(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		for(var i = 0; i<dialog4.size(); i++){
			html.append(dialog4.get(i).getHTML())
		}	
		html.append("</form>")
		return html.toString()
	}
	
	def static saveForthForm(String answer){
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
		ExperimentJS::showFifthDialog(getFifthForm(), language)
	}
	
	def static getFifthForm(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		for(var i = 0; i<dialog5.size(); i++){
			html.append(dialog5.get(i).getHTML())
		}	
		html.append("</form>")
		return html.toString()
	}
	
	def static saveFifthForm(String answer){
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
		ExperimentJS::finishQuestionnaireDialog(getSixthForm())
	}
	
	def static getSixthForm(){
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		for(var i = 0; i<dialog6.size(); i++){
			html.append(dialog6.get(i).getHTML())
		}	
		html.append("</form>")
		return html.toString()
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
		cleanS = cleanS.replace("%2C","U+002C").replace("%3B","U+003B").replace("%3A","U+003A")//,, ;, :,
		cleanS = cleanS.replace("%C3%A4","U+00E4").replace("%C3%BC","U+00FC").replace("%C3%B6","U+00F6").replace("%C3%9F","U+00DF")// ä, ü, ö, ß
		cleanS = cleanS.replace("%C3%84","U+00C4").replace("%C3%9C","U+00DC").replace("%C3%96","U+00D6")//Ä, Ü, Ö 
		cleanS = cleanS.replace("%26","U+0026").replace("%3F","U+003F").replace("%22","U+0022")// &, ? , " 
		cleanS = cleanS.replace("%7B","U+007B").replace("%7D","U+007D").replace("%2F","U+002F")//{,},/
		cleanS = cleanS.replace("%5B","U+005B").replace("%5D","U+005D").replace("%5C","U+005C")// [, ], \
		cleanS = cleanS.replace("%23","U+0023") // #
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