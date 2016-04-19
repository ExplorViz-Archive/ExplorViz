package explorviz.visualization.experiment

import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.main.ExplorViz
import explorviz.visualization.main.JSHelpers

class ExperimentToolsPage implements IPage {
	override render(PageControl pageControl) {
		pageControl.setView("")

		JSHelpers::hideElementById("legendDiv")

		ExperimentTools::toolsModeActive = true

		if (Experiment::experiment) {
			pageControl.setView(
				'''
					<div style="width: 50%; height: 100%; float: left;">
					  «showQuestionsAndAnswers()»
					</div>
					<div style="width: 50%; height: 100%; float: left; border-left: 1px solid;">
					  <button id="stopExperimentBtn" type="button" class="btn btn-default btn-sm">
					    <span class="glyphicon glyphicons-notes-2"></span> Stop Experiment 
					  </button>
					</div>
				'''.toString())
		} else {
			pageControl.setView(
				'''
					<div style="width: 50%; height: 100%; float: left;">
					  <button id="startExperimentBtn" type="button" style="display: block; margin: 0 auto;"
					  class="btn btn-default btn-sm">
					    <span class="glyphicon glyphicons-notes-2"></span> Start Experiment 
					  </button>
					</div>
					<div style="width: 50%; height: 50%; float: left; border-left: 1px solid;">
					  <button id="newExperimentBtn" type="button" style="display: block; margin: 0 auto;"
					  class="btn btn-default btn-sm">
					    <span class="glyphicon glyphicons-notes-2"></span> Create New Experiment 
					  </button>
					</div>
					<div style="width: 50%; height: 50%; float: left; border-left: 1px solid; border-top: 1px solid;">
					  <button id="prevExperimentBtn" type="button" style="display: block; margin: 0 auto;"
					  class="btn btn-default btn-sm">
					    <span class="glyphicon glyphicon-search"></span> Previous Experiments
					  </button>
					</div>
				'''.toString())
		}

		ExperimentToolsJS::init()

	}

	def showQuestionsAndAnswers() {
				
		var questionList = Questionnaire.questions

		var StringBuilder html = new StringBuilder()

		html.append("<div align='center' style='width: 50%; height: 50%;'>")

		html.append("<select id='questionsSelect' class='form-control' name='textQuestions'>")
		var selectedInfo = "selected"
		for (var j = 0; j < questionList.size(); j++) {
			html.append(
				"<option id='" + j + "'" + selectedInfo + ">" + "Question " + (questionList.get(j).questionID + 1) +
					"</option>")
			if (j == 0) selectedInfo = ""
		}
		html.append("</select><p>")
		
		html.append("<label id=questionTextLabel> " + questionList.get(0).text + "</label>")

		html.append(
			"<div style='width: 75%; height: 50%; border-style: dashed;'>
                       <label> Question: Show one answer of chosen question. </label> 
                     </div>")

		html.append("</div>")

		return html.toString()
	}
	
	def static getQuestionText(int id) {
		return Questionnaire.questions.get(id).text
	}

	def static showNewExpWindow() {
		ExplorViz::getPageCaller().showNewExp()
	}
}
