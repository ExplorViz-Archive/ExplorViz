package explorviz.visualization.experiment.tools;

import com.google.gwt.core.client.Callback;

public class ExperimentToolsPageJS {

	public static native void prepareModal(String modal) /*-{

		if ($wnd.jQuery("#modalExp").length == 0) {
			$wnd.jQuery("body").prepend(modal);
		}
		
		makeExperimentDropzoneClickable();
		makeLandscapeDropzoneClickable();
				
		function makeExperimentDropzoneClickable() {
			
			var input = $wnd.jQuery($doc.createElement('input'));
		
			$wnd.jQuery("#experimentUpload").click(function() {    
			    input.attr("type", "file");
			    input.trigger('click');
			});
			
			input.change(function(evt){
				evt.stopPropagation();
				evt.preventDefault();
				var data = evt.target.files;
				uploadLogicExperiment(data);
			});
		}
		
		function makeLandscapeDropzoneClickable() {
			
			var input = $wnd.jQuery($doc.createElement('input'));
		
			$wnd.jQuery("#landscapeUpload").click(function() {    
			    input.attr("type", "file");
			    input.trigger('click');
			});
			
			input.change(function(evt){
				evt.stopPropagation();
				evt.preventDefault();
				var data = evt.target.files;
				uploadLogicLandscape(data);
			});
		}

		var experimentUpload = $doc.getElementById('experimentUpload');

		experimentUpload.addEventListener('dragover', function(evt) {
			evt.stopPropagation();
			evt.preventDefault();
			evt.dataTransfer.dropEffect = 'copy';
		}, false);

		experimentUpload.addEventListener('drop', function(evt) {
			
			evt.stopPropagation();
			evt.preventDefault();			
			var data = evt.dataTransfer.files;			
			uploadLogicExperiment(data);
			
		}, false);
		
		function uploadLogicExperiment(data) {
		
			if(!data[0].name.endsWith(".json")) {
				$wnd.swal({
							title: "No valid data!",
							text: "Please insert a valid ExplorViz experiment.",
							type: "error"
						});
			} 
			else {
			
				$wnd.swal({
					title: "Do you want to upload this file?",
					type: "info",
					showCancelButton: true,
					closeOnConfirm: false,
					showLoaderOnConfirm: true
				},
				function(){
					upload()
					}
				);
				
				function upload() {
							
					var uploadFile = data[0];
					var reader = new FileReader();	
					var senddata = new Object();
					
					senddata.filename = uploadFile.name;
					senddata.date = uploadFile.lastModified;
					senddata.size = uploadFile.size;
					senddata.type = uploadFile.type;
		
					reader.onload = function(fileData) {
						senddata.fileData = fileData.target.result;
						@explorviz.visualization.experiment.tools.ExperimentToolsPage::uploadExperiment(Ljava/lang/String;)(JSON.stringify(senddata));
					}
		
					reader.readAsDataURL(uploadFile);
									
				}
			}		
		}
		
		
		var landscapeUpload = $doc.getElementById('landscapeUpload');

		landscapeUpload.addEventListener('dragover', function(evt) {
			evt.stopPropagation();
			evt.preventDefault();
			evt.dataTransfer.dropEffect = 'copy';
		}, false);

		landscapeUpload.addEventListener('drop', function(evt) {
			
			evt.stopPropagation();
			evt.preventDefault();
			var data = evt.dataTransfer.files;
			uploadLogicLandscape(data);
						
		}, false);
		
		function uploadLogicLandscape(data){
		
			if(!data[0].name.endsWith(".expl")) {
				$wnd.swal({
							title: "No valid data!",
							text: "Please insert a valid ExplorViz landscape.",
							type: "error"
						});
			} 
			else {
			
				$wnd.swal({
					title: "Do you want to upload this file?",
					type: "info",
					showCancelButton: true,
					closeOnConfirm: false,
					showLoaderOnConfirm: true
				},
				function(){
					upload()
					}
				);
				
				function upload() {
							
					var uploadFile = data[0];
					var reader = new FileReader();	
					var senddata = new Object();
					
					senddata.filename = uploadFile.name;
					senddata.date = uploadFile.lastModified;
					senddata.size = uploadFile.size;
					senddata.type = uploadFile.type;
		
					reader.onload = function(fileData) {
						console.log(fileData)
						senddata.fileData = fileData.target.result;
						@explorviz.visualization.experiment.tools.ExperimentToolsPage::uploadLandscape(Ljava/lang/String;)(JSON.stringify(senddata));
					}
		
					reader.readAsDataURL(uploadFile);
									
				}
			}		
		}

	}-*/;

	public static native void updateAndShowModal(final String body, final boolean needsSaveButton,
			final String jsonExperiment, final boolean isUserManagement) /*-{

		$wnd.jQuery("#exp-modal-body").html(body);

		var closeButton = $wnd.jQuery('<button/>', {
			text : 'Close',
			'class' : 'btn btn-secondary',
			'data-dismiss' : 'modal'
		});

		var saveButton = $wnd.jQuery('<button/>', {
			text : 'Save',
			'class' : 'btn btn-secondary',
			click : function() {
				save($wnd.jQuery('#exp-modal-body :input').serializeArray())
			}
		});
		
		var userManButton = $wnd.jQuery('<button/>', {
			text : 'Create Users',
			'class' : 'btn btn-secondary',
			click : function() {
				saveUsers($wnd.jQuery('#exp-modal-body :input').serializeArray())
			}
		});
		
		var userDeleteButton = $wnd.jQuery('<button/>', {
			text : 'Delete Users',
			'class' : 'btn btn-danger',
			click : function() {
				removeUsers($wnd.jQuery('#exp-modal-body :input[type=checkbox]:checked').serializeArray())
			}
		});

		$wnd.jQuery("#exp-modal-footer").html("");

		if (needsSaveButton)
			$wnd.jQuery("#exp-modal-footer").append(saveButton);
			
		if (isUserManagement) {
			$wnd.jQuery("#exp-modal-footer").append(userManButton);	
			$wnd.jQuery("#exp-modal-footer").append(userDeleteButton);				
		}

		$wnd.jQuery("#exp-modal-footer").append(closeButton);
		$wnd.jQuery("#modalExp").modal("show");
		// Fix background for scrolling
		$wnd.jQuery(".modal-backdrop").css("position","fixed");

		// save function
		function save(serializedInputs) {
			
			var isCompleted = true;

			var jsonObj = {};
			
			if(jsonExperiment)
				jsonObj = JSON.parse(jsonExperiment);				

			serializedInputs.forEach(function(element, index, array) {
				if(element.value == "") {
					isCompleted = false;
				}
				else {
					jsonObj[element.name] = element.value;
				}								
			});
				
			if(!jsonObj["filename"])
				jsonObj["filename"] = "exp_" + (new Date().getTime().toString()) + ".json";
				
			if(!jsonObj["questionnaires"]) {
				jsonObj["questionnaires"] = [];
			} 
			else if(!$wnd.jQuery("#experimentTitle").val()){
				var $questionnaireID = $wnd.jQuery("#questionnareID").val();
				
				if($questionnaireID) {
					// find questionnaire with this id and then update
					jsonObj["questionnaires"].forEach(function(el, index, array) {
						if (el.questionnareID.search($questionnaireID) == 0) {
							serializedInputs.forEach(function(element, index, array) {
																
								if(element.value == "") {
									isCompleted = false;
								}
								else {
									el[element.name] = element.value;
								}
																		
							});
						}
					});				
				} 
				else {
					// create new questionnaire
					var questionnaireObj = {};
					serializedInputs.forEach(function(element, index, array) {
												
						if(element.value == "") {
							isCompleted = false;
						}
						else {
							questionnaireObj[element.name] = element.value;	
						}
								
					});
					questionnaireObj["questionnareID"] = "quest" + (new Date().getTime().toString());
					questionnaireObj["questions"] = [];
					
					jsonObj["questionnaires"].push(questionnaireObj);
				}
			}
			
			if(isCompleted) {
				@explorviz.visualization.experiment.tools.ExperimentToolsPage::saveToServer(Ljava/lang/String;)(JSON.stringify(jsonObj));
				$wnd.jQuery("#modalExp").modal('toggle');
			}
			else {
				$wnd.swal({
					title: "Data missing!",
					text: "Please insert all data.",
					type: "error"
				});
			}
		}
		
		function saveUsers(serializedInputs) {
			
			var userCount = serializedInputs.find(function(object){
				return object.name == "userCount";
			});
			
			var jsonExp = JSON.parse(jsonExperiment);
			
			var prefix = jsonExp["prefix"] + "_";
			
			var $questionnaireID = $wnd.jQuery("#questionnareID").val();
			
			jsonExp["questionnaires"].forEach(function(el, index, array) {				
				if (el.questionnareID.search($questionnaireID) == 0) {
						prefix = prefix.concat(el.questionnarePrefix);
					};
			});
			
			$wnd.swal({
				title : "Be aware!",
				text : "You will only see the plain password in the following window once only.",
				type : "warning",
				showCancelButton : false,
				confirmButtonColor : "#8cd4f5",
				confirmButtonText : "I understand.",
				closeOnConfirm : true
			});				
			
			@explorviz.visualization.experiment.tools.ExperimentToolsPage::createUsers(Ljava/lang/String;I)(prefix, userCount.value);
		}
		
		function removeUsers(serializedInputs) {		
			var users = [];
			
			var length = serializedInputs.length;
			
			if(length == 0)
				return;
			
			for (var i = 0; i < length; i++) {				
			  	users.push(serializedInputs[i].value);
			}
			
			$wnd.swal({
				title: "Are you sure about deleting these users?",
				text: "You will not be able to recover their results!",
				type: "warning",
				showCancelButton: true,
				confirmButtonColor: "#DD6B55",
				confirmButtonText: "Yes, delete them!",
				closeOnConfirm: false
				}, 
				function(){
					$wnd.swal("Deleted!", "Users have been removed.", "success"); 
					@explorviz.visualization.experiment.tools.ExperimentToolsPage::removeUser([Ljava/lang/String;)(users);
				}
			);
		}

}-*/;

	public static native void showSuccessMessage(final String title,
			final String text) /*-{

		$wnd.swal({
			title : title,
			text : text,
			type : "success"
		});

	}-*/;

	public static native void showWarningMessage(final String title, final String text,
			Callback<String, String> c) /*-{

		$wnd
				.swal(
						{
							title : title,
							text : text,
							type : "warning",
							showCancelButton : true,
							confirmButtonColor : "#DD6B55",
							confirmButtonText : "Yes.",
							closeOnConfirm : false
						},
						function() {
							$wnd.swal("Completed!",
									"Your request was performed successfully.",
									"success");
							c.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)("success!");
						});

	}-*/;

	public static native void showWarning(final String title,
			final String text) /*-{

		$wnd.swal({
			title : title,
			text : text,
			type : "warning",
			showCancelButton : false,
			confirmButtonColor : "#DD6B55",
			confirmButtonText : "I understand.",
			closeOnConfirm : true
		});

	}-*/;

	public static native void showError(final String title,
			final String text) /*-{

		$wnd.swal({
			title : title,
			text : text,
			type : "error"
		});

	}-*/;

}
