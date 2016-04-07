package explorviz.visualization.experiment

import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.main.ExplorViz

class ExperimentToolsPage implements IPage {
	override render(PageControl pageControl) {
		pageControl.setView("")		
		

		ExperimentTools::toolsModeActive = true

		// JSHelpers::showElementById("overwriteQuestions")
		if (Experiment::experiment) {
			pageControl.setView('''
			<div style="width:400px; margin:0 auto">
			  <form style="display: inline-block; text-align: center;" class='form' role='form' id='runningExperimentForm'>
			    <div class="form-group">
			     <h3 Running experiment data, fast and hard...and fast </h3>			     
			    </div>
			  </form></br>
			  <button id="newExperimentBtn" type="button" class="btn btn-default btn-sm">
			    <span class="glyphicon glyphicons-notes-2"></span> Create New Experiment
			  </button>
			  <button id="prevExperimentBtn" type="button" class="btn btn-default btn-sm">
			    <span class="glyphicon glyphicons-history"></span> Previous Experiments
			  </button>
			</div>'''.toString())
		} else {
			pageControl.setView('''
			<div style="width:400px; margin:0 auto">
			  <button id="startExperimentBtn" type="button" class="btn btn-default btn-sm">
			    <span class="glyphicon glyphicons-notes-2"></span> Start Experiment 
			  </button>
			  <button id="newExperimentBtn" type="button" class="btn btn-default btn-sm">
			    <span class="glyphicon glyphicons-notes-2"></span> Create New Experiment 
			  </button>
			  <button id="prevExperimentBtn" type="button" class="btn btn-default btn-sm">
			    <span class="glyphicon glyphicons-history"></span> Previous Experiments
			  </button>
			</div>'''.toString())
		}
		
		ExperimentToolsJS::init()

	}
	
	def static showNewExpWindow() {	
		ExplorViz::getPageCaller().showNewExp()
	}
}
