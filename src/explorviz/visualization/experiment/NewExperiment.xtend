package explorviz.visualization.experiment

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.tools.ExperimentTools
import java.util.ArrayList
import elemental.client.Browser
import elemental.dom.Element
import explorviz.visualization.engine.Logging
import explorviz.visualization.experiment.NewExperimentJS.MyJsArray

class NewExperiment implements IPage {
	private static int questionPointer = 0;
	private static PageControl pc;
	private static ArrayList<String> questions;
	private static Element expSliderFormDiv
	private static Element expSliderButtonDiv
	private static Element expSliderSelectDiv
	protected static int numOfCorrectAnswers = 1

	override render(PageControl pageControl) {
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
			    <option value="3">Statistical</option>
			  </select>
			</label>
		'''

		expSliderSelectDiv.hidden = true;
	}

	def static protected getNextQuestion() {

		if (questionPointer >= 1)
			NewExperimentJS::saveQuestion

		if (questionPointer >= 0) {
			createQuestForm(1)
			expSliderFormDiv.hidden = false
			expSliderButtonDiv.hidden = false
			expSliderSelectDiv.hidden = false
		} 
		
		questionPointer += 1;
	}

	def static protected processCompletedQuestion(int questionIndex) {
		// get all data
		expSliderFormDiv.innerHTML
	}

	def static protected setNextQuestion(int next) {
		questionPointer = next;
	}

	def static protected getQuestForm(int i) {
		return questions.get(i)
	}

	def static protected showOptionsDialog() {
		expSliderFormDiv.innerHTML = '''
			<button id='closeExp'>Close Experiment</button>
			<br><br>
			<button id='nextQuestion'>Create next question</button>
			<br><br>
			<button id='showPrevQuest'>Show previous question</button>
		'''

		NewExperimentJS::setupOptButtonHandlers
	}

	// TODO do we really need different forms?
	// use input fields for correct answers and show
	// radio buttons etc. only for subject
	def static protected createQuestForm(int index) {
		var String form

		numOfCorrectAnswers = 0

		if (index < 0) {
			form = ''''''
		} else if (index == 1) {
			form = '''
				<form id='expQuestionForm'>
				  Question «1»
				  <br>
				  Question text:
				  <br>
				  <textarea class='expTextArea' rows='4' cols='35' id='inputQType' name='inputQType'></textarea>
				  <br>
				  <br> Correct answers:
				  <br>
				  <div id='freeTextAnswers'>
				    <input type='text' name='correctAnswer«numOfCorrectAnswers»' id='correctAnswer«numOfCorrectAnswers»'>
				  </div>
				</form>
			'''
		} else if (index == 2) {
			form = '''
				<form id='expQuestionForm'>
				  Question «1»
				  <br>
				  Question text:
				  <br>
				  <textarea class='expTextArea' rows='4' cols='35' id='inputQType' name='inputQType'></textarea>
				  <br>
				  <input type='radio' name='gender' value='male' checked> Male
				  <br>
				  <input type='radio' name='gender' value='female'> Female
				  <br>
				  <input type='radio' name='gender' value='other'> Other
				  <br>
				</form>
			'''
		} else if (index == 3) {
			form = '''
				<form id='expQuestionForm'>
				  Question «1»
				  <br>
				  Question text:
				  <br>
				  <textarea class='expTextArea' rows='4' cols='35' id='inputQType' name='inputQType'></textarea>
				  <br>
				  <br> Possible answers:
				  <br>
				  <div id='freeTextAnswers'>
				    <input type='text' name='correctAnswer«numOfCorrectAnswers»' id='correctAnswer«numOfCorrectAnswers»'>
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

	def static protected createXML(MyJsArray obj) {

		Logging::log(obj.getValue(0))
	}

}
