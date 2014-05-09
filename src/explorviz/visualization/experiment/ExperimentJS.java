package explorviz.visualization.experiment;

public class ExperimentJS {

	public static native void changeTutorialDialog(String text) /*-{
		$doc.getElementById("tutorialdialog").innerHTML = '<p>' + text + '</p>';
	}-*/;

	public static native void changeQuestionDialog(final String html) /*-{
		$doc.getElementById("questiondialog").innerHTML = html;
	}-*/;

	// Todo: find out ids of dialogs
	public static native void closeTutorialDialog() /*-{
		$doc.getElementByID("").display = 'none';
	}-*/;

	public static native void closeQuestionDialog() /*-{
		$doc.getElementByID("").display = 'none';
	}-*/;
}
