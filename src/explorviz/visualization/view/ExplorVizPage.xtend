package explorviz.visualization.view

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.TutorialJS
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.main.Util
import explorviz.visualization.experiment.callbacks.BooleanFuncCallback
import explorviz.visualization.experiment.callbacks.GenericFuncCallback
import explorviz.shared.model.Landscape
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.services.AuthorizationService
import explorviz.visualization.landscapeexchange.LandscapeExchangeManager
import explorviz.visualization.main.JSHelpers

class ExplorVizPage implements IPage {
	override render(PageControl pageControl) {
	    pageControl.setView("")

	    Experiment::tutorial = false
	    ExperimentTools::toolsModeActive = false
	    TutorialJS.closeTutorialDialog()
	    TutorialJS.hideArrows()
	    
		WebGLStart::initWebGL()
	    Navigation::registerWebGLKeys()    
	    
	    // czi 22.03.2017 hide timeshift for study
	    JSHelpers::hideElementById("startStopBtn")
		JSHelpers::hideElementById("timeshiftChartDiv")
		JSHelpers::hideElementById("startStopLabel")
	    
	    Util::tutorialService.isExperiment(new BooleanFuncCallback<Boolean>([setExperimentState]))
		
	}
	
	def private static void setExperimentState(boolean isExperimentRunning){		
		Experiment::experiment = isExperimentRunning
		
		if(isExperimentRunning)
			Util::JSONService.isUserInCurrentExperiment(AuthorizationService.getCurrentUsername(), new BooleanFuncCallback<Boolean>([checkUserState]))					
	}
	
	
	def private static void checkUserState(boolean isUserInExperiment){
		if(isUserInExperiment) {
			Util::tutorialService.getExperimentFilename(new GenericFuncCallback<String>([setExperimentFile]))		
		}
		else {
			resetLandscape()
		}
	}	
	
	def private static void setExperimentFile(String filename) {			
		Questionnaire::experimentFilename = filename
				
		if (Experiment::experiment && filename != null) {
			LandscapeExchangeManager::stopAutomaticExchange("0")
		
			JSHelpers::hideElementById("startStopBtn")
			JSHelpers::hideElementById("timeshiftChartDiv")
			JSHelpers::hideElementById("startStopLabel")
				
			Questionnaire::startQuestions()	
		}		
	}
	
	def static void resetLandscape() {		
		Util::landscapeService.getCurrentLandscapeByFlag(false, new GenericFuncCallback<Landscape>(
			[
				Landscape l | 
				SceneDrawer::createObjectsFromLandscape(l, false)
			]
		))	
	}

}