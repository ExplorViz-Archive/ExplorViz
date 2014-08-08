package explorviz.visualization.experiment;

public class ExperimentJS {

	public static native void fillLanguageSelect(String[] choices) /*-{
		var select = $doc.getElementById("languages");
		select.innerHTML = '';
		for ( var i = 0; i < choices.length; i++) {
			var opt = $doc.createElement('option');
			opt.value = choices[i];
			opt.innerHTML = choices[i];
			select.appendChild(opt);
		}
	}-*/;

	public static native void showTutorialDialog() /*-{
		$wnd.jQuery("#tutorialDialog").show();
		$wnd.jQuery("#tutorialDialog").dialog(
				{
					closeOnEscape : false,
					title : 'Tutorial',
					width : '500px',
					resizable : false,
					height : 'auto',
					dialogClass : "experimentPart",
					open : function(event, ui) {
						$wnd.jQuery(this).closest('.ui-dialog').find(
								'.ui-dialog-titlebar-close').hide();
					},
					position : {
						my : 'left top',
						at : 'left center',
						of : $wnd.jQuery("#view")
					}
				});
	}-*/;

	public static native void changeTutorialDialog(String text) /*-{
		$wnd.jQuery("#tutorialDialog").html("<p>" + text + "</p>");
		$wnd.jQuery("#tutorialDialog").height(250);
	}-*/;

	public static native void closeTutorialDialog() /*-{
		if ($wnd.jQuery("#tutorialDialog").hasClass('ui-dialog-content')) {
			$wnd.jQuery("#tutorialDialog").dialog('close');
		}
	}-*/;

	public static native void showTutorialContinueButton() /*-{
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', [ {
			text : 'Next >>',
			click : function() {
				@explorviz.visualization.experiment.Experiment::incStep()()
			},
			id : 'tutorialnextbutton'
		} ]);
		$wnd.jQuery("#tutorialnextbutton").css('float', 'right');
	}-*/;

	public static native void removeTutorialContinueButton() /*-{
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {});
	}-*/;

	public static native void showBackToLandscapeArrow() /*-{
		var div = $wnd.jQuery("#tutorialArrowLeft");
		div.show();
		div.style.display = 'block';
		div.style.top = '60px';
		div.style.left = '125px';
		$wnd.jQuery("#tutorialArrowDown").hide();
	}-*/;

	public static native void showTimshiftArrow() /*-{
		var top = $doc.getElementById("timeshiftChartDiv").style.top;
		var div = $doc.getElementById("tutorialArrowDown");
		$wnd.jQuery("#tutorialArrowDown").show();
		div.style.display = 'block';
		div.style.top = top;
		div.style.left = '70px';
		$wnd.jQuery("#tutorialArrowLeft").hide();
	}-*/;

	public static native void showChooseTraceArrow() /*-{
		@explorviz.visualization.experiment.ExperimentJS::hideArrows()();
		var button = $wnd.jQuery("#choose-trace-button1");
		var div = $wnd.jQuery("#tutorialArrowLeft").clone();
		div.appendTo($wnd.jQuery("#traceChooser_wrapper"));
		div.css('display', 'block');
		//get position
		var top = button.position().top + 'px';
		div.css('top', top);
		var left = button.position().left + (button.width()) + 'px';
		div.css('left', left);
		div.show();
	}-*/;

	public static native void showPlayPauseHighlightArrow() /*-{
		@explorviz.visualization.experiment.ExperimentJS::hideArrows()();
		var button = $wnd.jQuery("#traceReplayStartPause");
		var div = $wnd.jQuery("#tutorialArrowLeft").clone();
		div.attr('class', 'experimentPart tutorialNextArrow');
		div.css('display', 'block');
		div.show();
		div.appendTo($wnd.jQuery("#traceReplayerDialog"));
		//get position
		var top = button.position().top + 'px';
		div.css('top', top);
		var left = button.position().left + (button.width()) + 'px';
		div.css('left', left);
	}-*/;

	public static native void showNextHighlightArrow() /*-{
		@explorviz.visualization.experiment.ExperimentJS::hideArrows()();
		var button = $wnd.jQuery("#traceReplayNext");
		var div = $wnd.jQuery("#tutorialArrowLeft").clone();
		div.attr('class', 'experimentPart tutorialNextArrow');
		div.css('display', 'block');
		div.show();
		div.appendTo($wnd.jQuery("#traceReplayerDialog"));
		//get position
		var top = button.position().top + 'px';
		div.css('top', top);
		var left = button.position().left + (button.width()) + 'px';
		div.css('left', left);
	}-*/;

	public static native void hideArrows() /*-{
		$wnd.jQuery("#tutorialArrowLeft").hide();
		$wnd.jQuery("#tutorialArrowDown").hide();
		$wnd.jQuery(".tutorialPauseArrow").hide();
		$wnd.jQuery(".tutorialNextArrow").hide();
	}-*/;

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

	public static native void showQuestionDialogExtraVis() /*-{
		$wnd.jQuery("#questionDialog").show();
		$wnd.jQuery("#questionDialog").dialog(
				{
					closeOnEscape : false,
					title : 'Questionnaire',
					width : 500,
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

	public static native void changeQuestionDialog(String html, String language, String caption,
			boolean allowSkip) /*-{
		@explorviz.visualization.experiment.ExperimentJS::validationLanguage(Ljava/lang/String;)(language);
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.show();
		var timer = $wnd.jQuery("#questiontimer");
		$doc.getElementById("questionDialog").innerHTML = html;
		timer.appendTo("#questionDialog");
		qDialog.dialog('option', 'width', 400);
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
		$wnd.jQuery(".ui-dialog-buttonset").css('width', '100%');
		$wnd.jQuery("#questionSubmit").css('float', 'right');
		if (!allowSkip) {
			$wnd.jQuery("#skip").hide();
		}
		$wnd.jQuery("#skip").css('float', 'left');
		$wnd.jQuery("input,select").keypress(function(event) {
			if (event.which == 13) {
				event.preventDefault();
				$wnd.jQuery("#questionSubmit").trigger("click");
			}
		});
	}-*/;

	public static native void closeQuestionDialog() /*-{
		if ($wnd.jQuery("#questionDialog").hasClass('ui-dialog-content')) {
			$wnd.jQuery("#questionDialog").dialog('close');
		}
	}-*/;

	public static native void personalDataDialog(String html, String language) /*-{
		@explorviz.visualization.experiment.ExperimentJS::validationLanguage(Ljava/lang/String;)(language);
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'width', 400);
		qDialog.dialog('option', 'title', "Personal Information");
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd.jQuery("select").prop("selectedIndex", -1);
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
											@explorviz.visualization.experiment.Questionnaire::savePersonalInformation(Ljava/lang/String;)(res);
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
		$wnd.jQuery("input,select").keypress(function(event) {
			if (event.which == 13) {
				event.preventDefault();
				$wnd.jQuery("#questionSubmit").trigger("click");
			}
		});
	}-*/;

	public static native void experienceDataDialog(String html, String language) /*-{
		@explorviz.visualization.experiment.ExperimentJS::validationLanguage(Ljava/lang/String;)(language);
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'width', 400);
		qDialog.dialog('option', 'title', "Personal Information");
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd.jQuery("select").prop("selectedIndex", -1);
		$wnd.jQuery('span[data-toggle=popover]').popover();
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
											@explorviz.visualization.experiment.Questionnaire::saveExperienceInformation(Ljava/lang/String;)(res);
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
		$wnd.jQuery("input,select").keypress(function(event) {
			if (event.which == 13) {
				event.preventDefault();
				$wnd.jQuery("#questionSubmit").trigger("click");
			}
		});
	}-*/;

	public static native void introQuestionnaireDialog(String html) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'title', "Intro");
		$doc.getElementById("questionDialog").innerHTML = html;
		qDialog.dialog('option', 'width', 400);
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
		$wnd.jQuery(".ui-dialog-buttonset").css('width', '100%');
		$wnd.jQuery("#questionSubmit").css('float', 'right');
	}-*/;

	public static native void tutorialCommentDialog(String html, String language) /*-{
		@explorviz.visualization.experiment.ExperimentJS::validationLanguage(Ljava/lang/String;)(language);
		var qDialog = $wnd.jQuery("#questionDialog");
		$doc.getElementById("questionDialog").innerHTML = html;
		qDialog.dialog('option', 'width', 400);
		qDialog.dialog('option', 'title', "Debriefing Questionnaire");
		$wnd.jQuery("select").prop("selectedIndex", -1);
		$wnd.jQuery('span[data-toggle=popover]').popover();
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
											@explorviz.visualization.experiment.Questionnaire::saveTutorialComments(Ljava/lang/String;)(res);
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
		$wnd.jQuery("input,select").keypress(function(event) {
			if (event.which == 13) {
				event.preventDefault();
				$wnd.jQuery("#questionSubmit").trigger("click");
			}
		});
		$wnd.jQuery(".ui-dialog-buttonset").css('width', '100%');
		$wnd.jQuery("#questionSubmit").css('float', 'right');
	}-*/;

	public static native void explorvizCommentDialog(String html, String language) /*-{
		@explorviz.visualization.experiment.ExperimentJS::validationLanguage(Ljava/lang/String;)(language);
		var qDialog = $wnd.jQuery("#questionDialog");
		$doc.getElementById("questionDialog").innerHTML = html;
		qDialog.dialog('option', 'width', 400);
		qDialog.dialog('option', 'title', "Debriefing Questionnaire");
		$wnd.jQuery("select").prop("selectedIndex", -1);
		$wnd.jQuery('span[data-toggle=popover]').popover();
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
											@explorviz.visualization.experiment.Questionnaire::saveExplorVizComments(Ljava/lang/String;)(res);
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
		$wnd.jQuery("input,select").keypress(function(event) {
			if (event.which == 13) {
				event.preventDefault();
				$wnd.jQuery("#questionSubmit").trigger("click");
			}
		});
		$wnd.jQuery(".ui-dialog-buttonset").css('width', '100%');
		$wnd.jQuery("#questionSubmit").css('float', 'right');
	}-*/;

	public static native void finishQuestionnaireDialog(String html) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'title', "Almost done");
		$doc.getElementById("questionDialog").innerHTML = html;
		qDialog.dialog('option', 'width', 'auto');
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
		$wnd.jQuery(".ui-dialog-buttonset").css('width', '100%');
		$wnd.jQuery("#questionSubmit").css('float', 'right');
	}-*/;

	public static native void setTimer(String label)/*-{
		var timer = $wnd.jQuery("#questiontimer");
		timer.html(label);
		timer.css('display', 'block');
		timer.show();
	}-*/;

	public static native void hideTimer()/*-{
		$wnd.jQuery("#questiontimer").hide();
	}-*/;

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
