package explorviz.visualization.experiment

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.WebGLStartTutorial
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.view.IPage

class TutorialPage implements IPage {
	override render(PageControl pageControl) {
				
	    pageControl.setView('''<script>$(function() { $( "#dialog" ).dialog(); }); </script>
	    	<div id="dialog" title="Tutorial">
    			<p>«getTutorialText(Experiment::tutorialStep)»</p>
  			</div>'''.toString())
	    
	    Navigation::registerWebGLKeys()
	    
		WebGLStartTutorial::initWebGL()
	    
	}
	
		
	def getTutorialText(int step) {
		//TutorialService.getText(step, language)
	}
	
	
}