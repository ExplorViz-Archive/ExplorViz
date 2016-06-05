package explorviz.visualization.experiment;

public class NewExperimentJS {

	public static native void init() /*-{
		var toggle = [ slideOut, slideIn ], c = 0;

		var height = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;
		$doc.getElementById("expSliderInnerContainer").style.height = height
				+ 'px';

		$wnd.jQuery("#expSliderLabel").click(function(e) {
			e.preventDefault();
			toggle[c++ % 2]();
		});

		function slideOut() {
			$wnd.jQuery('#expSlider').animate({
				right : "+=350px"
			});
			$wnd.jQuery('#expSliderInnerContainer').animate({
				right : "+=350px"
			});
		}
		;

		function slideIn() {
			$wnd.jQuery('#expSlider').animate({
				right : "-=350px"
			})
			$wnd.jQuery('#expSliderInnerContainer').animate({
				right : "-=350px"
			})
		}
		;

		$wnd
				.jQuery("#expSaveBtn")
				.bind(
						'click',
						@explorviz.visualization.experiment.NewExperiment::getNextQuestion());

		$wnd.jQuery("#qtType").on("change", function() {
			var value = $wnd.jQuery(this).val();
			@explorviz.visualization.experiment.NewExperiment::createtQuestForm(I)(value)
		});

	}-*/;
}
