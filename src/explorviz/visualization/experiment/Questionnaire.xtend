package explorviz.visualization.experiment

import java.util.ArrayList
import explorviz.shared.experiment.Question
import explorviz.shared.experiment.Answer
import java.util.List
import explorviz.visualization.experiment.services.QuestionServiceAsync
import explorviz.visualization.services.AuthorizationService
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.experiment.callbacks.ZipCallback
import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.main.LogoutCallBack
import explorviz.visualization.experiment.callbacks.SkipCallback
import explorviz.visualization.main.ExplorViz
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.experiment.callbacks.EmptyLandscapeCallback
import explorviz.visualization.main.Util
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.experiment.callbacks.GenericFuncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.engine.Logging
import explorviz.shared.experiment.Prequestion
import explorviz.shared.experiment.Postquestion


/**
 * @author Santje Finke
 * 
 */
class Questionnaire {
	static int questionNr = 0
	static boolean answeredPersonal = false
	static long timestampStart
	static ArrayList<Prequestion> preDialog;
	static ArrayList<Postquestion> postDialog;
	public static List<Question> questions = new ArrayList<Question>()
	public static List<Answer> answers = new ArrayList<Answer>()
	static String userID
	var static QuestionServiceAsync questionService
	public static String language = ""
	public static boolean allowSkip = false
	public static QuestionTimer qTimer

	var static JSONServiceAsync jsonService
	public static String experimentFilename = null
	private static String experimentName = null
	
	public static boolean preAndPostquestions = false
	public static boolean eyeTracking = false
	public static boolean screenRecording = false

	/**
	 * Setups services and variables from server and starts questionnaire
	 */
	def static void startQuestions() {
		
		if(experimentFilename == null)
			return;
		
		jsonService = Util::getJSONService()
		questionService = Util::getQuestionService()
		
		if (questionNr == 0 && !answeredPersonal) {
			// start new experiment
			questionService.getLanguage(new GenericFuncCallback<String>(
				[
					String result | 
					Questionnaire.language = result
				]
			))

			userID = AuthorizationService.getCurrentUsername()
			qTimer = new QuestionTimer(8)
			
			//get preAndPostquestions, eyeTracking and RecordScreen from JSON
			initQuestionnaireSpecialSettings()
		}
		else {
			// continue experiment
			var form = getQuestionBox(questions.get(questionNr))
			Util::landscapeService.getLandscape(questions.get(questionNr).timestamp, questions.get(questionNr).activity, new GenericFuncCallback<Landscape>([updateClientLandscape]))
			timestampStart = System.currentTimeMillis()
			var caption = experimentName + ": " + "Question " + questionNr.toString + " of " + questions.size()
			ExperimentJS::changeQuestionDialog(form, language, caption, allowSkip)
			
			timestampStart = System.currentTimeMillis()			
			ExperimentJS::showQuestionDialog()
		}
	}
	
	/**
	 * 
	 */
	def static finishStart(Question[] questions) {		
		var List<Question> list = new ArrayList<Question>();
		for(Question q : questions){
			list.add(q)
		}
		Questionnaire::questions = list
		
		questionService.allowSkip(new SkipCallback())

		if (userID.equals("")) {
			userID = "DummyUser"
		}
		
		timestampStart = System.currentTimeMillis()
		var content = Util::dialogMessages.expProbandModalStart()
		ExperimentJS::showExperimentStartModal(experimentName, content)
	}
	
	/**
	 * 
	 */
	def static continueAfterModal() {
		ExperimentJS::showQuestionDialog()
		jsonService.getQuestionnairePrequestionsForUser(experimentFilename, userID, new GenericFuncCallback<ArrayList<Prequestion>>([showPrequestionDialog]))
	}

	/**
	 * 
	 */
	def static getFullForm(boolean prequestions) {
		if(prequestions) {
			 return getPrequestionForm()
		} else {
			 return getPostquestionForm()
		}
	}
	
	/**
	 * 
	 */
	def static getPrequestionForm() {
		
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		
		var Prequestion currentQuestion;
		//append for every question
		for (var j = 0; j < preDialog.size(); j++) {
			currentQuestion = preDialog.get(j)
			if(currentQuestion.getText() != "") {
				//append a div and a label for every question
				html.append("<div class='form-group' id='form-group'>")
				html.append("<label for='"+(j+1)+"'>"+currentQuestion.getText()+"</label>")
			
				//append special answer input
				html.append(currentQuestion.getTypeDependentHTMLInput())
			
				html.append("</div>")
			}
		}
		
		html.append("</form>")
		return html.toString()
		
	}
	
	/**
	 * 
	 */
	def static getPostquestionForm() {
		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		
		var Postquestion currentQuestion;
		//append for every question
		for (var j = 0; j < postDialog.size(); j++) {
			currentQuestion = postDialog.get(j)
			if(currentQuestion.getText() != "") {
				//append a div and a label for every question
				html.append("<div class='form-group' id='form-group'>")
				html.append("<label for='"+(j+1)+"'>"+currentQuestion.getText()+"</label>")
			
				//append special answer input
				html.append(currentQuestion.getTypeDependentHTMLInput())
			
				html.append("</div>")
			}
		}
		
		html.append("</form>")
		return html.toString()
	}
	
	/**
	 * 
	 */
	def static saveStatisticalAnswers(String answer) {
		var StringBuilder answerString = new StringBuilder()
		var String[] answerList = answer.split("&")
		var String s;
		for (var int i = 0; i < answerList.length; i++) {
			s = answerList.get(i)
			s = cleanInput(s.substring(s.indexOf("=") + 1))
			answerString.append(s)
			if (i + 1 == answerList.length) {
				answerString.append("\n")
			} else {
				answerString.append(",")
			}
		}
		questionService.writeStringAnswer(answerString.toString(), userID, new VoidCallback())
	}

	/**
	 * 
	 */
	def static showPrequestionDialog(ArrayList<Prequestion> loadedPrequestions) {
		if(loadedPrequestions.size() == 0 || !preAndPostquestions) {
			introQuestionnaire()
		} else {
			preDialog = loadedPrequestions
			var forms = getFullForm(true)
			ExperimentJS.showPrequestionDialog(forms, language)
		}
	}

	/**
	 * 
	 */
 	def static savePrequestionForm(String answer) {
		saveStatisticalAnswers(answer)
		introQuestionnaire()
	}

	/**
	 * Starts the main part of the questionnaire: displays first question, starts the timer
	 */
	def static introQuestionnaire() {
		//start eyeTracking and/or screen recording
		jsonService.getQuestionnairePrefix(userID, new GenericFuncCallback<String>([
			String questionnairePrefix |
			ExperimentJS::startEyeTrackingScreenRecording(eyeTracking, screenRecording, userID, questionnairePrefix)
			//start a predialog for user
			ExperimentJS::showMainQuestionsStartModal();
		]))
	}
	
	/**
	 * 
	 */
	def static startMainQuestionsDialog() {
		// start questionnaire
		Util::landscapeService.getLandscape(questions.get(questionNr).timestamp, questions.get(questionNr).activity, new GenericFuncCallback<Landscape>([updateClientLandscape]))
		qTimer.setTime(System.currentTimeMillis())
		qTimer.setMaxTime(questions.get(questionNr).worktime)
		qTimer.scheduleRepeating(1000)
		var caption = experimentName + ": " + "Question " + (questionNr + 1).toString + " of " + questions.size()
		ExperimentJS::changeQuestionDialog(getQuestionBox(questions.get(questionNr)), language, caption, allowSkip)
	}

	/**
	 * Builds the html form for the given question
	 * @param question The question that shall be displayed
	 */
	def static getQuestionBox(Question question) {
		var StringBuilder html = new StringBuilder()
		html.append("<p>" + question.text + "</p>")
		html.append("<form class='form' role='form' id='questionForm'>")
		var String[] ans = question.answers
		html.append("<div class='form-group' id='form-group'>")
		if (question.type.equals("Free")) {
			html.append("<label for='input'>Answer</label>")
			html.append("<div id='input' class='input-group'>")
			if (question.freeAnswers > 1) {
				for (var i = 0; i < question.freeAnswers; i++) {
					if(!question.answers.get(i).equals("")) {	//check whether the correct answer is '' (empty)
						html.append(
						"<input type='text' class='form-control' id='input" + i.toString() +
							"' placeholder='Enter Answer' name='input" + i.toString() +
							"' minlength='1' autocomplete='off' required>")
					}	
				}
			} else { // only one question gets a textbox
				html.append(
						"<textarea class='form-control questionTextarea' id='input1' name='input1' rows='2' required></textarea>"
				)
			}
				html.append("</div>")
		} else if (question.type.equals("MC")) {
			html.append("<div id='radio' class='input-group'>")
			var i = 0;
			while (i < ans.length) {
				html.append("<input type='radio' id='radio" + i + "' name='radio' value='" + ans.get(i) + "' style='margin-left:10px;' required>
					<label for='radio" + i + "' style='margin-right:15px; margin-left:5px'>" + ans.get(i) +
					"</label> ")
				i = i + 1
			}
			html.append("</div>")
		} else if (question.type.equals("MMC")) {
			html.append("<div id='check' class='input-group'>")
			var i = 0;			
			while (i < ans.length) {
				if(!ans.get(i).equals(" ")) {
					html.append("<input type='checkbox' id='check" + i + "' name='check' value='" + ans.get(i) + "' style='margin-left:10px;'>
					<label for='check" + i + "' style='margin-right:15px; margin-left:5px'>" + ans.get(i) +
					"</label> ")
				}
				i = i + 1
			}
			html.append("</div>")
		}
		html.append("</div>")
		html.append("</form>")
		return html.toString()
	}

			/**
			 * Saves the answer that was given for the previous question and loads 
			 * the new question or ends the questionnaire if it was the last question.
			 * @param answer The answer to the previous question
			 */
			def static nextQuestion(String answer) {
				var newTime = System.currentTimeMillis()
				var timeTaken = newTime - timestampStart
				var Answer ans = new Answer(questions.get(questionNr).questionID, cleanInput(answer), timeTaken,
					timestampStart, newTime, userID)
				answers.add(ans)
				questionService.writeAnswer(ans, new VoidCallback())
				if (questionNr == questions.size() - 1) {	//if last question
					SceneDrawer::lastViewedApplication = null
					questionService.getEmptyLandscape(new EmptyLandscapeCallback())
					//stop eye tracking / screen recording
					ExperimentJS::stopEyeTrackingScreenRecording()
					if(preAndPostquestions){
						jsonService.getQuestionnairePostquestionsForUser(experimentFilename, userID, new GenericFuncCallback<ArrayList<Postquestion>>([showPostquestionDialog]))
						
					} else {
						finishQuestionnaire()
					}	
					qTimer.cancel()
					ExperimentJS::hideTimer()
					questionNr = 0
				} else {
					// if not last question
					ExperimentJS::hideTimer()
					questionNr = questionNr + 1
					var form = getQuestionBox(questions.get(questionNr))
					Util::landscapeService.getLandscape(questions.get(questionNr).timestamp, questions.get(questionNr).activity, new GenericFuncCallback<Landscape>([updateClientLandscape]))
					timestampStart = System.currentTimeMillis()
					qTimer.setTime(timestampStart)
					qTimer.setMaxTime(questions.get(questionNr).worktime)
					var caption = experimentName + ": " + "Question " + (questionNr + 1).toString + " of " + questions.size()
					ExperimentJS::changeQuestionDialog(form, language, caption, allowSkip)
				}
			}
			
			def static showPostquestionDialog(ArrayList<Postquestion> loadedPostquestions) {
				if(loadedPostquestions.size() == 0 || loadedPostquestions.get(0).getText() == "") {
					finishQuestionnaire()
				} else {
					postDialog = loadedPostquestions
					ExperimentJS::showPostquestionDialog(getFullForm(false), language)
				}
			}
			
			def static updateClientLandscape(Landscape l) {
				
				var maybeApplication = questions.get(questionNr).maybeApplication
				
				if(maybeApplication.equals("")) {
						SceneDrawer::createObjectsFromLandscape(l, false)
					}
					else {
						for (system : l.systems) {
							for (nodegroup : system.nodeGroups) {
								for (node : nodegroup.nodes) {
									for (application : node.applications) {
										if (application.name.equals(maybeApplication)) {											
											SceneDrawer::createObjectsFromApplication(application, false)
											
											JSHelpers::hideElementById("openAllComponentsBtn")
											JSHelpers::hideElementById("export3DModelBtn")
											JSHelpers::hideElementById("performanceAnalysisBtn")
											JSHelpers::hideElementById("virtualRealityModeBtn")
											JSHelpers::hideElementById("databaseQueriesBtn")
											
											return;
										}
									}
								}
							}
						}
					}
			}

			/**
			 * Save answers of Postquestions and 
			 */
			def static savePostquestionForm(String answer) {
				saveStatisticalAnswers(answer)
				finishQuestionnaire()
			}

			/**
			 * Ends the experiment and logs out the user.
			 */
			def static finishQuestionnaire() {
				//in case of screen recording, let the user first upload the local files
				
				if(screenRecording) {
					ExperimentJS::tryToFinishQuestionnaire() //call here a function					
				} else {
					ExperimentJS::closeQuestionDialog()	
					Util::getLoginService.setFinishedExperimentState(true, new GenericFuncCallback<Void>([finishLogout]))	
				}				
			}
			
			/**
			 * Logs out the user
			 */
			def static void finishLogout() {
				Util::getLoginService.logout(new LogoutCallBack)
			}

			/**
			 * Downloads the answers as a .zip
			 */
			def static downloadAnswers() {
				if (questionService == null) {
					questionService = Util::getQuestionService()
				}
				questionService.downloadAnswers(new ZipCallback())
			}
			
			/**
			 * 
			 */
			def static cleanInput(String s) {
				var cleanS = s.replace("+", " ").replace("%40", "@").replace("%0D%0A", " ") // +,@,enter
				cleanS = cleanS.replace("%2C", "U+002C").replace("%3B", "U+003B").replace("%3A", "U+003A") // ,, ;, :,
				cleanS = cleanS.replace("%C3%A4", "U+00E4").replace("%C3%BC", "U+00FC").replace("%C3%B6", "U+00F6").
					replace("%C3%9F", "U+00DF") // ä, ü, ö, ß
				cleanS = cleanS.replace("%C3%84", "U+00C4").replace("%C3%9C", "U+00DC").replace("%C3%96", "U+00D6") // Ä, Ü, Ö 
				cleanS = cleanS.replace("%26", "U+0026").replace("%3F", "U+003F").replace("%22", "U+0022") // &, ? , " 
				cleanS = cleanS.replace("%7B", "U+007B").replace("%7D", "U+007D").replace("%2F", "U+002F") // {,},/
				cleanS = cleanS.replace("%5B", "U+005B").replace("%5D", "U+005D").replace("%5C", "U+005C") // [, ], \
				cleanS = cleanS.replace("%23", "U+0023") // #
				return cleanS
			}
			
			/**
			 * Retun
			 */
			/*def static boolean getPreAndPostquestions() {
				return preAndPostquestions
			}*/
			
			/**
			 * Sets the attribute preAndPostquestions with given parameter and requests the boolean eyeTracking from the server (for the questionnaire)
			 * @param newPreAndPostquestions is a boolean value to set as the attribute of the questionnaire
			 */
			def static initPreAndPostquestions(boolean newPreAndPostquestions) {
				preAndPostquestions = newPreAndPostquestions 
				jsonService.getQuestionnaireEyeTracking(experimentFilename, userID, "", new GenericFuncCallback<Boolean>([initEyeTracking]))
			}
			
			/**
			 * Sets the attribute eyeTracking of the questionnaire with the given parameter and requests the boolean value screenRecording from the server (for the questionnaire)
			 * @param newEyeTracking is a boolean value to set the attribute of the questionnaire
			 */
			def static initEyeTracking(boolean newEyeTracking) {
				eyeTracking = newEyeTracking
				jsonService.getQuestionnaireRecordScreen(experimentFilename, userID, "", new GenericFuncCallback<Boolean>([initScreenRecording]))
			}
			
			def static initScreenRecording(boolean newScreenRecording) {
				screenRecording = newScreenRecording
				//a setup for a JS functionality for uploading the screenRecords mp4 file to the server
				if(screenRecording) {
					ExperimentJS::setupTryToFinishQuestionnaire();
				}
				finishInitOfQuestionnaire()
			}
			
			def static initQuestionnaireSpecialSettings() {	//workaround for asynchron race-conditions
				jsonService.getQuestionnairePreAndPostquestions(experimentFilename, userID, "", new GenericFuncCallback<Boolean>([initPreAndPostquestions]))		
			}
			
			def static finishInitOfQuestionnaire() {
				jsonService.getExperimentTitle(experimentFilename, new GenericFuncCallback<String>(
							[
								String name | 
								experimentName = name
								jsonService.getQuestionnaireQuestionsForUser(experimentFilename, userID, new GenericFuncCallback<Question[]>([finishStart]))
							]
						))
			}
	
	def static closeAndFinishExperiment() {
		ExperimentJS::closeQuestionDialog()	
		Util::getLoginService.setFinishedExperimentState(true, new GenericFuncCallback<Void>([finishLogout]))
	}
	
	def static startUploadEyeTrackingData(String eyeTrackingData) {
		//RPC to server for upload of data
		jsonService.uploadEyeTrackingData(experimentName, userID, eyeTrackingData, new GenericFuncCallback<Boolean>([
			boolean response |
			
		]));
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
		
		
		
		
		