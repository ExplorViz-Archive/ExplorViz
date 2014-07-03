package explorviz.visualization.view

import com.google.gwt.event.dom.client.ClickEvent

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.ExperimentJS
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.experiment.services.TutorialServiceAsync
import explorviz.visualization.main.PageControl
import java.util.ArrayList
import java.util.List

import static explorviz.visualization.experiment.Experiment.*
import static explorviz.visualization.view.ConfigurationPage.*
import explorviz.visualization.main.ClientConfiguration
import com.google.gwt.event.shared.HandlerRegistration
import explorviz.visualization.main.JSHelpers
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.experiment.services.ConfigurationServiceAsync
import explorviz.visualization.experiment.services.ConfigurationService
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.engine.Logging

class ConfigurationPage implements IPage {
	
	static protected ArrayList<String> languages;
	static HandlerRegistration saveConfigHandler
	
	override render(PageControl pageControl) {
	    Navigation::deregisterWebGLKeys()
	    getLanguages()
	    JSHelpers::hideAllButtonsAndDialogs()
	    JSHelpers::showElementById("saveAdminConfig")
	    
		pageControl.setView('''<form class='form' role='form' id='adminConfigurationForm'>
					<div class='form-group'>
					<label for='fps'>Show FPS:</label>«createBooleanIdCombobox("fps", ClientConfiguration::showFPS)»
					<label for='languages'>Tutorial Language:</label> «createLanguageCombobox()»
					<label for='experiment'>Experiment mode:</label> «createBooleanIdCombobox("experiment", false)»
					</div></form>'''.toString())
		
		val saveConfig = RootPanel::get("saveAdminConfig")
		
		saveConfig.sinkEvents(Event::ONCLICK)
		saveConfigHandler = saveConfig.addHandler([
			//Call function to transport to server
			JSHelpers.saveConfiguration()
		], ClickEvent::getType())
		 
		Experiment::tutorial = false
	}
	
	def createBooleanIdCombobox(String id, boolean selectedValue) {
		val possibilities = new ArrayList<String>
		possibilities.add("true")
		possibilities.add("false")
		createIdCombobox(possibilities, id, "width: 100px;", if (selectedValue) 0 else 1 )
	}
	
	def getLanguages(){
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getLanugages(new LanguagesCallback())
	}
	
	def createLanguageCombobox() {
		if(languages!=null){
			createIdCombobox(languages, "languages", "width: 100px;", 0)
		}else{
			var english = new ArrayList<String>()
			english.add("english")
			createIdCombobox(english, "languages", "width:100px;",0)
		}
	}
	
	def protected createIdCombobox(List<String> possibilities, String id, String style, int selectedIndex) {
		'''<select class='form-control' name="«id»" id="«id»" style="«style»" onchange="alert(this.value)">
		«FOR i : 0 .. possibilities.size-1»
			<option « if (i == selectedIndex) "selected" » value="«possibilities.get(i)»">«possibilities.get(i).toFirstUpper»</option>
		«ENDFOR»
		</select>'''
	}
	
	static def saveConfiguration(String config){
		var String[] configList = config.split("&")
		var String language = configList.get(1).substring(10) //cut off "languages="
		var boolean experiment = configList.get(2).substring(11).equals("true") //cut off "experiment="
Logging.log("Setting language to "+language+" and experiment to "+experiment)		
		val ConfigurationServiceAsync configService = GWT::create(typeof(ConfigurationService))
		val endpoint = configService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "configurationservice"
		configService.saveConfiguration(language, experiment, new VoidCallback())
	}
		
}

class LanguagesCallback implements AsyncCallback<String[]> {
			
	override onFailure(Throwable caught) {
	}
	
	override onSuccess(String[] result) {
		ConfigurationPage.languages = new ArrayList<String>()
		var i = 0
		while(i < result.length){
			var r = result.get(i)
			ConfigurationPage.languages.add(r)
			i = i+1
		}
		ExperimentJS::fillLanguageSelect(result)
	}
}