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
						my : 'center top',
						at : 'center center',
						of : $wnd.jQuery("#view")
					}
				});
	}-*/;

	public static native void changeTutorialDialog(String text) /*-{
		$doc.getElementById("tutorialDialog").innerHTML = '<p>' + text + '</p>';
	}-*/;

	public static native void closeTutorialDialog() /*-{
		if ($wnd.jQuery("#tutorialDialog").hasClass('ui-dialog-content')) {
			$wnd.jQuery("#tutorialDialog").dialog('close');
		}
	}-*/;

	public static native void showTutorialContinueButton() /*-{
		//		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {
		//			'Next >>' : function() {
		//				@explorviz.visualization.experiment.Experiment::incStep()()
		//			}
		//		});
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
		$wnd.jQuery("#tutorialArrowDown").hide();
		var button = $wnd.jQuery("#choose-trace-button0");
		var div = $wnd.jQuery("#tutorialArrowLeft");
		div.style.display = 'block';
		//get position
		div.style.top = (button.position.top + button.height() / 2) + 'px';
		div.style.left = button.position.left + 'px';
		div.show();
	}-*/;

	public static native void showPlayPauseHighlightArrow() /*-{
		$wnd.jQuery("#tutorialArrowDown").hide();
		var button = $wnd.jQuery("#traceReplayStartPause");
		var div = $wnd.jQuery("#tutorialArrowLeft");
		div.style.display = 'block';
		//get position
		div.style.top = (button.position.top + button.height() / 2) + 'px';
		div.style.left = button.position.left + 'px';
		div.show();
	}-*/;

	public static native void showNextHighlightArrow() /*-{
		$wnd.jQuery("#tutorialArrowDown").hide();
		var button = $wnd.jQuery("#traceReplayNext");
		var div = $wnd.jQuery("#tutorialArrowLeft");
		div.style.display = 'block';
		//get position
		div.style.top = (button.position.top + button.height() / 2) + 'px';
		div.style.left = button.position.left + 'px';
		div.show();
	}-*/;

	public static native void hideArrows() /*-{
		$wnd.jQuery("#tutorialArrowLeft").hide();
		$wnd.jQuery("#tutorialArrowDown").hide();
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
						of : $wnd
					}
				});
	}-*/;

	public static native void changeQuestionDialog(String html, String langugage, String caption,
			boolean allowSkip) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.show();
		var timer = $wnd.jQuery("#questiontimer");
		$doc.getElementById("questionDialog").innerHTML = html;
		timer.appendTo("#questionDialog");
		qDialog.dialog('option', 'width', 'auto');
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
													input : "required"
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
		//http://api.jquery.com/jQuery.getScript/
		$wnd.jQuery.getScript(language)
		//alert("path to load localization from: " + language);
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'width', 400);
		qDialog.dialog('option', 'title', "Personal Information");
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd.jQuery("#genderForm").prop("selectedIndex", -1);
		$wnd.jQuery("#degreeForm").prop("selectedIndex", -1);
		$wnd.jQuery("#affForm").prop("selectedIndex", -1);
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
		$wnd.jQuery.getScript(language)
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'width', 400);
		qDialog.dialog('option', 'title', "Personal Information");
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd.jQuery("#exp1Form").prop("selectedIndex", -1);
		$wnd.jQuery("#exp2Form").prop("selectedIndex", -1);
		$wnd.jQuery("#exp3Form").prop("selectedIndex", -1);
		$wnd.jQuery("#exp4Form").prop("selectedIndex", -1);
		$wnd.jQuery(".glyphicon-question-sign").tooltip({
			html : true
		});
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

	public static native void tutorialCommentDialog(String html, String language) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		$doc.getElementById("questionDialog").innerHTML = html;
		qDialog.dialog('option', 'width', 'auto');
		qDialog.dialog('option', 'title', "Debriefing Questionnaire");
		$wnd.jQuery("#tutHelpForm").prop("selectedIndex", -1);
		$wnd.jQuery("#timeForm").prop("selectedIndex", -1);
		$wnd.jQuery("#speedForm").prop("selectedIndex", -1);
		$wnd.jQuery(".glyphicon-question-sign").tooltip({
			html : true
		});
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
		var qDialog = $wnd.jQuery("#questionDialog");
		$doc.getElementById("questionDialog").innerHTML = html;
		qDialog.dialog('option', 'width', 'auto');
		qDialog.dialog('option', 'title', "Debriefing Questionnaire");
		$wnd.jQuery(".glyphicon-question-sign").tooltip({
			html : true
		});
		$wnd.jQuery("#T1Form").prop("selectedIndex", -1);
		$wnd.jQuery("#T2Form").prop("selectedIndex", -1);
		$wnd.jQuery("#T3Form").prop("selectedIndex", -1);
		$wnd.jQuery("#T4Form").prop("selectedIndex", -1);
		$wnd.jQuery("#T5Form").prop("selectedIndex", -1);
		$wnd.jQuery("#T6Form").prop("selectedIndex", -1);
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
							id: "questionSubmit"
						}
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
}
