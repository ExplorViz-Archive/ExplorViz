package explorviz.visualization.experiment

import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage

import static explorviz.visualization.experiment.Experiment.*
//import explorviz.visualization.engine.main.SceneDrawer
//import explorviz.visualization.landscapeexchange.LandscapeConverter
//import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
//import explorviz.visualization.engine.Logging

class TutorialPage implements IPage {
	override render(PageControl pageControl) {
		pageControl.setView("")
		Experiment::resetTutorial()
		
		Navigation::registerWebGLKeys()
		Experiment::loadTutorial()
		Experiment::getTutorialText(Experiment::tutorialStep)
	    Experiment::tutorial = true
	    ExperimentJS.showTutorialDialog()   
	    ExperimentJS.showTutorialContinueButton()
		WebGLStart::initWebGL()
		
//		Logging.log("before redraw")
//		//Erzwinge redraw - doesn't work
//		LandscapeConverter::reset()
//		LandscapeExchangeManager::fetchSpecificLandscape(System.currentTimeMillis().toString())
//		SceneDrawer::redraw()

	    
	}
	
}