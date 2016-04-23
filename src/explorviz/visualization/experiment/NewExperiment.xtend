package explorviz.visualization.experiment

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.tools.ExperimentTools

class NewExperiment implements IPage {
	override render(PageControl pageControl) {
		pageControl.setView('''
		<div id="expSlider">
		  <div id="expSliderLabel" class="expRotate">
		    Question Interface
		  </div>
		  <div id="expSliderForm">
		    <form>
		      First name:
		      <br>
		      <input type="text" name="firstname">
		      <br> Last name:
		      <br>
		      <input type="text" name="lastname">
		      <br>
		      <input type="radio" name="gender" value="male" checked> Male
		      <br>
		      <input type="radio" name="gender" value="female"> Female
		      <br>
		      <input type="radio" name="gender" value="other"> Other
		      <br>
		      <br>
		      <br>
		      <button id="expBackBtn">&lt;&lt; Back</button>
		      <button id="expSaveBtn">Save &gt;&gt; </button>
		    </form>
		  </div>
		</div>
		'''.toString())
		
		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		WebGLStart::initWebGL()
		Navigation::registerWebGLKeys()
		NewExperimentJS::init()

		
	}
}
