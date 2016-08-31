package explorviz.visualization.experiment.tools

import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.EventListener
import com.google.gwt.user.client.Window
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.callbacks.StringCallback
import explorviz.visualization.experiment.callbacks.VoidFuncCallback
import explorviz.visualization.experiment.services.JSONServiceAsync
import explorviz.visualization.main.ExplorViz
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.main.PageControl
import explorviz.visualization.main.Util
import explorviz.visualization.view.IPage
import java.util.ArrayList
import static explorviz.visualization.experiment.Experiment.*
import static explorviz.visualization.experiment.Questionnaire.*
import static explorviz.visualization.experiment.tools.ExperimentSlider.*
import static explorviz.visualization.experiment.tools.ExperimentTools.*
import elemental.json.Json
import elemental.json.JsonObject
import explorviz.visualization.experiment.callbacks.ZipCallback
import java.util.Arrays

class ExperimentToolsPage implements IPage {

	var static JSONServiceAsync jsonService
	var static PageControl pc
	var static JsonObject jsonFilenameAndTitle
	var static String runningExperiment

	override render(PageControl pageControl) {

		pc = pageControl
		pc.setView("")

		JSHelpers::hideElementById("legendDiv")

		ExperimentTools::toolsModeActive = true

		jsonService = Util::getJSONService()
		jsonService.getExperimentTitlesAndFilenames(new StringCallback<String>([finishInit]))
	}

	def static void finishInit(String json) {		
		
		jsonFilenameAndTitle = Json.parse(json)
		var keys = new ArrayList<String>(Arrays.asList(jsonFilenameAndTitle.keys))

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
				«IF keys.size > 0»						
					«FOR i : 0 .. (keys.size - 1)»	
						<li id="«keys.get(i)»" class="expEntry">
							<div class="row">
								<div class="col-md-6">
									«jsonFilenameAndTitle.get(keys.get(i))»
								</div>
								<div class="col-md-6 expListButtons"> 
									<a class="expPlaySpan" id="expPlaySpan«i»">
										<span «getSpecificCSSClass(keys.get(i))» title="Start/Pause Experiment"></span>
									</a>
									<a class="expEditSpan" id="expEditSpan«i»">
										<span class="glyphicon glyphicon-cog" title="Edit experiment"></span>
									</a>
									<a class="expRemoveSpan" id="expRemoveSpan«i»">
										<span class="glyphicon glyphicon-remove-circle" title="Delete Experiment"></span>
									</a>
									<a class="expBlueSpan" id="expAddSpan«i»">
										<span class="glyphicon glyphicon-plus" title="Add questionnaire"></span>
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
	
//										<a class="expBlueSpan" id="expUserSpan«i»">
//										<span class="glyphicon glyphicon-user" title="User Management"></span>
//									</a>

	def static private setupChart() {

		ExperimentChartJS::showExpChart()

	}

	def static private setupButtonHandler() {

		val buttonAdd = DOM::getElementById("newExperimentBtn")
		Event::sinkEvents(buttonAdd, Event::ONCLICK)
		Event::setEventListener(buttonAdd, new EventListener {

			override onBrowserEvent(Event event) {
				showExperimentModal(null)
			}
		})
		
		
		val keys = new ArrayList<String>(Arrays.asList(jsonFilenameAndTitle.keys))
		
		for (var j = 0; j < keys.size; j++) {
			
			val filename = keys.get(j);

			val buttonRemove = DOM::getElementById("expRemoveSpan" + j)
			Event::sinkEvents(buttonRemove, Event::ONCLICK)
			Event::setEventListener(buttonRemove, new EventListener {
				override onBrowserEvent(Event event) {

					if (Window::confirm("Are you sure about deleting this file? It can not be restored."))
						jsonService.removeExperiment(filename, new VoidFuncCallback<Void>([loadExpToolsPage]))

				}
			})

			val buttonEdit = DOM::getElementById("expEditSpan" + j)
			Event::sinkEvents(buttonEdit, Event::ONCLICK)
			Event::setEventListener(buttonEdit, new EventListener {

				override onBrowserEvent(Event event) {					
					jsonService.getExperiment(filename, new StringCallback<String>([showExperimentModal]))
				}
			})

			val buttonPlay = DOM::getElementById("expPlaySpan" + j)
			Event::sinkEvents(buttonPlay, Event::ONCLICK)
			Event::setEventListener(buttonPlay, new EventListener {

				override onBrowserEvent(Event event) {
					if (runningExperiment != null && filename.equals(runningExperiment)) {
						stopExperiment()
					} else {
						startExperiment(filename)
					}

				}
			})

			val buttonDownload = DOM::getElementById("expDownloadSpan" + j)
			Event::sinkEvents(buttonDownload, Event::ONCLICK)
			Event::setEventListener(buttonDownload, new EventListener {

				override onBrowserEvent(Event event) {
					
					jsonService.downloadExperimentData(filename, new ZipCallback("experimentData.zip"))
					
				}
			})
			
			val buttonDuplicate = DOM::getElementById("expDuplicateSpan" + j)
			Event::sinkEvents(buttonDuplicate, Event::ONCLICK)
			Event::setEventListener(buttonDuplicate, new EventListener {

				override onBrowserEvent(Event event) {
					jsonService.duplicateExperiment(filename, new VoidFuncCallback<Void>([loadExpToolsPage]))
				}
			})

			val buttonDetailsModal = DOM::getElementById("expDetailSpan" + j)
			Event::sinkEvents(buttonDetailsModal, Event::ONCLICK)
			Event::setEventListener(buttonDetailsModal, new EventListener {

				override onBrowserEvent(Event event) {

					jsonService.getExperimentDetails(filename, new StringCallback<String>([showDetailsModal]))

				}
			})

//			val buttonUserModal = DOM::getElementById("expUserSpan" + j)
//			Event::sinkEvents(buttonUserModal, Event::ONCLICK)
//			Event::setEventListener(buttonUserModal, new EventListener {
//
//				override onBrowserEvent(Event event) {
//					jsonService.getExperiment(filename, new StringCallback<String>([showUserManagement]))
//				}
//			})

			val buttonAddQuest = DOM::getElementById("expAddSpan" + j)
			Event::sinkEvents(buttonAddQuest, Event::ONCLICK)
			Event::setEventListener(buttonAddQuest, new EventListener {

				override onBrowserEvent(Event event) {
					jsonService.getExperiment(filename, new StringCallback<String>([showQuestModal]))
				}
			})
			
			

		}
	}

	def static void startExperiment(String experimentFilename) {

		runningExperiment = experimentFilename

		ExperimentTools::toolsModeActive = false

		Experiment::experiment = true
		Questionnaire::experimentFilename = experimentFilename

		loadExpToolsPage()
	}

	def static void stopExperiment() {

		runningExperiment = null

		ExperimentTools::toolsModeActive = true

		Experiment::experiment = false
		Questionnaire::experimentFilename = null

		loadExpToolsPage()
	}

	def static void editExperiment(String jsonString) {

		ExperimentSlider::jsonExperiment = jsonString
		ExperimentSlider::isWelcome = true
		ExplorViz::getPageCaller().showExperimentSlider()

	}
	
	def static void addQuestionnaire(String jsonString) {

		ExperimentSlider::jsonExperiment = jsonString
		ExperimentSlider::isWelcome = false
		ExplorViz::getPageCaller().showExperimentSlider()

	}

	def static loadExpToolsPage() {
		ExplorViz::getPageCaller().showExpTools()
	}

	def static getQuestionText(int id) {

		return Questionnaire.questions.get(id).text

	}

	def static showNewExpWindow() {

		ExperimentSlider::jsonExperiment = null
		ExperimentSlider::isWelcome = true
		ExplorViz::getPageCaller().showExperimentSlider()

	}


	def static getSpecificCSSClass(String filename) {
		
		if (runningExperiment != null && filename.equals(runningExperiment)) {
			return '''class="glyphicon glyphicon-pause"'''
		} else {
			return '''class="glyphicon glyphicon-play"'''
		}
		
	}

	def static private prepareModal() {		
		
		var modal = " 
				<div class='modal fade' id='modalExp' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>
				  <div class='modal-dialog modal-dialog-center' role='document'>
				    <div class='modal-content'>
				      <div class='modal-header'>
				        <button type='button' class='close' data-dismiss='modal' aria-label='Close'>
				          <span aria-hidden='true'>&times;</span>
				        </button>
				        <h4 class='modal-title' id='myModalLabel'>Experiment details</h4>
				      </div>
				      <div id='exp-modal-body' class='modal-body'>
				        CONTENT HERE
				      </div>
				      <div id='exp-modal-footer' class='modal-footer'>
				        <button type='button' class='btn btn-secondary' data-dismiss='modal'>Close</button>
				      </div>
				    </div>
				  </div>
				</div>
		"
		
		ExperimentToolsPageJS::prepareModal(modal)
	}
	

	def static private showDetailsModal(String jsonDetails) {		
		
		var JsonObject jsonObj = Json.parse(jsonDetails)
		
		var body = '''
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
			  <tr>
			  	<th>Used landscapes:</th>
			  	<td>«jsonObj.getString("landscapes")»</td>
			  </tr>
			</table>
		'''

		ExperimentToolsPageJS::updateAndShowModal(body, false, jsonDetails)
		
	}
	
	def static private showExperimentModal(String jsonData) {	
		
		var JsonObject jsonObj = Json.createObject
		jsonObj.put("title","")
		jsonObj.put("prefix","")
		jsonObj.put("numQuestions","")
		jsonObj.put("landscapes","")
		
		if(jsonData != null) {
			
			jsonObj = Json.parse(jsonData)
			
		}
		
		var body = '''
			<p>Welcome to the Experiment Tools Question Interface.</p>
			<p>Please select an experiment title:</p>
			<table class='table table-striped'>
			  <tr>
			    <th>Experiment Title:</th>
			    <td>
			    	 <input id="experimentTitle" name="title" size="35" value="«jsonObj.getString("title")»">
				</td>
			  </tr>
			  <tr>
			    <th>Prefix:</th>
			    <td>
			    	<input id="experimentPrefix" name="prefix" size="35" value="«jsonObj.getString("prefix")»">
			    </td>
			  </tr>
			</table>
		'''

		ExperimentToolsPageJS::updateAndShowModal(body, true, jsonData)
		
	}
	
	def static private showQuestModal(String jsonData) {	
		
		var body = '''			
			<p>Please select an questionnaire title:</p>
			<table class='table table-striped'>
			  <tr>
			    <th>Questionnaire Title:</th>
			    <td>
			    	 <input id="questionnareTitle" name="questionnareTitle" size="35">
				</td>
			  </tr>
			  <tr>
			    <th>Prefix:</th>
			    <td>
			    	<input id="questionnarePrefix" name="questionnarePrefix" size="35">
			    </td>
			  </tr>
			</table>
		'''

		ExperimentToolsPageJS::updateAndShowModal(body, true, jsonData)
		
	}
	
	def static void saveToServer(String jsonExperiment) {

		jsonService.saveJSONOnServer(jsonExperiment, new VoidFuncCallback<Void>([loadExpToolsPage]))
	}

}
