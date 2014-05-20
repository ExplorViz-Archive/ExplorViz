package explorviz.visualization.experiment

import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.WebGLStartTutorial
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.view.IPage
import explorviz.visualization.experiment.services.TutorialServiceAsync
import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.callbacks.TextCallback
import explorviz.visualization.engine.Logging

class TutorialPage implements IPage {
	override render(PageControl pageControl) {
		     
        var htmlResult = '''<script>
					    $(function() { 
					      $("#tutorialdialog")
					        .dialog({
					          modal: true,
					          closeOnEscape: false,
					          title: 'Tutorial',
					          open: function(event, ui) {
					            $(this).closest('.ui-dialog').
					              find('.ui-dialog-titlebar-close').hide();
					          }
					        }); 
					    }); 
					  </script>
          			<div id="tutorialdialog">«getFirstTutorialText()»</div>'''.toString() 
				
		pageControl.setView(htmlResult)
		
	    Navigation::registerWebGLKeys()
	    Experiment::tutorial = true
//	    
		//WebGLStartTutorial::initWebGL()
	    
	}
	
		
	def getFirstTutorialText() {
		Logging.log("ask for text")
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getText(0, new TextCallback())
	}
	
}