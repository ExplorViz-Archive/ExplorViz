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
		//Pressing enter doesn't do anything -> okay
	}-*/;

	public static native void removeTutorialContinueButton() /*-{
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {});
	}-*/;

	public static native void showBackToLandscapeArrow() /*-{
		$doc.getElementById("tutorialArrowLeft").style.display = 'block';
		//$doc.getElementById("tutorialArrowDown").style.display = 'none';
		$wnd.jQuery("#tutorialArrowDown").hide();
	}-*/;

	public static native void showTimshiftArrow() /*-{
		var top = $doc.getElementById("timeshiftChartDiv").style.top;
		var left = $wnd.jQuery("#timeshiftChartDiv").width() / 3;
		var div = $doc.getElementById("tutorialArrowDown");
		$wnd.jQuery("#tutorialArrowDown").show();
		div.style.display = 'block';
		div.style.top = top;
		div.style.left = left + 'px';
		//$doc.getElementById("tutorialArrowLeft").style.display = 'none';
		$wnd.jQuery("#tutorialArrowLeft").hide();
	}-*/;

	public static native void hideArrows() /*-{
		//$doc.getElementById("tutorialArrowLeft").style.display = 'none';
		//$doc.getElementById("tutorialArrowDown").style.display = 'none';
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

	public static native void changeQuestionDialog(String html) /*-{
		$wnd.jQuery("#questionDialog").show();
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd.jQuery("input,select").keypress(function(event) {
			return event.keyCode != 13;
		});
		$wnd
				.jQuery("#questionDialog")
				.dialog(
						'option',
						'buttons',
						{
							'Skip' : function() {
								@explorviz.visualization.experiment.Questionnaire::nextQuestion(Ljava/lang/String;)("");
							},
							'Ok' : function() {
								var res = $wnd.jQuery("#questionForm")
										.serialize();
								@explorviz.visualization.experiment.Questionnaire::nextQuestion(Ljava/lang/String;)(res);
							}
						});
	}-*/;

	public static native void closeQuestionDialog() /*-{
		if ($wnd.jQuery("#questionDialog").hasClass('ui-dialog-content')) {
			$wnd.jQuery("#questionDialog").dialog('close');
		}
	}-*/;

	public static native void personalDataDialog(String html) /*-{
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd.jQuery("input,select").keypress(function(event) {
			return event.keyCode != 13;
		});
		$wnd.jQuery("#genderForm").prop("selectedIndex", -1);
		$wnd.jQuery("#degreeForm").prop("selectedIndex", -1);
		$wnd.jQuery("#exp1Form").prop("selectedIndex", -1);
		$wnd.jQuery("#exp2Form").prop("selectedIndex", -1);
		$wnd
				.jQuery("#questionDialog")
				.dialog(
						'option',
						'buttons',
						{
							'Ok' : function() {
								var res = $wnd.jQuery("#questionForm")
										.serialize();
								@explorviz.visualization.experiment.Questionnaire::savePersonalInformation(Ljava/lang/String;)(res);
							}
						});
	}-*/;

	public static native void commentDialog(String html) /*-{
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd.jQuery("input,select").keypress(function(event) {
			return event.keyCode != 13;
		});
		$wnd.jQuery("#difficultyForm").prop("selectedIndex", -1);
		$wnd.jQuery("#tutHelpForm").prop("selectedIndex", -1);
		$wnd.jQuery("#questHelpForm").prop("selectedIndex", -1);
		$wnd
				.jQuery("#questionDialog")
				.dialog(
						'option',
						'buttons',
						{
							'Ok' : function() {
								var res = $wnd.jQuery("#questionForm")
										.serialize();
								@explorviz.visualization.experiment.Questionnaire::saveComments(Ljava/lang/String;)(res);
							}
						});
	}-*/;

	public static native void clickExplorVizRibbon() /*-{
		alert("called clickExplorVizRibbon");
		//$wnd.jQuery("#explorviz_ribbon").click();
		//$wnd.jQuery("#explorviz_ribbon").trigger('click');

		//		var elem = $wnd.jQuery("#explorviz_ribbon");
		//		alert("found " + elem);
		//		if (typeof elem.onclick == "function") {
		//			elem.onclick.apply(elem);
		//		}
	}-*/;

}
