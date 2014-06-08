package explorviz.visualization.experiment;

public class ExperimentJS {

	public static native void showTutorialDialog() /*-{
		$wnd.jQuery("#tutorialDialog").show();
		$wnd.jQuery("#tutorialDialog").dialog(
				{
					closeOnEscape : false,
					title : 'Tutorial',
					width : 'auto',
					zIndex : 10000,
					open : function(event, ui) {
						$wnd.jQuery(this).closest('.ui-dialog').find(
								'.ui-dialog-titlebar-close').hide();
					}
				});
	}-*/;

	public static native void changeTutorialDialog(String text) /*-{
		$doc.getElementById("tutorialDialog").innerHTML = '<p>' + text + '</p>';
	}-*/;

	public static native void changeQuestionDialog(String html) /*-{
		$doc.getElementById("questionDialog").innerHTML = html;
	}-*/;

	/**
	 * Instantiate the dialog before closing it; can't close it if it wasn't
	 * instantiated before hand, which can't be guaranteed.
	 */
	public static native void closeTutorialDialog() /*-{
		$wnd.jQuery("#tutorialDialog").show();
		$wnd.jQuery("#tutorialDialog").dialog(
				{
					closeOnEscape : false,
					title : 'Tutorial',
					width : 'auto',
					zIndex : 10000,
					open : function(event, ui) {
						$wnd.jQuery(this).closest('.ui-dialog').find(
								'.ui-dialog-titlebar-close').hide();
					}
				});
		$wnd.jQuery("#tutorialDialog").dialog('close');
	}-*/;

	public static native void closeQuestionDialog() /*-{
		$doc.getElementByID("questionDialog").display = 'none';
	}-*/;
}
