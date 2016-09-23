package explorviz.visualization.experiment.tools

import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.EventListener
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.Questionnaire
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
import elemental.json.JsonArray
import explorviz.visualization.experiment.callbacks.JsonExperimentCallback
import explorviz.visualization.experiment.services.ConfigurationServiceAsync
import explorviz.visualization.experiment.callbacks.GenericFuncCallback
import explorviz.visualization.experiment.callbacks.VoidCallback
import com.google.gwt.core.client.Callback
import explorviz.visualization.engine.Logging

class ExperimentToolsPage implements IPage {

	var static JSONServiceAsync jsonService
	var static ConfigurationServiceAsync configService
	var static PageControl pc
	private var static JsonObject experimentsData
	var static String runningExperiment
	
	private var static String filenameExperiment
	private var static String questionnareID
	private var static String questionnaireTitle
	
	private static var JsonObject tempCallbackData

	override render(PageControl pageControl) {

		pc = pageControl
		pc.setView("")

		JSHelpers::hideElementById("legendDiv")
		JSHelpers::hideElementById("backToLandscapeBtn")

		ExperimentTools::toolsModeActive = true
		
		configService = Util::getConfigService()

		jsonService = Util::getJSONService()
		
		Util::tutorialService.getExperimentFilename(
			new GenericFuncCallback<String>(
				[ 
					String runningExperimentFilename |
					runningExperiment = runningExperimentFilename
					jsonService.getExperimentTitlesAndFilenames(
						new GenericFuncCallback<String>([showExperimentList])
					)
				]
			)
		)
	}

	def static void showExperimentList(String json) {
		
		experimentsData = Json.parse(json);
		var keys = new ArrayList<String>(Arrays.asList(experimentsData.keys))

		pc.setView(
			'''
			<div class = "container-fluid">
				<div class="row">					
					<div class="col-md-12">
						<ul>
							<li class="expHeader">
								<div class="">
									<div>
										Experiment&nbsp;name
									</div>
								</div>
							</li>
						«IF keys.size > 0»
							«FOR i : 0 .. (keys.size - 1)»	
								«var JsonObject experimentData = experimentsData.getObject(keys.get(i))»
								«var experimentTitle = experimentData.getString("title")»
								«var questionnaires = experimentData.getArray("questionnaires")»
								<li id="«keys.get(i)»" class="expEntry">
									<div class ="container-fluid">
										<div class="row">
											<div class="col-md-6 expListButtons">
												«experimentTitle»
											</div>
											<div class="col-md-6 expListButtons">
												<div class="dropdown col-md-3" style="position: relative; display: inline;">
													<a class="dropdown-toggle expBlueSpan" data-toggle="dropdown">
														<span class="glyphicon glyphicon-list"></span>
													</a>
													<ul class="dropdown-menu">
														<li><a id="expAddSpan«i»" >Add Questionnaire</a></li>
														<li class="divider"></li>												
														«IF questionnaires.length > 0»														
															«FOR j : 0 .. (questionnaires.length - 1)»
																<li class="dropdown-submenu">
																	«var JsonObject questionnaire = questionnaires.getObject(j)»
																	«var String questionnaireTitle = questionnaire.getString("questionnareTitle")»
																	<a>«questionnaireTitle»</a>
																	<ul class="dropdown-menu">
																		<li><a id="expShowQuestDetailsSpan«i.toString + j.toString»">Show Details</a></li>
																		<li><a id="expEditQuestSpan«i.toString + j.toString»">Edit Questionnaire</a></li>
																		<li><a id="expEditQuestionsSpan«i.toString + j.toString»">Edit Questions</a></li>
																		<li><a id="expUserManQuestSpan«i.toString + j.toString»">User Management</a></li>
																		<li><a id="expRemoveQuestSpan«i.toString + j.toString»">Remove Questionnaire</a></li>
																	</ul>
																</li>
															«ENDFOR»
														«ENDIF»
														</ul>
												</div>
												<div class ="col-md-9">
													<a class="expPlaySpan" id="expPlaySpan«i»">
														<span «getSpecificCSSClass(experimentData.getString("filename"))» title="Start/Pause Experiment"></span>
													</a>
													<a class="expEditSpan" id="expEditSpan«i»">
														<span class="glyphicon glyphicon-cog" title="Edit experiment"></span>
													</a>
													<a class="expRemoveSpan" id="expRemoveSpan«i»">
														<span class="glyphicon glyphicon-remove-circle" title="Delete Experiment"></span>
													</a>
													<a class="expBlueSpan" id="expDetailSpan«i»">
														<span class="glyphicon glyphicon-info-sign" title="More Details"></span>
													</a>
													<a class="expBlueSpan" id="expDownloadSpan«i»">
														<span class="glyphicon glyphicon-download" title="Download Experiment"></span>
													</a>
													<a class="expBlueSpan" id="expDuplicateSpan«i»">
														<span class="glyphicon glyphicon-retweet" title="Duplicate experiment"></span>
													</a>
												</div>
											</div>
										</div>
									</div>
								</li>
							«ENDFOR»
						«ENDIF»
						<button id="newExperimentBtn" type="button" style="margin-top:10px;" class="btn btn-default btn-sm">
							<span class="glyphicon glyphicon-plus"></span> Create New Experiment 
						</button>
						<div style="text-align: center;">
							<div id="experimentUpload" class="dropzone">
								Drag your <b>experiment</b> or click here for uploading to server.
							</div>
							<div id="landscapeUpload" class="dropzone">
								Drag your <b>landscape</b> or click here for uploading to server.
							</div>
						</div>
						</ul>
					</div>
				</div>	
			</div>		
			'''.toString())

		prepareModal()
		setupButtonHandler()
	}

	def static private setupChart(String jsonExperimentAndUsers) {
		
		var JsonObject data = Json.parse(jsonExperimentAndUsers)
		
		var JsonArray users = data.getArray("users")
		
		var int finished = 0
		var int remaining = 0
		
		var int length = users.length
		
		for(var i = 0; i < length; i++) {
			var JsonObject user = users.getObject(i)
			
			if(user.getBoolean("expFinished")) {
				finished++
			}
			else {
				remaining++
			}			
		}		

		ExperimentChartJS::showExpChart(finished, remaining)

	}

	def static private setupButtonHandler() {

		// create Experiment Button
		val buttonAdd = DOM::getElementById("newExperimentBtn")
		Event::sinkEvents(buttonAdd, Event::ONCLICK)
		Event::setEventListener(buttonAdd, new EventListener {

			override onBrowserEvent(Event event) {
				showCreateExperimentModal()
			}
		})
		
		// experiment button handlers
		var keys = new ArrayList<String>(Arrays.asList(experimentsData.keys))

		for (var j = 0; j < keys.size; j++) {
			
			val JsonObject experiment = experimentsData.getObject(j.toString)
			val filename = experiment.getString("filename")
			
			val questionnaires = experiment.getArray("questionnaires")

			val buttonRemove = DOM::getElementById("expRemoveSpan" + j)
			Event::sinkEvents(buttonRemove, Event::ONCLICK)
			Event::setEventListener(buttonRemove, new EventListener {
				override onBrowserEvent(Event event) {
					
					if(!isChangeable(filename)) {
						ExperimentToolsPageJS::showError("Error!", "Experiment is running.")
						return
					}
						
					
					var Callback<String,String> c = new Callback<String,String>() {
						
						override onFailure(String reason) {
							Logging::log(reason)
						}
						
						override onSuccess(String result) {
							jsonService.removeExperiment(filename, new GenericFuncCallback<Void>([loadExpToolsPage]))
						}						
					}
					ExperimentToolsPageJS::showWarningMessage("Are you sure about deleting this file?", "It can NOT be restored!", c)				
				}
			})

			val buttonEdit = DOM::getElementById("expEditSpan" + j)
			Event::sinkEvents(buttonEdit, Event::ONCLICK)
			Event::setEventListener(buttonEdit, new EventListener {

				override onBrowserEvent(Event event) {
					
					if(!isChangeable(filename)) {
						ExperimentToolsPageJS::showError("Error!", "Experiment is running.")
						return
					}
					
					jsonService.getExperiment(filename, new GenericFuncCallback<String>([showExperimentModal]))
				}
			})

			val buttonPlay = DOM::getElementById("expPlaySpan" + j)
			Event::sinkEvents(buttonPlay, Event::ONCLICK)
			Event::setEventListener(buttonPlay, new EventListener {			

				override onBrowserEvent(Event event) {
										
					filenameExperiment = filename			
					
					if (runningExperiment != null && filename.equals(runningExperiment)) {
						stopExperiment()
					} else {
						prepareStartExperiment(filename)
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
					jsonService.duplicateExperiment(filename, new GenericFuncCallback<Void>([loadExpToolsPage]))
				}
			})

			val buttonDetailsModal = DOM::getElementById("expDetailSpan" + j)
			Event::sinkEvents(buttonDetailsModal, Event::ONCLICK)
			Event::setEventListener(buttonDetailsModal, new EventListener {

				override onBrowserEvent(Event event) {
					jsonService.getExperimentDetails(filename, new GenericFuncCallback<String>([showDetailsModal]))
				}
			})

			val buttonAddQuest = DOM::getElementById("expAddSpan" + j)
			Event::sinkEvents(buttonAddQuest, Event::ONCLICK)
			Event::setEventListener(buttonAddQuest, new EventListener {

				override onBrowserEvent(Event event) {
					jsonService.getExperiment(filename, new GenericFuncCallback<String>([showCreateQuestModal]))
				}
			})

			// questionnaires button handler
			for (var i = 0; i < questionnaires.length; i++) {
				val JsonObject questionnaire = questionnaires.getObject(i);

				val buttonEditQuest = DOM::getElementById("expEditQuestSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonEditQuest, Event::ONCLICK)
				Event::setEventListener(buttonEditQuest,
					new EventListener {

						override onBrowserEvent(Event event) {
							
							if(!isChangeable(filename)) {
								ExperimentToolsPageJS::showError("Error!", "Experiment is running.")
								return
							}
							
							questionnareID = questionnaire.getString("questionnareID")							
							
							jsonService.getExperiment(filename,
								new GenericFuncCallback<String>([showQuestModal]))
						}
					})

				val buttonEditQuestions = DOM::getElementById("expEditQuestionsSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonEditQuestions, Event::ONCLICK)
				Event::setEventListener(buttonEditQuestions,
					new EventListener {

						override onBrowserEvent(Event event) {
							
							if(!isChangeable(filename)) {
								ExperimentToolsPageJS::showError("Error!", "Experiment is running.")
								return
							}
							
							filenameExperiment = filename							
							var JsonObject data = Json.createObject
							data.put("filename", filename)
							data.put("questionnareID", questionnaire.getString("questionnareID"))
							
							jsonService.getQuestionnaire(data.toJson,
								new GenericFuncCallback<String>([editQuestQuestions]))
						}
					})

				val buttonDetailsQuest = DOM::getElementById("expShowQuestDetailsSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonDetailsQuest, Event::ONCLICK)
				Event::setEventListener(buttonDetailsQuest, new EventListener {

					override onBrowserEvent(Event event) {
						
						filenameExperiment = filename
						
						var JsonObject data = Json.createObject
						data.put("filename", filename)
						data.put("questionnareID", questionnaire.getString("questionnareID"))

						jsonService.getQuestionnaireDetails(data.toJson, new GenericFuncCallback<String>([
							showQuestDetailsModal
						]))
					}
				})

				val buttonRemoveQuest = DOM::getElementById("expRemoveQuestSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonRemoveQuest, Event::ONCLICK)
				Event::setEventListener(buttonRemoveQuest, new EventListener {

					override onBrowserEvent(Event event) {
						
						if(!isChangeable(filename)) {
							ExperimentToolsPageJS::showError("Error!", "Experiment is running.")
							return
						}						

						var JsonObject data = Json.createObject
						data.put("filename", filename)
						data.put("questionnareID", questionnaire.getString("questionnareID"))
						
						tempCallbackData = data
						
						var Callback<String,String> c = new Callback<String,String>() {
						
							override onFailure(String reason) {
								Logging::log(reason)
							}
							
							override onSuccess(String result) {
								jsonService.removeQuestionnaire(tempCallbackData.toJson, new GenericFuncCallback<Void>([loadExpToolsPage]))
								tempCallbackData = null
							}						
						}
						ExperimentToolsPageJS::showWarningMessage("Are you sure about deleting this questionnaire?", "It can NOT be restored!", c)				
					}
				})

				val buttonUserModal = DOM::getElementById("expUserManQuestSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonUserModal, Event::ONCLICK)
				Event::setEventListener(buttonUserModal, new EventListener {
					
					override onBrowserEvent(Event event) {
						
							filenameExperiment = filename
							questionnareID = questionnaire.getString("questionnareID")
						
							var JsonObject data = Json.createObject
							data.put("filename", filename)
							data.put("questionnareID", questionnaire.getString("questionnareID"))
						
							jsonService.getExperimentAndUsers(data.toJson,
								new GenericFuncCallback<String>([showUserManagement]))
						}
					})
			}
		}
	}
	
	def private static isChangeable(String expFilename) {
		return !runningExperiment.equals(expFilename)
	}

	def static void prepareStartExperiment(String expFilename) {
		
		runningExperiment = expFilename
		
		jsonService.isExperimentReadyToStart(expFilename, new GenericFuncCallback<String>([startExperiment]))
	}
	
	def static void startExperiment(String status){
		
		if(status.equals("ready")) {
			ExperimentTools::toolsModeActive = false
	
			Experiment::experiment = true	
			
			configService.saveConfig("english", true, true, runningExperiment, new VoidCallback())
			
			loadExpToolsPage()
		} else {
			ExperimentToolsPageJS::showError("Couldn't start experiment!", status)
			runningExperiment = null
		}		
	}

	def static void stopExperiment() {
		
		ExperimentTools::toolsModeActive = true

		Experiment::experiment = false
		Questionnaire::experimentFilename = null
		runningExperiment = null
		
		configService.saveConfig("english", false, true, null, new VoidCallback())

		loadExpToolsPage()
	}

	def static void editQuestQuestions(String jsonQuestionnaire) {

		ExperimentSlider::filename = filenameExperiment
		ExperimentSlider::jsonQuestionnaire = jsonQuestionnaire
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

		ExperimentSlider::jsonQuestionnaire = null
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

		var JsonObject jsonObj = Json.parse(
			jsonDetails)

		var body = '''
			<table class='table table-striped'>
			  <tr>
			    <th>Title:</th>
			    <td>«jsonObj.getString("title")»</td>
			  </tr>
			  <tr>
			    <th>Number of Questionnaires:</th>
			    <td>«jsonObj.getString("numQuestionnaires")»</td>
			  </tr>
			  <tr>
			  	<th>Used landscapes:</th>
			  	<td>«jsonObj.getString("landscapes")»</td>
			  </tr>
			  	<tr>
				  	<th>Filename:</th>
					<td>
						<input class="form-control" id="experimentFilename" name="filename" size="35" value="«jsonObj.getString("filename")»" readonly>
					</td>
				</tr>
			</table>
		'''
		ExperimentToolsPageJS::updateAndShowModal(body, false, jsonDetails, false)
	}

	def static private showCreateExperimentModal() {

		var body = '''
			<p>Welcome to the Experiment Tools Question Interface.</p>
			<p>Please select an experiment title:</p>
			<table class='table table-striped'>
			  <tr>
			    <th>Experiment Title:</th>
			    <td>
			    	 <input class="form-control" id="experimentTitle" name="title" size="35">
				</td>
				 </tr>
			</table>
		'''

		ExperimentToolsPageJS::updateAndShowModal(body, true, null, false)

	}

	def static private showExperimentModal(String jsonData) {

		var JsonObject jsonObj = Json.createObject
		jsonObj.put("title", "")
		jsonObj.put("numQuestionnaires", "")
		jsonObj.put("landscapes", "")

		if (jsonData != null) {

			jsonObj = Json.parse(
				jsonData)

		}

		var body = '''
			<p>Welcome to the Experiment Tools Question Interface.</p>
			<p>Please select an experiment title:</p>
			<table class='table table-striped'>
			  <tr>
			    <th>Experiment Title:</th>
			    <td>
			    	 <input class="form-control" id="experimentTitle" name="title" size="35" value="«jsonObj.getString("title")»">
				</td>
				 </tr>
				 <tr>
				   <th>Filename:</th>
				   <td>
				   	<input class="form-control"id="experimentFilename" name="filename" size="35" value="«jsonObj.getString("filename")»" readonly>
				   </td>
				 </tr>
			</table>
		'''

		ExperimentToolsPageJS::updateAndShowModal(body, true, jsonData, false)

	}

	def static private showCreateQuestModal(String jsonData) {

		var body = '''			
			<p>Please select an questionnaire title:</p>
			<table class='table table-striped'>
				<tr>
				   	<th>Questionnaire Title:</th>
				   	<td>
				   		<input class="form-control" id="questionnareTitle" name="questionnareTitle" size="35">
					</td>
				</tr>
			</table>
		'''

		ExperimentToolsPageJS::updateAndShowModal(body, true, jsonData, false)

	}

	def static private showQuestModal(String jsonData) {

		var JsonObject data = Json.parse(jsonData)

		var String questionnareID = questionnareID
		var JsonObject experiment = Json.parse(data.getString(questionnareID))

		var questionnaires = experiment.getArray("questionnaires");

		var title = ""
		var id = ""

		for (var i = 0; i < questionnaires.length(); i++) {

			var JsonObject questionnaire = questionnaires.get(i)

			if (questionnaire.getString("questionnareID").equals(questionnareID)) {
				title = questionnaire.getString("questionnareTitle")
				id = questionnaire.getString(
					"questionnareID")
			}
		}

		var body = '''			
			<p>Please select an questionnaire title:</p>
			<table class='table table-striped'>
				<tr>
				   	<th>Questionnaire Title:</th>
				   	<td>
				   		<input class="form-control" id="questionnareTitle" name="questionnareTitle" size="35" value="«title»">
					</td>
				</tr>
				<tr>
					<th>ID:</th>
					<td>
					  	<input class="form-control" id="questionnareID" name="questionnareID" size="35" value="«id»" readonly>
					</td>
				</tr>
			</table>
		'''

		ExperimentToolsPageJS::updateAndShowModal(body, true, experiment.toJson, false)

	}

	def static private showQuestDetailsModal(String jsonQuestionnaireData) {

		var JsonObject data = Json.parse(
			jsonQuestionnaireData)

		var body = '''			
			<p>Questionnaire Details:</p>
			<table class='table table-striped'>
				<tr>
				   	<th>Questionnaire Title:</th>
				   	<td>
				   		<input class="form-control" id="questionnareTitle" name="questionnareTitle" size="35" value="«data.getString("questionnareTitle")»" readonly>
					</td>
				</tr>
				<tr>
					<th>ID:</th>
					<td>
					  	<input class="form-control" id="questionnareID" name="questionnareID" size="35" value="«data.getString("questionnareID")»" readonly>
					</td>
				</tr>
				<tr>
					<th>Number of Questions:</th>
					<td>
					  	<input class="form-control" id="questionnareNumQuestions" name="questionnareNumQuestions" size="35" value="«data.getString("numQuestions")»" readonly>
					</td>
				</tr>
				<tr>
					<th>Used Landscapes:</th>
					<td>
					  	<input class="form-control" id="questionnareLandscapes" name="questionnareLandscapes" size="35" value="«data.getString("landscapes")»" readonly>
					</td>
				</tr>
				<tr>
					<th>Number of Users:</th>
					<td>
					  	<input class="form-control" id="questionnareNumUsers" name="questionnareNumUsers" size="35" value="«data.getString("numUsers")»" readonly>
					</td>
				</tr>
				<tr>
					<th>Last started:</th>
					<td>
						<input class="form-control" id="questionnareStarted" name="questionnareStarted" size="35" value="«data.getString("started")»" readonly>
					</td>
				</tr>
				<tr>
					<th>Last finished:</th>
					<td>
						<input class="form-control" id="questionnareEnded" name="questionnareEnded" size="35" value="«data.getString("ended")»" readonly>
					</td>
				</tr>
			</table>
			<div id="expChartContainer">
				<canvas id="expChart"></canvas>
			</div>
		'''
		
		ExperimentToolsPageJS::updateAndShowModal(body, false, null, false)
		
		var JsonObject returnObj = Json.createObject
		returnObj.put("filename", filenameExperiment)
		returnObj.put("questionnareID", data.getString("questionnareID"))
						
		jsonService.getExperimentAndUsers(returnObj.toJson, new GenericFuncCallback<String>([setupChart]))
		

	}
	
	def static private showUserManagement(String jsonData) {
		
		var JsonObject data = Json.parse(jsonData)
		
		var questionnareID = data.getString("questionnareID")
		
		var JsonObject experiment = Json.parse(data.getString("experiment"))
		
		var JsonArray jsonUsers = data.getArray("users")

		var questionnaires = experiment.getArray("questionnaires");
		
		var JsonObject questionnaire

		for (var i = 0; i < questionnaires.length(); i++) {

			var JsonObject questionnaireTemp = questionnaires.get(i)

			if (questionnaireTemp.getString("questionnareID").equals(questionnareID)) {
				questionnaire = questionnaireTemp
			}
		}

		var body = '''			
			<p>Please select the number of users you want to create:</p>
			<table class='table table-striped'>
				<tr>
				   	<th>Number of users:</th>
				   	<td>
				   		<input type="number" min="0" class="form-control" id="userCount" name="userCount" size="35">
					</td>
				</tr>
				<tr>
					<th>ID:</th>
					<td>
					  	<input class="form-control" id="questionnareID" name="questionnareID" size="35" value="«questionnaire.getString("questionnareID")»" readonly>
					</td>
				</tr>
				 <tr>
				 	<th>Number of users:</th>
					<td>
					  	<input class="form-control" id="questionnareNumUsers" name="questionnareNumUsers" size="35" value="«jsonUsers.length»" readonly>
					</td>
				 </tr>
			</table>
			<table class="table" id="expUserList" style="text-align:center;">
				<thead>
					<tr>
						<th style="text-align:center;">Name</th>
						<th style="text-align:center;">Password</th>
						<th style="text-align:center;">Done</th>
						<th style="text-align:center;">Remove</th>
					 </tr>
				</thead>
				<tbody>
					«IF jsonUsers.length > 0»
						«FOR i : 0 .. (jsonUsers.length - 1)»								
							«var user = jsonUsers.getObject(i)»
							«var name = user.getString("username")»
							«var password = user.getString("pw")»
							<tr>
							    <td>«name»</td>
							    <td>«password»</td>
							    <td>
							    	«IF user.getBoolean("expFinished")»
							    		<span class="glyphicon glyphicon-ok"></span>
							    	«ENDIF»							    		
								</td>
							    <td>
									<input type="checkbox" id="expRemoveSingleUser«i»" name="«name»" value="«name»">
								</td>
							</tr>
				     	«ENDFOR»
				    «ENDIF»
				</tbody>
			</table>
		'''
		
		ExperimentToolsPageJS::updateAndShowModal(body, false, experiment.toJson, true)		
	}

	def static void saveToServer(String jsonExperiment) {
		jsonService.saveJSONOnServer(jsonExperiment, new GenericFuncCallback<Void>([loadExpToolsPage]))
	}
	
	def static void uploadExperiment(String jsonFile) {
		jsonService.uploadExperiment(jsonFile, new GenericFuncCallback<Void>([showUploadSuccessMessage]))
	}
	
	def static void uploadLandscape(String jsonFile) {
		jsonService.uploadLandscape(jsonFile, new GenericFuncCallback<Void>([showUploadSuccessMessage]))
	}
	
	def static void showUploadSuccessMessage() {
		ExperimentToolsPageJS::showSuccessMessage("Upload completed", "You can continue now.")
		loadExpToolsPage()
	}
	
	def static void createUsers(String prefix, int count) {
		jsonService.createUsersForQuestionnaire(count, prefix, new GenericFuncCallback<String>([updateUserModal]))
	}
	
	def static void removeUser(String[] users) {
				
		var data = Json.createObject
		data.put("filename", filenameExperiment)
		data.put("questionnareID", questionnareID)
		
		var length = users.length
		var JsonArray jsonUsers = Json.createArray
		
		for(var i = 0; i < length; i++){
			jsonUsers.set(i, users.get(i))
		}
		
		data.put("users", jsonUsers);
			
		jsonService.removeQuestionnaireUser(data.toJson, new GenericFuncCallback<String>([updateUserModal]))
	}
	
	def static updateUserModal(String userData) {
		
		var users = Json.parse(userData).getArray("users")
		
		var data = Json.createObject
		data.put("users", users)
		data.put("questionnaireTitle", questionnaireTitle)
		data.put("questionnareID", questionnareID)
		
		jsonService.getExperiment(filenameExperiment, new JsonExperimentCallback<String>([showUserManagement],data))
	}

}