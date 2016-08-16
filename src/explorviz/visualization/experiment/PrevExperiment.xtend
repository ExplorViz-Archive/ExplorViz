package explorviz.visualization.experiment

import explorviz.visualization.main.PageControl
import explorviz.visualization.view.IPage
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.main.Util
import java.util.List
import explorviz.visualization.experiment.callbacks.StringListCallback
import explorviz.visualization.engine.Logging
import java.util.ArrayList
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.EventListener

class PrevExperiment implements IPage {

	var static JSONServiceAsync jsonService
	var static PageControl pc
	var static List<String> filteredNames

	override render(PageControl pageControl) {

		pc = pageControl

		pc.setView("")

		Experiment::tutorial = false
		ExperimentTools::toolsModeActive = true
		TutorialJS.closeTutorialDialog()
		TutorialJS.hideArrows()

		jsonService = Util::getJSONService()

		jsonService.getExperimentNames(new StringListCallback<List<String>>([finishInit]))
	}

	def static void finishInit(List<String> names) {

		filteredNames = new ArrayList<String>()
		for (String s : names) {
			if (s.endsWith(".json"))
				filteredNames.add(s.split(".json").get(0));
		}

		pc.setView('''
			<ul>
			
			<li class="expHeader">
			<div class="container">
			<div class="expElement">
			Experiment name
			</div>			
			</div>
			</li>
			
			«FOR i : 0 .. filteredNames.size-1»
				<li class="expEntry">
				   <div class="container">
				     <div class="expElement">
				       «filteredNames.get(i)»
				     </div>
				     <div class="expElement expListButtons">
				       <a id="expRemoveSpan«i»">
				       	<span class="glyphicon glyphicon-remove-circle"></span>
				       </a>
				     </div>
				     <div class="expElement expListButtons">
				       <a id="expEditSpan«i»">
				       	<span class="glyphicon glyphicon-cog"></span>
				       </a>
				     </div>
				      <div class="expElement expListButtons">
				      	 <a id="expPlaySpan«i»">
				      	 <span class="glyphicon glyphicon-play"></span>
				      	</a>
				     </div>
				   </div>
				</li>
				«ENDFOR»
				</ul>
		'''.toString())

		setupHandler()
	}

	def static private setupHandler() {

		for (var i = 0; i < filteredNames.size; i++) {

			val buttonRemove = DOM::getElementById("expRemoveSpan" + i)
			Event::sinkEvents(buttonRemove, Event::ONCLICK)
			Event::setEventListener(buttonRemove, new EventListener {

				override onBrowserEvent(Event event) {
					Logging::log(buttonRemove.toString)
				}
			})

			val buttonEdit = DOM::getElementById("expEditSpan" + i)
			Event::sinkEvents(buttonEdit, Event::ONCLICK)
			Event::setEventListener(buttonEdit, new EventListener {

				override onBrowserEvent(Event event) {
					Logging::log(buttonEdit.toString)
				}
			})

			val buttonPlay = DOM::getElementById("expPlaySpan" + i)
			Event::sinkEvents(buttonPlay, Event::ONCLICK)
			Event::setEventListener(buttonPlay, new EventListener {

				override onBrowserEvent(Event event) {
					Logging::log(buttonPlay.toString)
				}
			})
		}

	}

}
