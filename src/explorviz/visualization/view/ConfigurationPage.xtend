package explorviz.visualization.view

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.ExperimentJS
import explorviz.visualization.experiment.callbacks.VoidCallback
import explorviz.visualization.experiment.services.ConfigurationService
import explorviz.visualization.experiment.services.ConfigurationServiceAsync
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.experiment.services.TutorialServiceAsync
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.main.PageControl
import java.util.ArrayList
import java.util.List
import explorviz.visualization.experiment.tools.ExperimentTools

class ConfigurationPage implements IPage {

	static protected ArrayList<String> languages;

	override render(PageControl pageControl) {
		Navigation::deregisterWebGLKeys()
		getLanguages()
		JSHelpers::hideAllButtonsAndDialogs()
		JSHelpers::showElementById("saveAdminConfig")

		pageControl.setView(
			'''<div style="width:300px; margin:0 auto;">
				<form style="display: inline-block;" class='form' role='form' id='adminConfigurationForm'>
					<div class='form-group'>
					<br />
					<h3>Tutorial Settings</h3>
					<br />
					<label for='languages'>Tutorial Language:</label>			
					<br />
					<font size="2">(Change the tutorial language)</font>
					<br />
					«createLanguageCombobox()»
					<br />
					<h3>Experiment Mode Settings</h3>
					<br />
					<label for='experiment'>Experiment Mode:</label>
					<br />
					<font size="2">(Activate the Experiment Mode?)</font>
					<br />
					«createBooleanIdCombobox("experiment", false)»
					<br />
					<label for='skip'>Allow Skip:</label>
					<br />
					<font size="2">(Allow to skip questions?)</font>
					<br />
					«createBooleanIdCombobox("skip", false)»
					</div></form></br>
						<button id="saveAdminConfig" type="button" class="btn btn-default btn-sm">
		<span class="glyphicon glyphicon-floppy-disk"></span> Save</button></div>'''.
				toString())

		ConfigurationPageJS::init()
		Experiment::tutorial = false
		ExperimentTools::toolsMode = false
	}

	def createIdNumberInput(String id, int min) {
		'''<input class='form-control' name="«id»" id="«id»" style="width:100px;" min="«min»">'''
	}

	def createBooleanIdCombobox(String id, boolean selectedValue) {
		val possibilities = new ArrayList<String>
		possibilities.add("true")
		possibilities.add("false")
		createIdCombobox(possibilities, id, "width: 100px;", if (selectedValue) 0 else 1)
	}

	def getLanguages() {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getLanugages(new LanguagesCallback())
	}

	def createLanguageCombobox() {
		if (languages != null) {
			createIdCombobox(languages, "languages", "width: 100px;", 0)
		} else {
			var english = new ArrayList<String>()
			english.add("english")
			createIdCombobox(english, "languages", "width:100px;", 0)
		}
	}

	def protected createIdCombobox(List<String> possibilities, String id, String style, int selectedIndex) {
		'''<select class='form-control' name="«id»" id="«id»" style="«style»">
		«FOR i : 0 .. possibilities.size - 1»
			<option «if (i == selectedIndex) "selected"» value="«possibilities.get(i)»">«possibilities.get(i)»</option>
		«ENDFOR»
		</select>'''
	}

	static def saveConfiguration(String config) {
		var String[] configList = config.split("&")
		var String language = configList.get(0).substring("languages=".length)
		var boolean experiment = configList.get(1).substring("experiment=".length).equals("true")
		var boolean skip = configList.get(2).substring("skip=".length).equals("true")
		val ConfigurationServiceAsync configService = GWT::create(typeof(ConfigurationService))
		val endpoint = configService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "configurationservice"
		configService.saveConfiguration(language, experiment, skip, new VoidCallback())
	}
}

class LanguagesCallback implements AsyncCallback<String[]> {

	override onFailure(Throwable caught) {
	}

	override onSuccess(String[] result) {
		ConfigurationPage.languages = new ArrayList<String>()
		var i = 0
		while (i < result.length) {
			var r = result.get(i)
			ConfigurationPage.languages.add(r)
			i = i + 1
		}
		ExperimentJS::fillLanguageSelect(result)
	}
}
