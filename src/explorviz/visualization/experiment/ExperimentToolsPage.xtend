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
			pageControl.setView('''
				<div style="width: 50%; height: 100%; float: left;">
				  «showTextQuestions()»
				</div>
				<div style="width: 50%; height: 100%; float: left; border-left: 1px solid;">
				  <button id="stopExperimentBtn" type="button" class="btn btn-default btn-sm">
				    <span class="glyphicon glyphicons-notes-2"></span> Stop Experiment 
				  </button>
				</div>
			     '''.toString())
		} else {
			pageControl.setView('''
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
				    <span class="glyphicon glyphicons-history"></span> Previous Experiments
				  </button>
				</div>
			     '''.toString())
		}

		ExperimentToolsJS::init()

	}

	def showTextQuestions() {
		
		//TODO Show "Question X" as option and display actual question text below when selected
		
		var questionList = Questionnaire.questions
		
		var StringBuilder html = new StringBuilder()
		html.append("<select name='textQuestions'>")
		for (var j = 0; j < questionList.size(); j++) {			
			html.append("<option>" + questionList.get(j).getText() + "</option>")
		}
		html.append("</select>")
		
		html.append("<div style='width: 50%; height: 50%; float: left; border-style: dashed;'>
                       <label> Question: Show one answer of chosen question. </label> 
                     </div>")
		
		return html.toString()				
	}

	def static showNewExpWindow() {
		ExplorViz::getPageCaller().showNewExp()
	}
}
