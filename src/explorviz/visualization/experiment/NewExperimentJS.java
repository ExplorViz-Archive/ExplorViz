package explorviz.visualization.experiment;

public class NewExperimentJS {

	public static native void init() /*-{
		var toggle = [ slideOut, slideIn ], c = 0;

		$wnd.jQuery("#expSliderLabel").click(function(e) {
			e.preventDefault();
			toggle[c++ % 2]();
		});

		function slideOut() {
			$wnd.jQuery('#expSlider').animate({
				right : "+=250px"
			})
			$wnd.jQuery('#expSliderForm').animate({
				right : "+=250px"
			})
		}
		;

		function slideIn() {
			$wnd.jQuery('#expSlider').animate({
				right : "-=250px"
			})
			$wnd.jQuery('#expSliderForm').animate({
				right : "-=250px"
			})
		}
		;
	}-*/;
}
