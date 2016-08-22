package explorviz.visualization.experiment.tools

import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.EventListener
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.callbacks.StringCallback
import explorviz.visualization.experiment.callbacks.StringListCallback
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.main.ExplorViz
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.main.PageControl
import explorviz.visualization.main.Util
import explorviz.visualization.view.IPage
import java.util.ArrayList
import java.util.List

import static explorviz.visualization.experiment.tools.ExperimentTools.*
import explorviz.visualization.experiment.callbacks.VoidFuncCallback
import explorviz.visualization.experiment.Experiment
import com.google.gwt.user.client.Window
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.engine.Logging
import com.google.gwt.dom.client.Node

class ExperimentToolsPage implements IPage {

	var static JSONServiceAsync jsonService
	var static PageControl pc
	var static List<String> filteredNames
	var static String runningExperiment

	override render(PageControl pageControl) {

		pc = pageControl
		pc.setView("")

		JSHelpers::hideElementById("legendDiv")

		ExperimentTools::toolsModeActive = true

		jsonService = Util::getJSONService()
		jsonService.getExperimentNames(new StringListCallback<List<String>>([finishInit]))
	}

	def static void finishInit(List<String> names) {

		filteredNames = new ArrayList<String>()
		for (String s : names) {
			if (s.endsWith(".json"))
				filteredNames.add(s.split(".json").get(0));
		}
		
		prepareModal()

		pc.setView('''
			<div class="row">
				<div class="col-md-6" id="expChartContainer">
					<canvas id="expChart"></canvas>
				</div>
				<div class="col-md-6">
					<ul style="padding: 10px;">
						<li class="expHeader">
							<div class="container">
								<div>
									Experiment&nbsp;name
								</div>
							</div>
						</li>
				«IF filteredNames.size > 0»						
					«FOR i : 0 .. filteredNames.size-1»	
						<li class="expEntry">
							<div class="row">
								<div class="col-md-7">
									«filteredNames.get(i)»
								</div>
								<div class="col-md-5 expListButtons"> 
									<a class="expPlaySpan" id="expPlaySpan«i»">
										<span «getSpecificCSSClass(filteredNames.get(i))»></span>
									</a>									  	
									<a class="expEditSpan" id="expEditSpan«i»">
										<span class="glyphicon glyphicon-cog"></span>
									</a>
									<a class="expRemoveSpan" id="expRemoveSpan«i»">
										<span class="glyphicon glyphicon-remove-circle"></span>
									</a>
									<a class="expDownloadSpan" id="expDownloadSpan«i»">
										<span class="glyphicon glyphicon-download"></span>
									</a>
									<a class="expDownloadSpan" id="expDownloadSpan«i»" data-toggle="modal" data-target="#myModal">
										<span class="glyphicon glyphicon-info-sign"></span>
									</a>
								</div>
							</div>
						</li>
					«ENDFOR»
				«ENDIF»
						<button id="newExperimentBtn" type="button" style="display: block; margin-top:10px;" class="btn btn-default btn-sm">
							<span class="glyphicon glyphicon-plus"></span> Create New Experiment 
						</button>
					</ul>
				</div>
			</div>			
		'''.toString())
		
		setupButtonHandler()
		setupChart()
	}

	def static private setupChart() {

		ExperimentChartJS::showExpChart()

	}

	def static private setupButtonHandler() {

		val buttonAdd = DOM::getElementById("newExperimentBtn")
		Event::sinkEvents(buttonAdd, Event::ONCLICK)
		Event::setEventListener(buttonAdd, new EventListener {

			override onBrowserEvent(Event event) {
				showNewExpWindow()
			}
		})

		var i = 0
		for (name : filteredNames) {

			val buttonRemove = DOM::getElementById("expRemoveSpan" + i)
			Event::sinkEvents(buttonRemove, Event::ONCLICK)
			Event::setEventListener(buttonRemove, new EventListener {

				override onBrowserEvent(Event event) {
					if (Window::confirm("Are you sure about deleting this file? It can not be restored."))
						jsonService.removeExperiment(name, new VoidFuncCallback<Void>([reloadExpToolsPage]))
				}
			})

			val buttonEdit = DOM::getElementById("expEditSpan" + i)
			Event::sinkEvents(buttonEdit, Event::ONCLICK)
			Event::setEventListener(buttonEdit, new EventListener {

				override onBrowserEvent(Event event) {
					jsonService.getExperimentByName(name, new StringCallback<String>([editExperiment]))
				}
			})

			val buttonPlay = DOM::getElementById("expPlaySpan" + i)
			Event::sinkEvents(buttonPlay, Event::ONCLICK)
			Event::setEventListener(buttonPlay, new EventListener {

				override onBrowserEvent(Event event) {	
					if(runningExperiment != null && name.equals(runningExperiment)) {
						stopExperiment()
					}
					else {
						startExperiment(name)
					}		
					
				}
			})
			
			val buttonDownload = DOM::getElementById("expDownloadSpan" + i)
			Event::sinkEvents(buttonDownload, Event::ONCLICK)
			Event::setEventListener(buttonDownload, new EventListener {

				override onBrowserEvent(Event event) {
					
					jsonService.getExperimentByName(name, new StringCallback<String>([downloadExperiment]))
					
				}
			})

			i++
		}
	}

	def static void startExperiment(String landscapeFileName) {
		
		runningExperiment = landscapeFileName

		ExperimentTools::toolsModeActive = false

		Experiment::experiment = true
		Questionnaire::landscapeFileName = landscapeFileName
		
		reloadExpToolsPage()
	}
	
	def static void stopExperiment() {
		
		runningExperiment = null

		ExperimentTools::toolsModeActive = true

		Experiment::experiment = false
		Questionnaire::landscapeFileName = null
		
		reloadExpToolsPage()
	}

	def static void editExperiment(String jsonString) {
		
		ExperimentSlider::jsonExperiment = jsonString
		ExplorViz::getPageCaller().showExperimentSlider()
		
	}
	
	def static void downloadExperiment(String jsonString) {
		
		JSHelpers::downloadAsFile("experiment.json", jsonString)
		
	}

	def static void reloadExpToolsPage() {
		
		ExplorViz::getPageCaller().showExpTools()
		
	}

	def static getQuestionText(int id) {
		
		return Questionnaire.questions.get(id).text
		
	}

	def static showNewExpWindow() {
		
		ExperimentSlider::jsonExperiment = null
		ExplorViz::getPageCaller().showExperimentSlider()
		
	}
	
	def static getSpecificCSSClass(String name) {

		if(runningExperiment != null && name.equals(runningExperiment)) {
			return '''class="glyphicon glyphicon-pause"'''
		}
		
		else {
			return '''class="glyphicon glyphicon-play"'''
		}

	}
	
	def static private prepareModal() {	
		
		ExperimentToolsPageJS::prepareModal()
		
		
//		var modal = " 
//				<div class='modal fade' id='myModal' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>
//				  <div class='modal-dialog modal-dialog-center' role='document'>
//				    <div class='modal-content'>
//				      <div class='modal-header'>
//				        <button type='button' class='close' data-dismiss='modal' aria-label='Close'>
//				          <span aria-hidden='true'>&times;</span>
//				        </button>
//				        <h4 class='modal-title' id='myModalLabel'>Modal title</h4>
//				      </div>
//				      <div class='modal-body'>
//				        ...
//				      </div>
//				      <div class='modal-footer'>
//				        <button type='button' class='btn btn-secondary' data-dismiss='modal'>Close</button>
//				      </div>
//				    </div>
//				  </div>
//				</div>
//		"
		

	}
	

}
