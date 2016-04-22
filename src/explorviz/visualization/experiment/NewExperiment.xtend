package explorviz.visualization.experiment

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.tools.ExperimentTools

class NewExperiment implements IPage {
	override render(PageControl pageControl) {
		pageControl.setView("")
		
		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
		/*
		pageControl.setView(''' 
		<div id="expSlider" style="
		 padding: 5px;
		 text-align: center;
		 background-color: #e5eecc;
		 border: solid 1px #c3c3c3;">Question Panel
		</div>
		
		<div id="expQuestionPanel" style="display:none; padding:50px;">
		 <form>
		  First name:<br>
		  <input type="text" name="firstname"><br>
		  Last name:<br>
		  <input type="text" name="lastname"><br>
		  <input type="radio" name="gender" value="male" checked> Male<br>
		  <input type="radio" name="gender" value="female"> Female<br>
		  <input type="radio" name="gender" value="other"> Other
		</form> 
		</div>
		
		'''.toString())
		NewExperimentJS::init()
		* */
		
	}
}
