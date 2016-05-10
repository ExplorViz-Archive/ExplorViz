package explorviz.visualization.experiment;

public class NewExperimentJS {

	public static native void init() /*-{
		var toggle = [ slideOut, slideIn ], c = 0;

		$wnd.jQuery("#expSliderLabel").click(function(e) {

			console.log("hallo");

			e.preventDefault();
			toggle[c++ % 2]();
		});

		function slideOut() {
			$wnd.jQuery('#expSlider').animate({
				right : "+=250px"
			});
			$wnd.jQuery('#expSliderForm').animate({
				right : "+=250px"
			});

			var vWidth = @explorviz.visualization.engine.main.WebGLStart::viewportWidth;
			var vHeight = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;
			$wnd.jQuery('#webglcanvas').prop('width', vWidth - 300);
		}
		;

		function slideIn() {
			$wnd.jQuery('#expSlider').animate({
				right : "-=250px"
			})
			$wnd.jQuery('#expSliderForm').animate({
				right : "-=250px"
			})

			var vWidth = @explorviz.visualization.engine.main.WebGLStart::viewportWidth;
			var vHeight = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;
			$wnd.jQuery('#webglcanvas').prop('width', vWidth + 300);
		}
		;

		$wnd.jQuery("#expSaveBtn").click(

		function() {
			console.log("hallo");
		});

	}-*/;
}
