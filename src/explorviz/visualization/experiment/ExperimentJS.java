package explorviz.visualization.experiment;

public class ExperimentJS {

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

	public static native void showTutorialDialog() /*-{
		$wnd.jQuery("#tutorialDialog").show();
		$wnd.jQuery("#tutorialDialog").dialog(
				{
					closeOnEscape : false,
					title : 'Tutorial',
					width : '500px',
					height : 'auto',
					zIndex : 99999999,
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
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {
			'Next >>' : function() {
				@explorviz.visualization.experiment.Experiment::incStep()()
			}
		});
	}-*/;

	public static native void removeTutorialContinueButton() /*-{
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {});
	}-*/;

	public static native void showBackToLandscapeArrow() /*-{
		$doc.getElementById("tutorialArrowLeft").style.display = 'block';
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
					height : 'auto',
					zIndex : 99999999,
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

	public static native void changeQuestionDialog(String html, String error) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.show();
		$doc.getElementById("questionDialog").innerHTML = html;
		qDialog.dialog('option', 'width', 'auto');
		qDialog
				.dialog({
					buttons : [
							{
								text : "Skip",
								click : function() {
									@explorviz.visualization.experiment.Questionnaire::nextQuestion(Ljava/lang/String;)("");
								}
							},
							{
								text : "Ok",
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
													check : "required"
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

	public static native void personalDataDialog(String html) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		qDialog.dialog('option', 'width', 400);
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd.jQuery("#genderForm").prop("selectedIndex", -1);
		$wnd.jQuery("#degreeForm").prop("selectedIndex", -1);
		$wnd.jQuery("#exp1Form").prop("selectedIndex", -1);
		$wnd.jQuery("#exp2Form").prop("selectedIndex", -1);
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

	public static native void commentDialog(String html) /*-{
		var qDialog = $wnd.jQuery("#questionDialog");
		$doc.getElementById("questionDialog").innerHTML = html;
		qDialog.dialog('option', 'width', 'auto');
		$wnd.jQuery("#difficultyForm").prop("selectedIndex", -1);
		$wnd.jQuery("#tutHelpForm").prop("selectedIndex", -1);
		$wnd.jQuery("#questHelpForm").prop("selectedIndex", -1);
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
											@explorviz.visualization.experiment.Questionnaire::saveComments(Ljava/lang/String;)(res);
										},
										errorPlacement : function(error,
												element) {
											var elem = element.parent();
											while (elem.attr('id') != 'form-group') {
												elem = elem.parent();
											}
											error.appendTo(elem);
										}//,
									//messages: {
									//	text: {required: 'Errornachricht'}
									//}
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

}
