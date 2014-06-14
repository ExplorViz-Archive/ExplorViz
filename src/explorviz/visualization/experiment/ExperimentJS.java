package explorviz.visualization.experiment;

public class ExperimentJS {

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
						my : 'right',
						at : 'right',
						of : $wnd
					}
				});
	}-*/;

	public static native void changeTutorialDialog(String text) /*-{
		$doc.getElementById("tutorialDialog").innerHTML = '<p>' + text + '</p>';
	}-*/;

	public static native void showTutorialContinueButton() /*-{
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {
			'Ok' : function() {
				@explorviz.visualization.experiment.Experiment::incStep()()
			}
		});
	}-*/;

	public static native void removeTutorialContinueButton() /*-{
		$wnd.jQuery("#tutorialDialog").dialog('option', 'buttons', {

		});
	}-*/;

	public static native void showArrow(float x, float y, String path) /*-{
		var div = $doc.getElementById("questionDialog");
		div.style.top = x;
		div.style.left = y;
		div.innerHTML = '<img src=' + path + '>';
	}-*/;

	public static native void hideArrow() /*-{
		$doc.getElementById("questionDialog").style.display = 'none';
	}-*/;

	public static native void changeQuestionDialog(String html) /*-{
		$doc.getElementById("questionDialog").innerHTML = html;
	}-*/;

	/**
	 * Instantiate the dialog before closing it; can't close it if it wasn't
	 * instantiated before hand, which can't be guaranteed.
	 */
	public static native void closeTutorialDialog() /*-{
		if ($wnd.jQuery("#tutorialDialog").hasClass('ui-dialog-content')) {
			$wnd.jQuery("#tutorialDialog").dialog('close');
		}
		//		$wnd.jQuery("#tutorialDialog").show();
		//		$wnd.jQuery("#tutorialDialog").dialog({
		//			title : 'Tutorial',
		//			width : '500px',
		//			height : 'auto',
		//		});
		//		$wnd.jQuery("#tutorialDialog").dialog('close');
	}-*/;

	public static native void closeQuestionDialog() /*-{
		$doc.getElementByID("questionDialog").display = 'none';
	}-*/;
}
