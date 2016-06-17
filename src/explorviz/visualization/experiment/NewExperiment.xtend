package explorviz.visualization.experiment

import elemental.client.Browser
import elemental.dom.Element
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.NewExperimentJS.ExplorVizJSArray
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import java.util.ArrayList

import static explorviz.visualization.experiment.tools.ExperimentTools.*
import explorviz.shared.experiment.Question
import explorviz.visualization.experiment.services.QuestionServiceAsync
import com.google.gwt.core.client.GWT
import explorviz.visualization.experiment.services.QuestionService
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.callbacks.VoidCallback
import java.util.List
import com.google.gwt.core.client.JsArray
import com.google.gwt.core.client.JavaScriptObject
import org.eclipse.xtend.lib.annotations.Accessors

class NewExperiment implements IPage {
	private static int questionPointer = -1;
	private static PageControl pc;
	private static ArrayList<String> questions
	private static Element expSliderFormDiv
	private static Element expSliderButtonDiv
	private static Element expSliderSelectDiv
	protected static int numOfCorrectAnswers = 1
	protected static boolean setupDone = false

	@Accessors protected static JsArray<ExplorVizJSArray> questionBuffer

	var static QuestionServiceAsync questionService

	override render(PageControl pageControl) {
		questionBuffer = JavaScriptObject.createArray().cast()
		questionService = getQuestionService()
		pc = pageControl
		pageControl.setView(initializeContainers());
		initializeWelcomeDialog()
		initializeButtons()

		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
		NewExperimentJS::init()

	// initializeQuestions()
	}

	def static private String initializeContainers() {
		questions = new ArrayList<String>();

		return '''
			<div id="expSlider">
			  <div id="expSliderLabel" class="expRotate">
			    Question Interface
			  </div>
			  <div id="expSliderInnerContainer">
			    <div id="expSliderSelectDiv">				     
			    </div>
			    <div id="expSliderForm" class='expScrollableDiv'>				     
			    </div>
			    <div id="expSliderButtonDiv">				     
			    </div>
			  </div>
			</div>			
		'''
	}

	def static private initializeWelcomeDialog() {

		expSliderFormDiv = Browser::getDocument().getElementById("expSliderForm")

		var welcomeText = '''			 
			Ich bin der Geist, der stets verneint!<br>
			Und das mit Recht; denn alles, was entsteht,<br>
			Ist wert, dass es zugrunde geht;<br>
			Drum besser waer's, dass nichts entstuende.<br>
			So ist denn alles, was ihr Suende,<br>
			Zerstoerung, kurz, das Boese nennt,<br>
			Mein eigentliches Element.<br>		
		'''

		expSliderFormDiv.innerHTML = welcomeText
	}

	def static private initializeButtons() {

		expSliderButtonDiv = Browser::getDocument().getElementById("expSliderButtonDiv")
		expSliderSelectDiv = Browser::getDocument().getElementById("expSliderSelectDiv")

		expSliderButtonDiv.innerHTML = '''
		<button id='expBackBtn'>&lt;&lt; Back</button>
		<button id='expSaveBtn'>Next &gt;&gt; </button>'''

		expSliderSelectDiv.innerHTML = '''
			<label for="qtType"> Question Type:
			  <select id="qtType" name="qtType">
			    <option value="1" selected>Free text</option>
			    <option value="2">Multiple-choice</option>
			  </select>
			</label>
		'''

		expSliderSelectDiv.hidden = true;
	}

	def static protected getNextQuestion() {

		questionPointer += 1;

		if (questionPointer >= 0) {
			createQuestForm(1, 1)
			expSliderFormDiv.hidden = false
			expSliderButtonDiv.hidden = false
			expSliderSelectDiv.hidden = false
		}

	}

	def static protected processCompletedQuestion(int questionIndex) {

		// get all data
		expSliderFormDiv.innerHTML
	}

	// not used atm
	def static protected setNextQuestion(int next) {
		questionPointer = next;
	}

	def static protected getQuestForm(int i) {
		return questions.get(i)
	}

	// not used atm
//	def static protected showOptionsDialog() {
//		expSliderFormDiv.innerHTML = '''
//			<button id='closeExp'>Close Experiment</button>
//			<br><br>
//			<button id='nextQuestion'>Create next question</button>
//			<br><br>
//			<button id='showPrevQuest'>Show previous question</button>
//		'''
//
//		NewExperimentJS::setupOptButtonHandlers
//	}
	def static protected createQuestForm(int index, int countOfAnswers) {
		var String form

		numOfCorrectAnswers = 0

		if (index <= 0) {
			form = ''''''
		} else if (index == 1) {
			form = '''
				<form id='expQuestionForm'>
				  Question «(questionPointer + 1)»
				  <br>
				  Question text:
				  <br>
				  <textarea class='expTextArea' rows='4' cols='35' id='inputQType' name='inputQType'></textarea>
				  <br>
				  Working time:
				  <br>
				  <input type='number' min='0' max='10' step='1' value='4' size='2' name='workingTime' id='workingTime'>
				  <br>
				  Timeframe:
				  <br>
				  <input type='number' min='0' max='10' step='1' value='6' size='2' name='timeframe' id='timeframe'>
				  <br>
				  Free answers:
				  <br>
				  <input type='number' min='0' max='10' step='1' value='0' size='2' name='freeAnswers' id='freeAnswers'>
				  <br>
				  <br> Correct answers:
				  <br>
				  <div id='answers'>
				  «FOR i : 0 .. countOfAnswers-1»
				  	<div id='answer«i»'>
				  	<input type='text' name='correctAnswer«i»' id='correctAnswer«i»'>
				  	</div>
				  «ENDFOR»
				  </div>
				</form>
			 '''

		} else if (index == 2) {
			form = '''
				<form id='expQuestionForm'>
				  Question «(questionPointer + 1)»
				  <br>
				  Question text:
				  <br>
				  <textarea class='expTextArea' rows='4' cols='35' id='inputQType' name='inputQType'></textarea>
				  <br>
				  Working time:
				  <br>
				  <input type='number' min='0' max='10' step='1' value='4' size='2' name='workingTime' id='workingTime'>
				  <br>
				  Timeframe:
				  <br>
				  <input type='number' min='0' max='10' step='1' value='6' size='2' name='timeframe' id='timeframe'>
				  <br>
				  Free answers:
				  <br>
				  <input type='number' min='0' max='10' step='1' value='0' size='2' name='freeAnswers' id='freeAnswers'>
				  <br>
				  <br> Possible answers:
				  <br>
				  <div id='answers'>
				  «FOR i : 0 .. countOfAnswers-1»
				  	<div id='answer«i»'>
				  	<input type='text' name='correctAnswer«i»' id='correctAnswer«i»'>
				  	<input type='checkbox' name='correctAnswerCheckbox«i»' id='correctAnswerCheckbox«i»'>
				  	</div>
				  «ENDFOR»
				  </div>
				</form>
			'''
		}

		expSliderFormDiv.innerHTML = form
		expSliderFormDiv.hidden = false;

		NewExperimentJS::setupAnswerHandler(numOfCorrectAnswers)

		if (index < 0) {
			expSliderButtonDiv.hidden = true;
		} else {
			expSliderButtonDiv.hidden = false;
		}
	}

	def static getQuestionService() {
		val QuestionServiceAsync questionService = GWT::create(typeof(QuestionService))
		val endpoint = questionService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "questionservice"
		return questionService
	}

	def static protected void updateOrSaveBuffer(ExplorVizJSArray formValues) {
		// TODO check if current question needs an update, i.e. is changed to it's previous save
		questionBuffer.push(formValues)
	}

	def static protected boolean saveToServer(ExplorVizJSArray formValues) {

		val length = formValues.length

		// question text
		val text = formValues.getValue(0)

		val workingTime = Integer.parseInt(formValues.getValue(1))

		val timeframe = Integer.parseInt(formValues.getValue(2))

		val freeAnswers = Integer.parseInt(formValues.getValue(3))

		// parse correct answers
		var List<String> correctList = new ArrayList<String>();
		var String[] correct = newArrayOfSize(length)

		for (var i = 4; i < length - 1; i++) {
			if (!formValues.getValue(i).equals(""))
				correctList.add(formValues.getValue(i))
		}
		correct = correctList.toArray(correct)

		val String[] answer = #[]

		if (text.equals("") || correctList.size() <= 0) {
			return false
		} else {
			updateOrSaveBuffer(formValues)
			// create object and send to server
			var Question newquestion = new Question(1, text, answer, correct, freeAnswers, workingTime, timeframe)
			// questionService.updateOrSaveQuestion(newquestion, new VoidCallback())
			questionService.saveQuestion(newquestion, new VoidCallback())
			return true
		}

	}

}
