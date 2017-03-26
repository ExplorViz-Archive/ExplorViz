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
import static explorviz.visualization.experiment.Experiment.*
import static explorviz.visualization.experiment.Questionnaire.*
import static explorviz.visualization.experiment.tools.ExperimentSlider.*
import static explorviz.visualization.experiment.tools.ExperimentTools.*
import elemental.json.Json
import elemental.json.JsonObject
import explorviz.visualization.experiment.callbacks.ZipCallback
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
		Experiment::tutorial = false
		
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
		var failedExperiments = experimentsData.getArray("failingExperiments");
		var expData = experimentsData.getArray("experimentsData");

		pc.setView(
			'''
			<div class = "container-fluid">
				<div class="row">					
					<div class="col-md-12">
						<ul>
							<li class="expHeader">
								<div class="well well-sm" style="margin-bottom:10px;">
									<div>
										Overview of Experiments
									</div>
								</div>
							</li>
							<div style="overflow-y : scroll; height : 40vh;">
						«IF expData.length > 0»
							«FOR i : 0 .. (expData.length - 1)»	
								«var JsonObject experimentData = expData.getObject(i)»
								«var experimentTitle = experimentData.getString("title")»
								«var questionnaires = experimentData.getArray("questionnaires")»
								<li id="expEntry«i»" class="expEntry">
									<div class ="container-fluid">
										<div class="row">
											<div class="col-md-6 expListButtons">
												«experimentTitle»
												«IF experimentData.getString("lastTouched").equals("true")»	
												<span class="badge">last modified</span></a><br>
												«ENDIF»
											</div>
											<div class="col-md-6 expListButtons">
												<div class="dropdown col-md-3" style="position: relative; display: inline;">
													<a class="dropdown-toggle expBlueSpan" data-toggle="dropdown" style="cursor : default;">
														<span id="dropdown-span«i»" class="glyphicon glyphicon-list" style="cursor : pointer;"></span>
													</a>
													<ul class="dropdown-menu menu-position-fix" id="dropdown-menu«i»" style="position : fixed; width : 200px;">
														<li><a id="expAddSpan«i»">Create Questionnaire</a></li>
														<li class="divider"></li>												
														«IF questionnaires.length > 0»														
															«FOR j : 0 .. (questionnaires.length - 1)»
																<li class="dropdown-submenu">
																	«var JsonObject questionnaire = questionnaires.getObject(j)»
																	«var String questionnaireTitle = questionnaire.getString("questionnareTitle")»
																	<a style="word-wrap: break-word; white-space: normal;">«questionnaireTitle»</a>
																	<ul class="dropdown-menu" style="position : fixed;">
																		<li><a id="expShowQuestDetailsSpan«i.toString + j.toString»">Show Details</a></li>
																		<li><a id="expEditQuestSpan«i.toString + j.toString»">Edit Questionnaire</a></li>
																		<li><a id="expEditQuestionsSpan«i.toString + j.toString»">Question-Interface</a></li>
																		<li><a id="expUserManQuestSpan«i.toString + j.toString»">User Management</a></li>
																		
																		<li class="dropdown-submenu">
																			<a style="word-wrap: break-word; white-space: normal;">Special Settings</a>
																			<ul class="dropdown-menu" >
																				<li><a id="expEditStatQuestionsSpan«i.toString + j.toString»">Statistic Questions
																					«IF questionnaire.getBoolean("preAndPostquestions") == true»
																						<span class="glyphicon glyphicon-ok" title="Statistical Questions is true"></span>
																					«ENDIF»
																					</a></li>
																				<li><a id="expTrackEyesSpan«i.toString + j.toString»">Track Eyes
																					«IF questionnaire.getBoolean("eyeTracking") == true»
																						<span class="glyphicon glyphicon-ok" title="Eye Tracking is true"></span>
																					«ENDIF»
																					</a></li>
																				<li><a id="expRecScreenSpan«i.toString + j.toString»">Record Screen
																					«IF questionnaire.getBoolean("recordScreen") == true»
																						<span class="glyphicon glyphicon-ok" title="Recording Screen is true"></span>
																					«ENDIF»
																					</a></li>
																			</ul>
																		</li>
																			
																		<li><a id="expResultsSpan«i.toString + j.toString»">Results</a></li>
																		
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
						«ELSE»
							<div class="alert alert-info">
								<strong>Info!</strong> No experiments found. You may upload them via dropzone below.
							</div>
						«ENDIF»
						</div>
						</ul>
					</div>
				</div>	
			</div>
			<button id="newExperimentBtn" type="button" style="margin-top:10px; margin-bottom:20px;" class="btn btn-default btn-sm center-block">
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
			'''.toString())

		prepareModal(false)
		setupButtonHandler()
		ExperimentToolsPageJS::dropdownPositionFix()
		
		if(failedExperiments.length > 0)
			ExperimentToolsPageJS::showWarning("Failed Experiments", "Following Experiments did not pass the validation\n" + failedExperiments.toString())
	}
	
	def static togglePreAndPostquestions(String experimentFileName, String questionnaireID, boolean serverPreAndPostquestions) {
		//toggle and update preAndPostquestions 
		var preAndPostquestions = !serverPreAndPostquestions;
		toggleGlyphicon(serverPreAndPostquestions, "expEditStatQuestionsSpan", experimentFileName, questionnaireID);
		jsonService.setQuestionnairePreAndPostquestions(experimentFileName, questionnaireID, preAndPostquestions, new GenericFuncCallback<Void>([]));
		ExperimentToolsPageJS::showSuccessMessage("Option Statistical Questions", "The option for pre- and postquestions was set to " + preAndPostquestions.toString() + ".")
	}
	
	def static toggleEyeTracking(String experimentFileName, String questionnaireID, boolean serverEyeTracking) {
		//toggle and update eyeTracking
		var eyeTracking = !serverEyeTracking;
		toggleGlyphicon(serverEyeTracking, "expTrackEyesSpan", experimentFileName, questionnaireID);
		jsonService.setQuestionnaireEyeTracking(experimentFileName, questionnaireID, eyeTracking, new GenericFuncCallback<Void>([]))
		ExperimentToolsPageJS::showSuccessMessage("Option Eye Tracking", "The option for eye tracking was set to " + eyeTracking.toString() + ".")
	}
	
	def static toggleRecordScreen(String experimentFileName, String questionnaireID, boolean serverRecordScreen) {
		//toggle and update recordScreen
		var recordScreen = !serverRecordScreen;
		toggleGlyphicon(serverRecordScreen, "expRecScreenSpan", experimentFileName, questionnaireID);
		jsonService.setQuestionnaireRecordScreen(experimentFileName, questionnaireID, recordScreen, new GenericFuncCallback<Void>([]))
		ExperimentToolsPageJS::showSuccessMessage("Option Recording Screen", "The option for eye tracking was set to " + recordScreen.toString() + ".")
	}
	
	def static toggleGlyphicon(boolean remove, String setting, String experimentFileName, String questionnaireID) {
		//get exact id of the menuitem
		jsonService.getExperimentTitlesAndFilenames(new GenericFuncCallback<String>([
			String jsonData |
			var expData = Json.parse(jsonData).getArray("experimentsData");
			for (var i = 0; i < expData.length(); i++) {
				if(expData.getObject(i).getString("filename") == experimentFileName) {
					var questionnaires = expData.getObject(i).getArray("questionnaires")
					for(var j = 0; j < questionnaires.length(); j++) {
						if(questionnaires.getObject(j).getString("questionnareID") == questionnaireID) {
							ExperimentToolsPageJS::toggleGlyphicon(remove, setting+i.toString()+j.toString())
						}
					}
				}
			}
		])
		)
		
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
		var keys = experimentsData.getArray("experimentsData").length

		for (var j = 0; j < keys; j++) {
			
			val JsonObject experiment = experimentsData.getArray("experimentsData").getObject(j)
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
				
				val buttonEyeTrackToggle = DOM::getElementById("expTrackEyesSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonEyeTrackToggle, Event::ONCLICK)
				Event::setEventListener(buttonEyeTrackToggle, new EventListener {
					
					override onBrowserEvent(Event event) {
							
							if(runningExperiment != null) {
								ExperimentToolsPageJS::showError("Could not change option.", "Experiment is running")
								return;
							}
							
							filenameExperiment = filename
							questionnareID = questionnaire.getString("questionnareID")
						
							var JsonObject data = Json.createObject
							data.put("filename", filename)
							data.put("questionnareID", questionnaire.getString("questionnareID"))
							jsonService.getQuestionnaireEyeTracking(filenameExperiment, "", questionnareID, new GenericFuncCallback<Boolean>(
								[
									boolean serverEyeTracking |
									toggleEyeTracking(filenameExperiment, questionnareID, serverEyeTracking)
							]))
							}
					})
					
				val buttonScreenRecToggle = DOM::getElementById("expRecScreenSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonScreenRecToggle, Event::ONCLICK)
				Event::setEventListener(buttonScreenRecToggle, new EventListener {
					
					override onBrowserEvent(Event event) {
						
							if(runningExperiment != null) {
								ExperimentToolsPageJS::showError("Could not change option.", "Experiment is running")
								return;
							}
						
							filenameExperiment = filename
							questionnareID = questionnaire.getString("questionnareID")
						
							//get, toggle and update attribute recordScreen on the Server
							jsonService.getQuestionnaireRecordScreen(filenameExperiment, "", questionnareID, new GenericFuncCallback<Boolean>(
								[
									boolean serverRecordScreen |
									toggleRecordScreen(filenameExperiment, questionnareID, serverRecordScreen)
								]
							))
							}
					})
					
				val buttonStatisticalQuestionsToggle = DOM::getElementById("expEditStatQuestionsSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonStatisticalQuestionsToggle, Event::ONCLICK)
				Event::setEventListener(buttonStatisticalQuestionsToggle, new EventListener {
					
					override onBrowserEvent(Event event) {
						
							if(runningExperiment != null) {
								ExperimentToolsPageJS::showError("Could not change option.", "Experiment is running")
								return;
							}
						
							filenameExperiment = filename
							questionnareID = questionnaire.getString("questionnareID")
							
							jsonService.getQuestionnairePreAndPostquestions(filenameExperiment, "", questionnareID, new GenericFuncCallback<Boolean>(
								[
									boolean serverPreAndPostquestions |
									togglePreAndPostquestions(filenameExperiment, questionnareID, serverPreAndPostquestions)
								]
							))
							}
					})
					
				val buttonResultsReplayModal = DOM::getElementById("expResultsSpan" + j.toString + i.toString)
				Event::sinkEvents(buttonResultsReplayModal, Event::ONCLICK)
				Event::setEventListener(buttonResultsReplayModal, new EventListener {
					
					override onBrowserEvent(Event event) {
							
							filenameExperiment = filename
							questionnareID = questionnaire.getString("questionnareID")
						
							var JsonObject data = Json.createObject
							data.put("filename", filename)
							data.put("questionnareID", questionnaire.getString("questionnareID"))
						
							jsonService.getExperimentAndUsers(data.toJson,
								new GenericFuncCallback<String>([ String experimentData |
									selectUserForScreenRecordingReplayModal(experimentData)
								]))
						}
					})
			}
		}
	}
	
	def private static isChangeable(String expFilename) {
		return !runningExperiment.equals(expFilename)
	}

	def static void prepareStartExperiment(String expFilename) {
		
		if(runningExperiment != null && !runningExperiment.equals(expFilename)) {
			
			var Callback<String, String> c = new Callback<String, String>() {

				override onFailure(String reason) {
					Logging::log(reason)
				}

				override onSuccess(String result) {
					jsonService.isExperimentReadyToStart(expFilename, new GenericFuncCallback<String>([String s | startExperiment(s, expFilename)]))
				}
			}
			ExperimentToolsPageJS::showWarningMessage("Are you sure about starting this experiment?",
				"Another experiment is running at the moment!", c)

		} else {		
			jsonService.isExperimentReadyToStart(expFilename, new GenericFuncCallback<String>([String s | startExperiment(s, expFilename)]))
		}
		
	}
	
	def static void startExperiment(String status, String filename){
		
		if(status.equals("ready")) {
			ExperimentTools::toolsModeActive = false			
	
			Experiment::experiment = true	
			
			runningExperiment = filename
			
			jsonService.setExperimentTimeAttr(runningExperiment, true, new VoidCallback())
			
			configService.saveConfig(true, true, runningExperiment, new VoidCallback())
			
			loadExpToolsPage()
		} else {
			ExperimentToolsPageJS::showError("Couldn't start experiment!", status)
		}		
	}

	def static void stopExperiment() {
		
		ExperimentTools::toolsModeActive = true
		
		jsonService.setExperimentTimeAttr(runningExperiment, false, new VoidCallback())

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

	def static private prepareModal(boolean reset) {

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

		ExperimentToolsPageJS::prepareModal(modal, reset)
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
			    <th>Number of total users:</th>
			    <td>«jsonObj.getString("userCount")»</td>
			  </tr>
			  <tr>
			  	<th>Used landscapes:</th>
			  	<td>«jsonObj.getString("landscapes")»</td>
			  </tr>
			<tr>
			  	<th>Last started:</th>
			  	<td>«jsonObj.getString("lastStarted")»</td>
			</tr>
			<tr>
				<th>Last ended:</th>
				<td>«jsonObj.getString("lastEnded")»</td>
			</tr>
			<tr>
				<th>Last modified:</th>
				<td>«jsonObj.getString("lastModified")»</td>
			</tr>
			<tr>
			  	<th>Filename:</th>
				<td>
					<input class="form-control" id="experimentFilename" name="filename" size="35" value="«jsonObj.getString("filename")»" readonly>
				</td>
			</tr>
			</table>
		'''
		ExperimentToolsPageJS::updateAndShowModal(body, "Experiment Details",false, jsonDetails, false)
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

		ExperimentToolsPageJS::updateAndShowModal(body, "Create Experiment", true, null, false)
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

		ExperimentToolsPageJS::updateAndShowModal(body, "Edit Experiment", true, jsonData, false)

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

		ExperimentToolsPageJS::updateAndShowModal(body, "Create Questionnaire", true, jsonData, false)

	}

	def static private showQuestModal(String jsonData) {

		var JsonObject experiment = Json.parse(jsonData)

		var String questionnareID = questionnareID

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

		ExperimentToolsPageJS::updateAndShowModal(body, "Edit Questionnaire", true, experiment.toJson, false)

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
			</table>
			<div id="expChartContainer">
				<canvas id="expChart"></canvas>
			</div>
		'''
		
		ExperimentToolsPageJS::updateAndShowModal(body, "Questionnaire Details", false, null, false)
		
		var JsonObject returnObj = Json.createObject
		returnObj.put("filename", filenameExperiment)
		returnObj.put("questionnareID", data.getString("questionnareID"))
						
		jsonService.getExperimentAndUsers(returnObj.toJson, new GenericFuncCallback<String>([setupChart]))
		

	}
	

	def static private selectUserForScreenRecordingReplayModal(String jsonData) {		
		var JsonObject data = Json.parse(jsonData)
		var JsonArray jsonUsers = data.getArray("users")
		var JsonObject jsonExperiment = Json.parse(data.getString("experiment"))
		var experimentName = jsonExperiment.getString("filename")
		var JsonArray questionnaires = jsonExperiment.getArray("questionnaires");
		var questionnaireID = data.getString("questionnareID");	//exits in data for context
		var recordScreen = false;
		var eyeTracking = false;
		for (var i = 0; i < questionnaires.length(); i++) {
			var tempQuest = questionnaires.getObject(i);
			if(questionnaireID.equals(tempQuest.getString("questionnareID"))) {
				recordScreen = tempQuest.getBoolean("recordScreen");
				eyeTracking = tempQuest.getBoolean("eyeTracking");
			}
		}

		var body = '''			
			<table class="table" id="expUserList" style="text-align:center;">
							<thead>
								<tr>
									<th style="text-align:center;">Name</th>
									<th style="text-align:center;">Done</th>
									<th style="text-align:center;">Eye Tracking</th>
									<th style="text-align:center;">Screen Recording</th>
									<th style="text-align:center;">Download</th>
								 </tr>
							</thead>
							<tbody>
								«IF jsonUsers.length > 0»
									«FOR i : 0 .. (jsonUsers.length - 1)»								
										«var user = jsonUsers.getObject(i)»
										«var userName = user.getString("username")»
										<tr>
										    <td>«userName»</td>
										    <td>
										    	«IF user.getBoolean("expFinished")»
										    		<span class="glyphicon glyphicon-ok"></span>
										    	«ENDIF»							    		
											</td>
										    <td id='eyeTrackingSpan«userName»'>
											</td>
											<td name="«experimentName»">
												«IF recordScreen == true»
													<button id="resultsScreenRecording«i.toString()»" class="btn btn-default btn-sm" name="recordScreen«userName»" style="margin-bottom: 10px;"
													«IF user.getBoolean("expFinished") == false»
														disabled
													«ENDIF»	>
													<span class="glyphicon glyphicon-facetime-video"></span></button>
												«ENDIF»	
											</td>
											<td name="«experimentName»">
												<button id="expDownloadZip«i.toString()»" class="btn btn-default btn-sm" name="«userName»" style="margin-bottom: 10px;"
												«IF user.getBoolean("expFinished") != true»
													disabled
												«ENDIF»
												><span class="glyphicon glyphicon-download"></span></button>
											</td>
										</tr>
							     	«ENDFOR»
							    «ENDIF»
							</tbody>
						</table>
		'''
		
		ExperimentToolsPageJS::updateAndShowModal(body, "Select for Screen Record Replay", false, null, false)
		ExperimentToolsPageJS::setScreenRecordingButtonAction(jsonUsers.length)
		
		//check for each user if there is a eyeTracking file and videoRecords
		jsonService.getQuestionnairePrefix(jsonUsers.getObject(0).getString("username"), new GenericFuncCallback<String>([
			String questionnairePrefix |
			jsonService.existsFilesForAllUsers(questionnairePrefix, "/eyeTrackingData", new GenericFuncCallback<String>([
				String eyeTrackingDataMap |
				ExperimentToolsPageJS::setResultsEyeTrackingGlyphicons(eyeTrackingDataMap);
			]));
			jsonService.existsFilesForAllUsers(questionnairePrefix, "/screenRecords", new GenericFuncCallback<String>([
				String screenRecordsMap |
				ExperimentToolsPageJS::setResultsScreenRecordsGlyphicons(screenRecordsMap);
			]));
		]));
		
	}
		
	/**
	 * Shows a Modal to admin for selected user and its screen record as replay and if they are there, eye tracking data 
	 */	
	def static private showScreenRecReplayModal(String eyeTrackingData, String videoData) {
		var JsonObject data = Json.parse(eyeTrackingData)
		var JsonArray eyeData = null;
		if(data.hasKey("eyeData")) {
			eyeData = data.getArray("eyeData");
		}
		var double height = 720;
		if(data.hasKey("height")) {
			height = data.getNumber("height");
			if(height > 720) {
				height = height * 0.6;
			}
		}
		var double width = 1290;
		if(data.hasKey("width")) {
			width = data.getNumber("width");
			if(width > 1290) {
				width = width * 0.6
			}
		}
			
		var body = '''
			<div id='replayOverlay' height=800>
				<div id='videoContainer' >
					<video id='screenRecordVideoplayer' width=«width» height=«height» preload>
						<source src='«videoData»' type='video/mp4'>
					</video>
					<div id="video-controls">
						<div class="btn-group">
							<button type="button" class="btn btn-default" id="play-pause" width="5%">
								<span class="glyphicon glyphicon-play">
							</button>
							<button type="button" class="btn btn-default" id="eyeTrackingData" width="5%" 
							«IF eyeData.length() == 0 »
								disabled><span class="glyphicon glyphicon-eye-close">
							«ELSE»
								><span class="glyphicon glyphicon-eye-close">
							«ENDIF»
							</span></button>
						</div>
						<input type="range" id="seek-bar" value="0" width="90%">
					</div>
				</div>
				<canvas  width=«width» height=«height» id='eyeTrackingReplayCanvas' 
					style="position: absolute; top: 0; left: 0; z-index: 10;"></canvas>
			</div>
		'''
		
		
		ExperimentToolsPageJS::showVideoCanvasModal(body, "Screen Recording Replay");
		ExperimentToolsPageJS::startReplayMode(true, eyeTrackingData);
	}

	def static public startShowScreenRecReplayModal(String filenameExperiment, String userID) {
		//get data from server for eyeTrackingData and videoData for specific user
		jsonService.getEyeTrackingData(filenameExperiment, userID, new GenericFuncCallback<String>([
			String eyeTrackingData |
			jsonService.getScreenRecordData(filenameExperiment, userID, new GenericFuncCallback<String>([
			String videoData |
			showScreenRecReplayModal(eyeTrackingData, videoData)
			]))
		]))
	}	
	
	def static public downloadUserDataZip(String filenameExperiment, String userID) {
		jsonService.downloadDataOfUser(filenameExperiment, userID, new ZipCallback(userID+"Data.zip"))
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
				   	<th>Create number of users:</th>
				   	<td>
				   		<input type="number" min="0" class="form-control" id="userCount" name="userCount" size="35">
					</td>
				</tr>
				<tr>
					<th>Experiment title:</th>
					<td>
					  	<input class="form-control" id="experimentTitle" name="experimentTitle" size="35" value="«experiment.getString("title")»" readonly>
					</td>
				</tr>
				<tr>
					<th>Questionnaire title:</th>
					<td>
					  	<input class="form-control" id="questionnareTitle" name="questionnareTitle" size="35" value="«questionnaire.getString("questionnareTitle")»" readonly>
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
						<th id="removeCellHeader" style="text-align:center; cursor:pointer; background-color: lightsalmon;">Remove</th>
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
		
		ExperimentToolsPageJS::updateAndShowModal(body, "User Management", false, experiment.toJson, true)		
	}

	def static void saveToServer(String jsonExperiment) {
		jsonService.saveJSONOnServer(jsonExperiment, new GenericFuncCallback<Void>([loadExpToolsPage]))
	}
	
	//TODO upload file help
	def static void uploadExperiment(String jsonFile) {
		jsonService.uploadExperiment(jsonFile, new GenericFuncCallback<Boolean>(
			[
				Boolean status | 
				if(status) {
					showUploadSuccessMessage
				} else {
					ExperimentToolsPageJS::showError("Couldn't upload file!", "Please insert a valid file!")
				}				
			]
		))
	}
	
	def static void uploadLandscape(String jsonFile) {
		jsonService.uploadLandscape(jsonFile, new GenericFuncCallback<Boolean>(
			[
				Boolean status | 
				if(status) {
					showUploadSuccessMessage
				} else {
					ExperimentToolsPageJS::showError("Couldn't upload file!", "Please insert a valid file!")
				}				
			]
		))
	}
	
	def static void showUploadSuccessMessage() {
		ExperimentToolsPageJS::showSuccessMessage("Upload completed", "You can continue now.")
		loadExpToolsPage()
	}
	
	def static void createUsers(String prefix, int count) {
		jsonService.createUsersForQuestionnaire(count, prefix, filenameExperiment, new GenericFuncCallback<String>([String s | loadExpToolsPage updateUserModal(s)]))
	}
	
	def static void removeUser(String[] users) {
		
		if(runningExperiment != null) {
			ExperimentToolsPageJS::showError("Could not remove user(s).", "Experiment is running")
			return;
		}
				
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
	
	def static public removeLocalVideoData() {
		jsonService.removeLocalVideoData(new GenericFuncCallback<Void>([
			
		]))
	}
	
}