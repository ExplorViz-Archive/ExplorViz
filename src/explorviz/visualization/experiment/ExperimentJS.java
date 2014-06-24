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
						of : $wnd
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
			'Ok' : function() {
				@explorviz.visualization.experiment.Experiment::incStep()()
			}
		});
	}-*/;

	public static native void removeTutorialContinueButton() /*-{
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {});
	}-*/;

	public static native void showBackToLandscapeArrow() /*-{
		$doc.getElementById("tutorialArrowLeft").style.display = 'block';
		$doc.getElementById("tutorialArrowDown").style.display = 'none';
	}-*/;

	public static native void showTimshiftArrow() /*-{
		var top = $doc.getElementById("timeshiftChartDiv").style.top;
		var left = $wnd.jQuery("#timeshiftChartDiv").width() / 3;
		var div = $doc.getElementById("tutorialArrowDown");
		div.style.display = 'block';
		div.style.top = top;
		div.style.left = left + 'px';
		$doc.getElementById("tutorialArrowLeft").style.display = 'none';
	}-*/;

	public static native void hideArrows() /*-{
		$doc.getElementById("tutorialArrowLeft").style.display = 'none';
		$doc.getElementById("tutorialArrowDown").style.display = 'none';
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
		$doc.getElementById("questionDialog").innerHTML = html;
		$wnd
				.jQuery("#questionDialog")
				.dialog(
						'option',
						'buttons',
						{
							'Ok' : function() {
								var res = $wnd.jQuery("#questionForm")
										.serialize();
								//alert(res);
								@explorviz.visualization.experiment.Questionnaire::nextQuestion(Ljava/lang/String;)(res);
							},
							'Skip' : function() {
								@explorviz.visualization.experiment.Questionnaire::nextQuestion(Ljava/lang/String;)("");
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
							},
						});
	}-*/;

	public static native void commentDialog(String html) /*-{
		$doc.getElementById("questionDialog").innerHTML = html;
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
							},
						});
	}-*/;

	public static native void clickExplorVizRibbon() /*-{
		$wnd.jQuery("#explorviz_ribbon").click();
	}-*/;

}
