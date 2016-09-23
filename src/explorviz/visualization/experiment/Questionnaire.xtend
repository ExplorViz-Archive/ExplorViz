package explorviz.visualization.experiment

import java.util.ArrayList
import explorviz.shared.experiment.Question
import explorviz.shared.experiment.Answer
import java.util.List
import explorviz.visualization.experiment.services.QuestionServiceAsync
import explorviz.visualization.services.AuthorizationService
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.experiment.callbacks.DialogCallback
import explorviz.visualization.experiment.callbacks.ZipCallback
import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.main.LogoutCallBack
import explorviz.visualization.experiment.callbacks.SkipCallback
import explorviz.visualization.main.ExplorViz
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.shared.experiment.StatisticQuestion
import explorviz.visualization.experiment.callbacks.EmptyLandscapeCallback
import explorviz.visualization.main.Util
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.experiment.callbacks.GenericFuncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.Logging

/**
 * @author Santje Finke
 * 
 */
class Questionnaire {
	static int questionNr = 0
	static boolean answeredPersonal = false
	static long timestampStart
	static List<List<StatisticQuestion>> dialog;
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

			jsonService.getExperimentTitle(experimentFilename, new GenericFuncCallback<String>([String name | experimentName = name]))
			jsonService.getQuestionnaireQuestionsForUser(experimentFilename, userID, new GenericFuncCallback<Question[]>([finishStart]))
		}
		else {
			// continue experiment
			var form = getQuestionBox(questions.get(questionNr))
			//questionService.setMaxTimestamp(questions.get(questionNr).timeframeEnd, new VoidCallback())
			Util::landscapeService.getLandscape(questions.get(questionNr).timestamp, questions.get(questionNr).activity, new GenericFuncCallback<Landscape>([updateClientLandscape]))
			timestampStart = System.currentTimeMillis()
			var caption = "Question " + questionNr.toString + " of " + questions.size()
			ExperimentJS::changeQuestionDialog(form, language, caption, allowSkip)
			
			timestampStart = System.currentTimeMillis()
			if (ExplorViz.isControlGroupActive()) {
				ExperimentJS::showQuestionDialogExtraVis()
			} else {			
				ExperimentJS::showQuestionDialog()
				ExperimentJS::showExperimentNameDialog(experimentName)
			}
		}
	}
	
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
		if (ExplorViz.isControlGroupActive()) {
			ExperimentJS::showQuestionDialogExtraVis()
		} else {			
			ExperimentJS::showQuestionDialog()
			ExperimentJS::showExperimentNameDialog(experimentName)
		}
		
		if (ExplorViz::isControlGroupActive) {
			questionService.getExtravisVocabulary(new DialogCallback())
		} else {
			questionService.getVocabulary(new DialogCallback())
		}
	
	}

	def static getForm(int i) {
		var List<StatisticQuestion> d = dialog.get(i)

		var StringBuilder html = new StringBuilder()
		html.append("<form class='form' role='form' id='questionForm'>")
		for (var j = 0; j < d.size(); j++) {
			html.append(d.get(j).getHTML())
		}
		html.append("</form>")
		return html.toString()
	}

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

	def static showFirstDialog(List<List<StatisticQuestion>> d) {
		dialog = d
		ExperimentJS.showFirstDialog(getForm(0), language)
	}

	def static saveFirstForm(String answer) {
		saveStatisticalAnswers(answer)
		ExperimentJS::showSecondDialog(getForm(1), language)
	}

	def static saveSecondForm(String answer) {
		saveStatisticalAnswers(answer)

		//LandscapeExchangeManager::fetchSpecificLandscape(questions.get(0).timeframeEnd.toString())

		answeredPersonal = true
		ExperimentJS::showThirdDialog(getForm(2))
	}

	/**
	 * Starts the main part of the questionnaire: displays first question, starts the timer
	 */
	def static introQuestionnaire() {
		// start questionnaire
		var caption = "Question " + (questionNr + 1).toString + " of " + questions.size()
		//questionService.setMaxTimestamp(questions.get(questionNr).timeframeEnd, new VoidCallback())
		Util::landscapeService.getLandscape(questions.get(questionNr).timestamp, questions.get(questionNr).activity, new GenericFuncCallback<Landscape>([updateClientLandscape]))
		qTimer.setTime(System.currentTimeMillis())
		qTimer.setMaxTime(questions.get(questionNr).worktime)
		qTimer.scheduleRepeating(1000)
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
					html.append(
						"<input type='text' class='form-control' id='input" + i.toString() +
							"' placeholder='Enter Answer' name='input" + i.toString() +
							"' minlength='1' autocomplete='off' required>")
						}
					} else { // only one question gets a textbox
						html.
							append(
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
						html.append("<input type='checkbox' id='check" + i + "' name='check' value='" + ans.get(i) + "' style='margin-left:10px;'>
							<label for='check" + i + "' style='margin-right:15px; margin-left:5px'>" + ans.get(i) +
							"</label> ")
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

				if (questionNr == questions.size() - 1) {
					Logging::log("if")
					SceneDrawer::lastViewedApplication = null
					questionService.getEmptyLandscape(new EmptyLandscapeCallback())
					ExperimentJS::showForthDialog(getForm(3), language)
					qTimer.cancel()
					ExperimentJS::hideTimer()
					questionNr = 0
				} else {
					Logging::log("else")
					// if not last question
					ExperimentJS::hideTimer()
					questionNr = questionNr + 1
					var form = getQuestionBox(questions.get(questionNr))
					//questionService.setMaxTimestamp(questions.get(questionNr).timeframeEnd, new VoidCallback())
					Util::landscapeService.getLandscape(questions.get(questionNr).timestamp, questions.get(questionNr).activity, new GenericFuncCallback<Landscape>([updateClientLandscape]))
					timestampStart = System.currentTimeMillis()
					qTimer.setTime(timestampStart)
					qTimer.setMaxTime(questions.get(questionNr).worktime)
					var caption = "Question " + (questionNr + 1).toString + " of " + questions.size()
					ExperimentJS::changeQuestionDialog(form, language, caption, allowSkip)
				}
			}
			
			def static updateClientLandscape(Landscape l) {
				SceneDrawer::createObjectsFromLandscape(l, false)
			}

			def static saveForthForm(String answer) {
				saveStatisticalAnswers(answer)
				ExperimentJS::showFifthDialog(getForm(4), language)
			}

			def static saveFifthForm(String answer) {
				saveStatisticalAnswers(answer)
				ExperimentJS::finishQuestionnaireDialog(getForm(5))
			}

			/**
			 * Ends the experiment and logs out the user.
			 */
			def static finishQuestionnaire() {
				ExperimentJS::closeQuestionDialog()	
				Util::getLoginService.setFinishedExperimentState(true, new GenericFuncCallback<Void>([finishLogout]))					
			}
			
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
			
		}

		class LanguageCallback implements AsyncCallback<String> {

			override onFailure(Throwable caught) {
				ErrorDialog::showError(caught)
			}

			override onSuccess(String result) {
				Questionnaire.language = result
			}

		}
		