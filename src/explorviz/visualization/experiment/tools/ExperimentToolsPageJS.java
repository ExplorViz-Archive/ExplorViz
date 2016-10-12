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
						senddata.fileData = fileData.target.result;
						@explorviz.visualization.experiment.tools.ExperimentToolsPage::uploadLandscape(Ljava/lang/String;)(JSON.stringify(senddata));
					}
		
					reader.readAsDataURL(uploadFile);
									
				}
			}		
		}

	}-*/;

	public static native void dropdownPositionFix() /*-{

		$wnd.jQuery('.dropdown-toggle').click(
				function(ev) {

					if (!ev.target.id)
						return;

					var id = ev.target.id;
					var num = id.split("span");

					dropDownFixPosition($wnd.jQuery("[id=" + id + "]"), $wnd
							.jQuery('#dropdown-menu' + num[1]), ev.clientX,
							ev.clientY);
				});

		function dropDownFixPosition(button, dropdown, clientX, clientY) {

			dropdown.css('top', clientY + "px");
			dropdown.css('left', clientX + "px");

			var childX = clientX + 200;
			var childY = clientY + 30;

			dropdown.children(".dropdown-submenu").children(".dropdown-menu")
					.each(function(index, element) {

						$wnd.jQuery(this).css('top', childY + "px");
						$wnd.jQuery(this).css('left', childX + "px");

						childY += 30;
					});
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
		
		var printButton = $wnd.jQuery('<button/>', {
			text : 'Print users',
			'class' : 'btn btn-secondary',
			click : function() {
				var qTitle = $wnd.jQuery("#questionnareTitle").val();				
				var eTitle = $wnd.jQuery("#experimentTitle").val();
				$wnd.printJS({
					printable:'expUserList', 
					type:'html', 
					header : "ExplorViz users for: " + eTitle + "-" + qTitle
				});
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
			$wnd.jQuery("#exp-modal-footer").append(printButton);	
			$wnd.jQuery("#exp-modal-footer").append(userDeleteButton);		
			
			$wnd.jQuery("#removeCellHeader").css("font-weight", "bold");
			
			var checked = false;
			
			$wnd.jQuery("#removeCellHeader").on( "click", function() {
				
				var checkStatus = false;
				
				$wnd.jQuery('#exp-modal-body :input[type=checkbox]').each(function() {
				      	if(checked) {
				      		this.checked = false;
				      		checkStatus = false;
				      	} 
				      	else {
				      		this.checked = true;
				      		checkStatus = true;
				      	}
				      	
				      });
				      
				 checked = checkStatus;
			});	
					
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
				else if(!element.name.startsWith("quest")){
					jsonObj[element.name] = element.value;
				}								
			});
			
			var timestamp = new Date().getTime();
				
			if(!jsonObj["filename"])
				jsonObj["filename"] = "exp_" + timestamp + ".json";
				
			if(!jsonObj["lastModified"])
				jsonObj["lastModified"] = timestamp;	
				
			if(!jsonObj["lastStarted"])
				jsonObj["lastStarted"] = 0;
				
			if(!jsonObj["lastEnded"])
				jsonObj["lastEnded"] = 0;
				
			if(!jsonObj["ID"])
				jsonObj["ID"] = "exp" + timestamp;
				
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
			
			var prefix = jsonExp["ID"] + "_";
			
			var $questionnaireID = $wnd.jQuery("#questionnareID").val();
			
			jsonExp["questionnaires"].forEach(function(el, index, array) {				
				if (el.questionnareID.search($questionnaireID) == 0) {
						prefix = prefix.concat(el.questionnareID);
					};
			});
			
			if(!userCount.value || userCount.value <= 0) {
				@explorviz.visualization.experiment.tools.ExperimentToolsPageJS::showError(Ljava/lang/String;Ljava/lang/String;)("Insert a number!", "Only integers greater than zero are valid.")
			}
			else {
				@explorviz.visualization.experiment.tools.ExperimentToolsPageJS::showWarning(Ljava/lang/String;Ljava/lang/String;)("Be aware!", "You will only see the plain password in the following window once only. Any following action will hide the passwords. Print them now!")
				@explorviz.visualization.experiment.tools.ExperimentToolsPage::createUsers(Ljava/lang/String;I)(prefix, userCount.value);
			}			
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
