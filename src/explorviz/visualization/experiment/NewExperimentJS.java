package explorviz.visualization.experiment;

public class NewExperimentJS {

	public static native void init() /*-{

		$wnd.jQuery("#expSlider").on("click touchstart", function() {
			$wnd.jQuery("#expQuestionPanel").slideToggle("slow");
		});
	}-*/;
}
