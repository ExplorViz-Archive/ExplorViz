package explorviz.visualization.experiment.tools

import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.EventListener
import com.google.gwt.user.client.Window
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.callbacks.StringCallback
import explorviz.visualization.experiment.callbacks.StringListCallback
import explorviz.visualization.experiment.callbacks.VoidFuncCallback
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.main.ExplorViz
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.main.PageControl
import explorviz.visualization.main.Util
import explorviz.visualization.view.IPage
import java.util.ArrayList
import java.util.List
import static explorviz.visualization.experiment.Experiment.*
import static explorviz.visualization.experiment.Questionnaire.*
import static explorviz.visualization.experiment.tools.ExperimentSlider.*
import static explorviz.visualization.experiment.tools.ExperimentTools.*
import elemental.json.Json
import elemental.json.JsonObject
import explorviz.visualization.experiment.callbacks.ZipCallback

class ExperimentToolsPage implements IPage {

	var static JSONServiceAsync jsonService
	var static PageControl pc
	var static List<String> titles
	var static String runningExperiment

	override render(PageControl pageControl) {

		pc = pageControl
		pc.setView("")

		JSHelpers::hideElementById("legendDiv")

		ExperimentTools::toolsModeActive = true

		jsonService = Util::getJSONService()
		jsonService.getExperimentTitles(new StringListCallback<List<String>>([finishInit]))
	}

	def static void finishInit(List<String> names) {

		titles = new ArrayList<String>()
		for (String s : names) {
				titles.add(s);	
		}

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
				«IF titles.size > 0»						
					«FOR i : 0 .. titles.size-1»	
						<li class="expEntry">
							<div class="row">
								<div class="col-md-6">
									«titles.get(i)»
								</div>
								<div class="col-md-6 expListButtons"> 
									<a class="expPlaySpan" id="expPlaySpan«i»">
										<span «getSpecificCSSClass(titles.get(i))» title="Start/Pause Experiment"></span>
									</a>									  	
									<a class="expEditSpan" id="expEditSpan«i»">
										<span class="glyphicon glyphicon-cog" title="Edit experiment"></span>
									</a>
									<a class="expRemoveSpan" id="expRemoveSpan«i»">
										<span class="glyphicon glyphicon-remove-circle" title="Delete Experiment"></span>
									</a>
									<a class="expBlueSpan" id="expUserSpan«i»">
										<span class="glyphicon glyphicon-user" title="User Management"></span>
									</a>
									<a class="expBlueSpan" id="expDetailSpan«i»">
										<span class="glyphicon glyphicon-info-sign" title="More Details"></span>
									</a>
									<a class="expBlueSpan" id="expDownloadSpan«i»">
										<span class="glyphicon glyphicon-download" title="Download Experiment"></span>
									</a>
									<a class="expBlueSpan" id="expDuplicateSpan«i»">
										<span class="glyphicon glyphicon-retweet" title="Duplicate Experiment"></span>
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

		prepareModal()
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
		for (name : titles) {

			val buttonRemove = DOM::getElementById("expRemoveSpan" + i)
			Event::sinkEvents(buttonRemove, Event::ONCLICK)
			Event::setEventListener(buttonRemove, new EventListener {

				override onBrowserEvent(Event event) {

					if (Window::confirm("Are you sure about deleting this file? It can not be restored."))
						jsonService.removeExperiment(name, new VoidFuncCallback<Void>([loadExpToolsPage]))

				}
			})

			val buttonEdit = DOM::getElementById("expEditSpan" + i)
			Event::sinkEvents(buttonEdit, Event::ONCLICK)
			Event::setEventListener(buttonEdit, new EventListener {

				override onBrowserEvent(Event event) {
					jsonService.getExperimentByTitle(name, new StringCallback<String>([editExperiment]))
				}
			})

			val buttonPlay = DOM::getElementById("expPlaySpan" + i)
			Event::sinkEvents(buttonPlay, Event::ONCLICK)
			Event::setEventListener(buttonPlay, new EventListener {

				override onBrowserEvent(Event event) {
					if (runningExperiment != null && name.equals(runningExperiment)) {
						stopExperiment()
					} else {
						startExperiment(name)
					}

				}
			})

			val buttonDownload = DOM::getElementById("expDownloadSpan" + i)
			Event::sinkEvents(buttonDownload, Event::ONCLICK)
			Event::setEventListener(buttonDownload, new EventListener {

				override onBrowserEvent(Event event) {
					
					jsonService.getExperimentByTitle(name, new ZipCallback())
					
				}
			})
			
			val buttonDuplicate = DOM::getElementById("expDuplicateSpan" + i)
			Event::sinkEvents(buttonDuplicate, Event::ONCLICK)
			Event::setEventListener(buttonDuplicate, new EventListener {

				override onBrowserEvent(Event event) {
					jsonService.duplicateExperiment(name, new VoidFuncCallback<Void>([loadExpToolsPage]))
				}
			})

			val buttonDetailsModal = DOM::getElementById("expDetailSpan" + i)
			Event::sinkEvents(buttonDetailsModal, Event::ONCLICK)
			Event::setEventListener(buttonDetailsModal, new EventListener {

				override onBrowserEvent(Event event) {

					jsonService.getExperimentDetails(name, new StringCallback<String>([showDetails]))

				}
			})

			val buttonUserModal = DOM::getElementById("expUserSpan" + i)
			Event::sinkEvents(buttonUserModal, Event::ONCLICK)
			Event::setEventListener(buttonUserModal, new EventListener {

				override onBrowserEvent(Event event) {
					jsonService.getExperimentByTitle(name, new StringCallback<String>([showUserManagement]))
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

		loadExpToolsPage()
	}

	def static void stopExperiment() {

		runningExperiment = null

		ExperimentTools::toolsModeActive = true

		Experiment::experiment = false
		Questionnaire::landscapeFileName = null

		loadExpToolsPage()
	}

	def static void editExperiment(String jsonString) {

		ExperimentSlider::jsonExperiment = jsonString
		ExplorViz::getPageCaller().showExperimentSlider()

	}

	def static void loadExpToolsPage() {
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
		
		if (runningExperiment != null && name.equals(runningExperiment)) {
			return '''class="glyphicon glyphicon-pause"'''
		} else {
			return '''class="glyphicon glyphicon-play"'''
		}
		
	}

	def static private prepareModal() {		

		var modalExpDetails = " 
				<div class='modal fade' id='modalExpDetails' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>
				  <div class='modal-dialog modal-dialog-center' role='document'>
				    <div class='modal-content'>
				      <div class='modal-header'>
				        <button type='button' class='close' data-dismiss='modal' aria-label='Close'>
				          <span aria-hidden='true'>&times;</span>
				        </button>
				        <h4 class='modal-title' id='myModalLabel'>Experiment details</h4>
				      </div>
				      <div id='exp-modal-details-body' class='modal-body'>
				        CONTENT HERE
				      </div>
				      <div class='modal-footer'>
				        <button type='button' class='btn btn-secondary' data-dismiss='modal'>Close</button>
				      </div>
				    </div>
				  </div>
				</div>
		"
		
		var modalExpUserManagement = " 
				<div class='modal fade' id='modalExpUserManagement' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>
				  <div class='modal-dialog modal-dialog-center' role='document'>
				    <div class='modal-content'>
				      <div class='modal-header'>
				        <button type='button' class='close' data-dismiss='modal' aria-label='Close'>
				          <span aria-hidden='true'>&times;</span>
				        </button>
				        <h4 class='modal-title' id='myModalLabel'>User management</h4>
				      </div>
				      <div id='exp-modal-user-body' class='modal-body'>
				        CONTENT HERE
				      </div>
				      <div class='modal-footer'>
				        <button type='button' class='btn btn-secondary' data-dismiss='modal'>Close</button>
				      </div>
				    </div>
				  </div>
				</div>
		"
		
		ExperimentToolsPageJS::prepareModal(modalExpDetails, modalExpUserManagement)
	}

	def static private showDetails(String jsonDetails) {
		
		var JsonObject jsonObj = Json.parse(jsonDetails)
		
		var template = '''
			<table class='table table-striped'>
			  <tr>
			    <th>Title:</th>
			    <td>«jsonObj.getString("title")»</td>
			  </tr>
			  <tr>
			    <th>Prefix:</th>
			    <td>«jsonObj.getString("prefix")»</td>
			  </tr>
			  <tr>
			    <th>Number of Questions:</th>
			    <td>«jsonObj.getString("numQuestions")»</td>
			  </tr>
			</table>
		'''

		ExperimentToolsPageJS::showDetailModal(template)
		
		}

	def static private showUserManagement(String modal) {

		ExperimentToolsPageJS::showUserModal(modal)

	}

}
