package explorviz.visualization.experiment

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.tools.ExperimentTools
import java.util.ArrayList
import elemental.client.Browser
import elemental.dom.Element

class NewExperiment implements IPage {
	private static int questionPointer = -1;
	private static PageControl pc;
	private static ArrayList<String> questions;
	private static Element expSliderFormDiv
	private static Element expSliderButtonDiv
	private static Element expSliderSelectDiv

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
			    <div id="expSliderForm">				     
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
		<button id='expSaveBtn'>Save &gt;&gt; </button>'''

		expSliderSelectDiv.innerHTML = '''
		<select id="qtType" name="questionType">
		  <option value="-1">--</option>
		  <option value="1">Free text</option> 
		  <option value="2">Multiple-choice</option>
		  <option value="3">Statistical</option>
		</select>'''

		expSliderSelectDiv.hidden = true;
	}

	def static protected getNextQuestion() {
		
		// diese Methode kann später zum hin und her schalten für Fragen
		// genutzt werden. Save und Back Btn haben dann diese Methode 
		// als Ziel, bei Save muss also auch hier die Frage gespeichert werden

		if (questionPointer < 0) {
			expSliderFormDiv.hidden = true;
			expSliderButtonDiv.hidden = true;
			expSliderSelectDiv.hidden = false;
		} else {
			// expSliderSelectDiv.hidden = false;			
			// expSliderFormDiv.innerHTML = getQuestForm(questionPointer)
		}
		questionPointer++;
	}

	def static protected setNextQuestion(int next) {
		questionPointer = next;
	}

	def static protected getQuestForm(int i) {
		return questions.get(i)
	}

	def static protected createtQuestForm(int index) {
		var String form

		if (index < 0) {
			form = ''''''
		} else if (index == 1) {
			form = '''
				<form id='expQuestionForm'>
					  Question «1»
					  <br>
					  First name:
					  <br>
					  <input type='text' name='firstname'>
					  <br> Last name:
					  <br>
					  <input type='text' name='lastname'>
					  <br>
					  <input type='radio' name='gender' value='male' checked> Male
					  <br>
					  <input type='radio' name='gender' value='female'> Female
					  <br>
					  <input type='radio' name='gender' value='other'> Other
					  <br>
				</form>
			'''
		} else if (index == 2) {
			form = ''' TODO '''
		} else if (index == 3) {
			form = ''' TODO '''
		}

		expSliderFormDiv.innerHTML = form
		expSliderFormDiv.hidden = false;
		
		if (index < 0) {
			expSliderButtonDiv.hidden = true;
		} else {
			expSliderButtonDiv.hidden = false;
		}
	}

}
