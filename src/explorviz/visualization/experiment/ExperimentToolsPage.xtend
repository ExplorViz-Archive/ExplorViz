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

		// JSHelpers::showElementById("overwriteQuestions")
		if (Experiment::experiment) {
			pageControl.setView('''
				<div style="width: 50%; height: 100%; float: left;">
				  <button id="startExperimentBtn" type="button" class="btn btn-default btn-sm">
				    <span class="glyphicon glyphicons-notes-2"></span> Start Experiment 
				  </button>
				</div>
				<div style="width: 50%; height: 50%; float: left;">
				  <button id="newExperimentBtn" type="button" class="btn btn-default btn-sm">
				    <span class="glyphicon glyphicons-notes-2"></span> Create New Experiment 
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

	def static showNewExpWindow() {
		ExplorViz::getPageCaller().showNewExp()
	}
}
