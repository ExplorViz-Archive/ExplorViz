package explorviz.visualization.experiment;

/**
 * @author Santje Finke
 *
 */
public class ExperimentJS {

	/**
	 * Creates and adds a modal to the HTML DOM body of ExplorViz webpage
	 *
	 * @param name
	 *            String that is shown as name of the Experiment
	 * @param content
	 *            a String array containing text resources which are displayed
	 *            to the user
	 */
	public static native void showExperimentStartModal(String name,
			String[] content) /*-{

		var modal = "<div class='modal fade' id='modalExpStart' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>"
				+ "<div class='modal-dialog modal-dialog-center' role='document'>"
				+ "<div class='modal-content'>"
				+ "<div class='modal-header'>"
				+ "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>"
				+ "<span aria-hidden='true'>&times;</span>"
				+ "</button>"
				+ "<h4 align='center' class='modal-title' id='modalExpStartTitle'>Experiment details</h4>"
				+ "</div>"
				+ "<div id='exp-start-modal-body' class='modal-body' style='text-align: center;'>"
				+ "</div>"
				+ "<div id='exp-start-modal-footer' class='modal-footer' style='text-align: center;'>"
				+ "<button id='expStartModalStartButton' type='button' class='btn btn-secondary' data-dismiss='modal'>Start Experiment</button>"
				+ "</div>" + "</div>" + "</div>" + "</div>";

		if ($wnd.jQuery("#modalExpStart").length == 0) {
			$wnd.jQuery("body").prepend(modal);
			$wnd.jQuery("#modalExpStart").modal({
				backdrop : 'static',
				keyboard : false
			})
		}

		$wnd.jQuery("#modalExpStartTitle").text("Welcome");

		$wnd.jQuery("#exp-start-modal-body").html(
				content[0] + " <i>" + name + "</i>.<br/>" + content[1]);

		$wnd.jQuery("#modalExpStart").modal("show");

		$wnd.jQuery("#expStartModalStartButton").on("click", function(e) {
			@explorviz.visualization.experiment.Questionnaire::continueAfterModal()()
		});

	}-*/;

	/**
	 * Manipulates the dialog element of the ExplorViz website to show the name
	 * of the selected experiment in a modal
	 *
	 * @param name
	 *            String which gets shown in the dialog
	 */
	public static native void showExperimentNameDialog(String name) /*-{
		$wnd.jQuery("#experimentNameDialog").show();
		$wnd.jQuery("#experimentNameDialog").dialog(
				{
					closeOnEscape : false,
					title : 'Experiment title',
					width : 170,
					resizable : false,
					height : 80,
					dialogClass : "experimentPartCenter",
					open : function(event, ui) {
						$wnd.jQuery(this).closest('.ui-dialog').find(
								'.ui-dialog-titlebar-close').hide();
					},
					draggable : false,
					position : {
						my : 'center top',
						at : 'center top',
						of : $wnd.jQuery("#webglcanvas")
					}
				});
		$wnd.jQuery("#experimentNameDialog").html(name);
	}-*/;

	/**
	 * Fills the language-combobox with the possible options.
	 *
	 * @param choices
	 *            The possible languages
	 */
	public static native void fillLanguageSelect(String[] choices) /*-{
		var select = $doc.getElementById("languages");
		select.innerHTML = '';
		for (var i = 0; i < choices.length; i++) {
			var opt = $doc.createElement('option');
			opt.value = choices[i];
			opt.innerHTML = choices[i];
			select.appendChild(opt);
		}
	}-*/;

	/**
	 * Displays the question dialog.
	 */
	public static native void showQuestionDialog() /*-{
		$wnd.jQuery("#questionDialog").show();
		$wnd.jQuery("#questionDialog").dialog(
				{
					closeOnEscape : false,
					title : 'Questionnaire',
					width : 'auto',
					resizable : false,
					height : 'auto',
					dialogClass : "experimentPart",
					open : function(event, ui) {
						$wnd.jQuery(this).closest('.ui-dialog').find(
								'.ui-dialog-titlebar-close').hide();
					},
					position : {
						my : 'left top',
						at : 'left top',
						of : $wnd.jQuery("#webglcanvas")
					}
				});
	}-*/;

	/**
	 * Changes the content of the question dialog.
	 *
	 * @param html
	 *            The HTML to be displayed
	 * @param language
	 *            The language to be used for the validation
	 * @param caption
	 *            The caption of the dialog
	 * @param allowSkip
	 *            is the skip button displayed
	 */
	public static native void changeQuestionDialog(String html, String language, String caption,
			boolean allowSkip) /*-{
		@explorviz.visualization.experiment.ExperimentJS::validationLanguage(Ljava/lang/String;)(language);
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.show();
		var timer = $wnd.jQuery("#questiontimer");
		qDialog.html(html);
		timer.appendTo("#questionDialog");
		qDialog.dialog('option', 'title', caption);
		qDialog
				.dialog({
					buttons : [
							{
								text : "Skip",
								click : function() {
									@explorviz.visualization.experiment.Questionnaire::nextQuestion(Ljava/lang/String;)("");
								},
								id : "skip"
							},
							{
								text : "Next >>",
								click : function(e) {
									var qform = $wnd.jQuery("#questionForm");
									qform
											.validate({
												submitHandler : function(form) {
													var res = qform.serialize();
													@explorviz.visualization.experiment.Questionnaire::nextQuestion(Ljava/lang/String;)(res);
												},
												errorPlacement : function(
														error, element) {
													var elem = element.parent();
													while (elem.attr('id') != 'form-group') {
														elem = elem.parent();
													}
													error.appendTo(elem);
												},
												rules : {
													radio : "required",
													check : "required",
													input : "required",
													textarea : "required"
												},
												focusInvalid : false
											});
								},
								type : "submit",
								form : "questionForm",
								id : "questionSubmit"
							} ]
				});
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog(Ljava/lang/Boolean;)(false);
		if (!allowSkip) {
			$wnd.jQuery("#skip").hide();
		} else {
			$wnd.jQuery("#skip").css('float', 'left');
		}

	}-*/;

	/**
	 * Closes the question dialog.
	 */
	public static native void closeQuestionDialog() /*-{
		if ($wnd.jQuery("#questionDialog").hasClass('ui-dialog-content')) {
			$wnd.jQuery("#questionDialog").dialog('close');
		}
	}-*/;

	/**
	 * Shows the 50%-width-style dialog of prequestions
	 *
	 * @param html
	 *            All prequestions embedded in html-elements
	 * @param language
	 *            Language of the questions for error- or warning-messages
	 */
	public static native void showPrequestionDialog(String html, String language) /*-{
		@explorviz.visualization.experiment.ExperimentJS::validationLanguage(Ljava/lang/String;)(language);
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'title', "Personal Information");
		qDialog.html(html);
		qDialog
				.dialog({
					buttons : [ {
						text : "Ok",
						click : function() {
							var qform = $wnd.jQuery("#questionForm");
							qform
									.validate({
										submitHandler : function(form) {
											var res = qform.serialize();
											@explorviz.visualization.experiment.Questionnaire::savePrequestionForm(Ljava/lang/String;)(res);
										},
										errorPlacement : function(error,
												element) {
											var elem = element.parent();
											while (elem.attr('id') != 'form-group') {
												elem = elem.parent();
											}
											error.appendTo(elem);
										},
										rules : {
											radio : "required",
										},
										focusInvalid : false
									});
						},
						type : "submit",
						form : "questionForm",
						id : "questionSubmit"
					} ]
				});
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog(Ljava/lang/Boolean;)(true);
	}-*/;

	/**
	 * Shows the 50%-width style postquestions dialog
	 *
	 * @param html
	 *            All postquestions embedded in html-elements
	 * @param language
	 *            Language of the questions for error- or warning-messages
	 */
	public static native void showPostquestionDialog(String html, String language) /*-{
		@explorviz.visualization.experiment.ExperimentJS::validationLanguage(Ljava/lang/String;)(language);
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.html(html);
		qDialog.dialog('option', 'title', "Debriefing Questionnaire");
		qDialog
				.dialog({
					buttons : [ {
						text : "Ok",
						click : function() {
							var qform = $wnd.jQuery("#questionForm");
							qform
									.validate({
										submitHandler : function(form) {
											var res = qform.serialize();
											@explorviz.visualization.experiment.Questionnaire::savePostquestionForm(Ljava/lang/String;)(res);
										},
										errorPlacement : function(error,
												element) {
											var elem = element.parent();
											while (elem.attr('id') != 'form-group') {
												elem = elem.parent();
											}
											error.appendTo(elem);
										}
									});
						},
						type : "submit",
						form : "questionForm",
						id : "questionSubmit"
					} ]
				});
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog(Ljava/lang/Boolean;)(true);
	}-*/;

	/**
	 * Sets basic configuration for the dialog: size, button position,
	 * popover-initialisation, changes selects to empty choices and changes
	 * "pressing enter" in selects or inputs to triggering the submit button
	 * instead of the default behaviour.
	 *
	 * @param preOrPostquestions
	 *            is a boolean and expands the width of the dialog to 50% of the
	 *            screen when true
	 */
	public static native void configureQuestionDialog(Boolean preOrPostquestions)/*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		if (preOrPostquestions == true) {
			qDialog.dialog('option', 'width', '50%');
			qDialog.addClass("modal-dialog-center");
		} else {
			qDialog.dialog('option', 'width', '25%');
		}
		$wnd.jQuery("select").prop("selectedIndex", -1);
		$wnd.jQuery(".ui-dialog-buttonset").css('width', '40%');
		$wnd.jQuery("#questionSubmit").css('float', 'right');
		$wnd.jQuery('span[data-toggle=popover]').popover();
		$wnd.jQuery("input,select").keypress(function(event) {
			if (event.which == 13) {
				event.preventDefault();
				$wnd.jQuery("#questionSubmit").trigger("click");
			}
		});
	}-*/;

	/**
	 * Adds a timer to the question dialog.
	 *
	 * @param label
	 *            The display of the timer
	 */
	public static native void setTimer(String label)/*-{
		var timer = $wnd.jQuery("#questiontimer");
		timer.html(label);
		timer.css('display', 'block');
		timer.show();
	}-*/;

	/**
	 * Removes the timer from the qustion dialog.
	 */
	public static native void hideTimer()/*-{
		$wnd.jQuery("#questiontimer").hide();
	}-*/;

	/**
	 * Changes the language used by jquery validate.
	 *
	 * @param lang
	 *            The languge to use
	 */
	public static native void validationLanguage(String lang)/*-{
		if (lang == "german") {
			$wnd.jQuery
					.extend(
							$wnd.jQuery.validator.messages,
							{
								required : "Dieses Feld ist ein Pflichtfeld.",
								maxlength : $wnd.jQuery.validator
										.format("Geben Sie bitte maximal {0} Zeichen ein."),
								minlength : $wnd.jQuery.validator
										.format("Geben Sie bitte mindestens {0} Zeichen ein."),
								rangelength : $wnd.jQuery.validator
										.format("Geben Sie bitte mindestens {0} und maximal {1} Zeichen ein."),
								email : "Geben Sie bitte eine g&uuml;ltige E-Mail Adresse ein.",
								date : "Bitte geben Sie ein g&uuml;ltiges Datum ein.",
								number : "Geben Sie bitte eine Nummer ein.",
								digits : "Geben Sie bitte nur Ziffern ein.",
								equalTo : "Bitte denselben Wert wiederholen.",
								range : $wnd.jQuery.validator
										.format("Geben Sie bitte einen Wert zwischen {0} und {1} ein."),
								max : $wnd.jQuery.validator
										.format("Geben Sie bitte einen Wert kleiner oder gleich {0} ein."),
								min : $wnd.jQuery.validator
										.format("Geben Sie bitte einen Wert gr&ouml;&szlig;er oder gleich {0} ein."),
							});
		} else { //english as default
			$wnd.jQuery
					.extend(
							$wnd.jQuery.validator.messages,
							{
								required : "This field is required.",
								maxlength : $wnd.jQuery.validator
										.format("Please enter no more than {0} characters."),
								minlength : $wnd.jQuery.validator
										.format("Please enter at least {0} characters."),
								rangelength : $wnd.jQuery.validator
										.format("Please enter a value between {0} and {1} characters long."),
								email : "Please enter a valid email address.",
								date : "Please enter a valid date.",
								number : "Please enter a valid number.",
								digits : "Please enter only digits.",
								equalTo : "Please enter the same value again.",
								range : $wnd.jQuery.validator
										.format("Please enter a value between {0} and {1}."),
								max : $wnd.jQuery.validator
										.format("Please enter a value less than or equal to {0}."),
								min : $wnd.jQuery.validator
										.format("Please enter a value greater than or equal to {0}."),
							});
		}
	}-*/;

	/**
	 * Calls the Javascript function object to start the screen recording and
	 * eye tracking
	 *
	 * @param eyeTracking
	 *            is a boolean and decides whether tracking eyes or not
	 *            (questionnaire option)
	 * @param screenRecording
	 *            is a boolean and decides whether recording the users screen or
	 *            not (questionnaire option)
	 * @param userID
	 *            String with users ID
	 * @param questionnairePrefix
	 *            String containing the experiment and questionnaire names
	 */
	public static native void startEyeTrackingScreenRecording(boolean eyeTracking,
			boolean screenRecording, String userID,
			String questionnairePrefix)/*-{

		if (eyeTracking || screenRecording) {
			//create a JS instance of EyeTrackScreenRecordExperiment
			$wnd.EyeTrackScreenRecordExperiment(eyeTracking, screenRecording,
					userID, questionnairePrefix, saveToServer);

			//create function to save data to server
			function saveToServer(data) {
				@explorviz.visualization.experiment.Questionnaire::startUploadEyeTrackingData(Ljava/lang/String;)(data);
			}
		}

	}-*/;

	/**
	 * Triggers an event to stop eyeTracking and screen recording
	 */
	public static native void stopEyeTrackingScreenRecording()/*-{
		//trigger an event in the document, a handler stops the experiment 
		$wnd.triggerStopExperiment();
	}-*/;

	/**
	 * Setups a Javascript object to gather informations about the fileuplaod of
	 * the screen recording and if the questionnaire (main questions) were
	 * finished to stop the user from logging out before the upload is finished
	 */
	public static native void setupTryToFinishQuestionnaire() /*-{
		$wnd.uploadAndQuestionnaireFinished(finishAndCloseQuestionnaire);

		function finishAndCloseQuestionnaire() {
			@explorviz.visualization.experiment.Questionnaire::closeAndFinishExperiment()();
		}
	}-*/;

	/**
	 * Triggering event to notify Javascript function object
	 * uploadAndQuestionnaireFinished that the questionnaire is done
	 */
	public static native void tryToFinishQuestionnaire() /*-{
		$wnd.triggerQuestionnaireFinished();
	}-*/;

	/**
	 * Shows a sweetalert in type success (animation of a green checkmark) with
	 * parameter as text
	 * 
	 * @param response
	 *            String that gets shwon inside the alert
	 */
	public static native void showSwalSuccessResponse(String response) /*-{
		$wnd
				.swal(
						{
							title : "Response from Server",
							text : response,
							type : "success",
							closeOnConfirm : true,
						},
						function() {
							@explorviz.visualization.experiment.Questionnaire::closeAndFinishExperiment()();
						});
	}-*/;

	/**
	 * Shows a sweetalert in type success (animation of a green checkmark) with
	 * parameter as text
	 * 
	 * @param response
	 *            String that gets shwon inside the alert
	 */
	public static native void showSwalWarningResponse(String response) /*-{
		$wnd
				.swal(
						{
							title : "Response from Server",
							text : response,
							type : "warning",
							closeOnConfirm : true,
						},
						function() {
							@explorviz.visualization.experiment.Questionnaire::closeAndFinishExperiment()();
						});
	}-*/;

	/**
	 * Calls a Javascript function to show a sweetalert about starting the main
	 * questions (after the prequestions) and only starts with the callback
	 * continueFunction the first question and its timer In case of screen
	 * recording, a window pops up to ask for permission to record the screen
	 */
	public static native void showMainQuestionsStartModal() /*-{

		$wnd.showMainQuestionsStartDialog(continueFunction);
		function continueFunction() {
			@explorviz.visualization.experiment.Questionnaire::startMainQuestionsDialog()();
		}
	}-*/;

}