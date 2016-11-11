package explorviz.visualization.experiment;

/**
 * @author Santje Finke
 *
 */
public class ExperimentJS {

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
			// use this event handler as eye tracker start trigger

			@explorviz.visualization.experiment.Questionnaire::continueAfterModal()()
		});

	}-*/;

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
	 * Displays the question dialog for usage with extravis (has slighly
	 * different positioning).
	 */
	public static native void showQuestionDialogExtraVis() /*-{
		$wnd.jQuery("#questionDialog").show();
		$wnd.jQuery("#questionDialog").dialog(
				{
					closeOnEscape : false,
					title : 'Questionnaire',
					width : 400,
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
						of : $wnd
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
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog()();
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

	public static native void showFirstDialog(String html, String language) /*-{
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
											@explorviz.visualization.experiment.Questionnaire::saveFirstForm(Ljava/lang/String;)(res);
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
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog()();
	}-*/;

	public static native void showSecondDialog(String html, String language) /*-{
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
											@explorviz.visualization.experiment.Questionnaire::saveSecondForm(Ljava/lang/String;)(res);
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
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog()();
	}-*/;

	public static native void showThirdDialog(String html) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'title', "Intro");
		qDialog.html(html);
		qDialog
				.dialog({
					buttons : [ {
						text : "Ok",
						click : function() {
							@explorviz.visualization.experiment.Questionnaire::introQuestionnaire()();
						},
						id : "questionSubmit"
					} ]
				});
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog()();
	}-*/;

	public static native void showForthDialog(String html, String language) /*-{
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
											@explorviz.visualization.experiment.Questionnaire::saveForthForm(Ljava/lang/String;)(res);
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
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog()();
	}-*/;

	public static native void showFifthDialog(String html, String language) /*-{
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
											@explorviz.visualization.experiment.Questionnaire::saveFifthForm(Ljava/lang/String;)(res);
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
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog()();
	}-*/;

	public static native void finishQuestionnaireDialog(String html) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'title', "Almost done");
		qDialog.html(html);
		qDialog
				.dialog({
					buttons : [ {
						text : "Ok",
						click : function() {
							@explorviz.visualization.experiment.Questionnaire::finishQuestionnaire()();
						},
						id : "questionSubmit"
					} ]
				});
		@explorviz.visualization.experiment.ExperimentJS::configureQuestionDialog()();
	}-*/;

	/**
	 * Sets basic configuration for the dialog: size, button position,
	 * popover-initialisation, changes selects to empty choices and changes
	 * "pressing enter" in selects or inputs to triggering the submit button
	 * instead of the default behaviour.
	 */
	public static native void configureQuestionDialog()/*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'width', 400);
		$wnd.jQuery("select").prop("selectedIndex", -1);
		$wnd.jQuery(".ui-dialog-buttonset").css('width', '100%');
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
	 * Adds functionality to the save buttons to add questions.
	 */
	public static native void initEditQuestions() /*-{
		$wnd
				.jQuery("#addQuestion")
				.on(
						"click touchstart",
						function() {
							var result = $wnd.jQuery("#editQuestionsForm")
									.serialize();
							$wnd.jQuery('#editQuestionsForm').each(function() {
								this.reset();
							});
							alert("Added question");
							@explorviz.visualization.experiment.EditQuestionsPage::saveQuestion(Ljava/lang/String;)(result);
						});
		$wnd
				.jQuery("#overwriteQuestions")
				.on(
						"click touchstart",
						function() {
							var result = $wnd.jQuery("#editQuestionsForm")
									.serialize();
							$wnd.jQuery('#editQuestionsForm').each(function() {
								this.reset();
							});
							alert("Overwritten questions");
							@explorviz.visualization.experiment.EditQuestionsPage::overwriteQuestions(Ljava/lang/String;)(result);
						});
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

}