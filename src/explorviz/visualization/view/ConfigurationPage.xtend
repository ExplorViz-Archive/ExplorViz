package explorviz.visualization.view

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.services.TutorialService
import explorviz.visualization.experiment.services.TutorialServiceAsync
import explorviz.visualization.main.Configuration
import explorviz.visualization.main.PageControl
import java.util.ArrayList
import java.util.List

import static explorviz.visualization.view.ConfigurationPage.*
import java.util.Collections

class ConfigurationPage implements IPage {
	
	static protected ArrayList<String> languages;
	
	override render(PageControl pageControl) {
	    Navigation::deregisterWebGLKeys()
	    
	    System.out.println("Testsyso")
	    System.err.println("Testsyse")
		pageControl.setView('''<table>
			<th>Name</th><th>Value</th>
			<tr><td>Show FPS</td><td>«createBooleanCombobox(Configuration::showFPS)»</td></tr>
			<tr><td>Tutorial Language </td><td>«createLanguageCombobox()»</td></tr>
		 </table>'''.toString())
	}
	
	def createLanguageCombobox() {
		val TutorialServiceAsync tutorialService = GWT::create(typeof(TutorialService))
		val endpoint = tutorialService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
		tutorialService.getLanugages(new LanguagesCallBack())
		createCombobox(languages,"width: 100px;", 0) 
		
//		var langs = new ArrayList<String>
//		langs.add("english")
//		langs.add("german")
//		createCombobox(langs, "width: 100px;", 0)
//		System.err.println("trying to build combobox")
//		val endpoint = tutorialService as ServiceDefTarget
//		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "tutorialservice"
//		tutorialService.getLanugages(new LanguagesCallBack())
//		if(languages.size() > 0){
//			createCombobox(languages,"width: 100px;", 0) 
//		}
	}

	def private createBooleanCombobox(boolean selectedValue) {
		val possibilities = new ArrayList<String>
		possibilities.add("true")
		possibilities.add("false")
		createCombobox(possibilities, "width: 100px;", if (selectedValue) 0 else 1 )
	}

	def private createCombobox(List<String> possibilities, String style, int selectedIndex) {
		'''<select style="«style»" onchange="alert(this.value)">
		«FOR i : 0 .. possibilities.size-1»
			<option « if (i == selectedIndex) "selected" » value="«possibilities.get(i)»">«possibilities.get(i).toFirstUpper»</option>
		«ENDFOR»
		</select>'''
	}
		
}

class LanguagesCallBack implements AsyncCallback<String[]> {
		
	override onFailure(Throwable caught) {
		System.err.println("Couldn't fetch languages")
	}
	
	override onSuccess(String[] result) {
		System.err.println("fetched languages")
		Collections.addAll(ConfigurationPage.languages, result);
	}
}