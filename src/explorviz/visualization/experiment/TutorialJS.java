package explorviz.visualization.experiment;

/**
 * @author Santje Finke
 *
 */
public class TutorialJS {

	/**
	 * Create and position the dialog that will be used to show the texts.
	 */
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

	/**
	 * Changes the displayed text of the tutorial dialog
	 *
	 * @param text
	 *            The text to display
	 * @param title
	 *            The title for the dialog
	 */
	public static native void changeTutorialDialog(String text, String title) /*-{
		var tutorial = $wnd.jQuery("#tutorialDialog");
		tutorial.html("<p>" + text + "</p>");
		tutorial.height(230);
		tutorial.dialog('option', 'title', title);
	}-*/;

	/**
	 * Closes the tutorial dialog. Can safely be called even if it isn't sure if
	 * the dialog exists.
	 */
	public static native void closeTutorialDialog() /*-{
		if ($wnd.jQuery("#tutorialDialog").hasClass('ui-dialog-content')) {
			$wnd.jQuery("#tutorialDialog").dialog('close');
		}
	}-*/;

	/**
	 * Adds a button to the tutorial dialog that activates the next step.
	 */
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

	/**
	 * Removes the continue button from the tutorial dialog.
	 */
	public static native void removeTutorialContinueButton() /*-{
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {});
	}-*/;

	public static native void showBackToLandscapeArrow() /*-{
		var div = $wnd.jQuery("#tutorialArrowLeft");
		div.show();
		//div.style.display = 'block';	creates Error and works without
		//div.style.top = '60px';
		//div.style.left = '125px';
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
		@explorviz.visualization.experiment.TutorialJS::hideArrows()();
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
		@explorviz.visualization.experiment.TutorialJS::hideArrows()();
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
		@explorviz.visualization.experiment.TutorialJS::hideArrows()();
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

	/**
	 * Hides all arrows that belong to the tutorial.
	 */
	public static native void hideArrows() /*-{
		$wnd.jQuery("#tutorialArrowLeft").hide();
		$wnd.jQuery("#tutorialArrowDown").hide();
		$wnd.jQuery(".tutorialPauseArrow").hide();
		$wnd.jQuery(".tutorialNextArrow").hide();
	}-*/;

	/**
	 * Changes the location of the tutorial dialog into the upper left corner
	 */
	public static native void changeDialogLocationToUpperLeftCorner() /*-{
		var tutorial = $wnd.jQuery('div[aria-describedby*="tutorialDialog"]');
		tutorial.position({
			my : "left top",
			at : "left top",
			of : $wnd.jQuery("#view")
		});
	}-*/;
}
