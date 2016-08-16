package explorviz.visualization.experiment

import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.main.Util
import java.util.List
import explorviz.visualization.experiment.callbacks.StringListCallback
import explorviz.visualization.engine.Logging
import com.google.gwt.core.client.JsArrayString

class PrevExperiment implements IPage {
	
	var static JSONServiceAsync jsonService
	
	override render(PageControl pageControl) {
		pageControl.setView("")

		Experiment::tutorial = false
		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()
		
		jsonService = Util::getJSONService()		
		
		jsonService.getExperimentNames(new StringListCallback<List<String>>([finishInit]))

		//WebGLStart::initWebGL()
		//Navigation::registerWebGLKeys()
	}
	
	def static void finishInit(List<String> names) {
		
		var JsArrayString jsArrayString = JsArrayString.createArray().cast();
		for (String s : names) {
			jsArrayString.push(s.split(".json").get(0));
		}
	
	}

}
