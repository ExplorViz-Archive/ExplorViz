package explorviz.visualization.experiment.tools;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ExperimentSliderJS {

	public static native void showSliderForExp(JsArrayString landscapeNames,
			String jsonQuestionnaire, boolean isWelcome) /*-{
		var height = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

		//slider.js in war/js/
		$wnd.Slider(height, saveQuestion, landscapeNames, loadlandscape,
				jsonQuestionnaire, loadExperimentToolsPage, isWelcome,
				getMaybeApplication);

		function loadlandscape(timestamp, maybeApplication) {
			@explorviz.visualization.experiment.tools.ExperimentSlider::loadLandscape(Ljava/lang/String;Ljava/lang/String;)(timestamp, maybeApplication)
		}

		function saveQuestion(questionForm) {
			@explorviz.visualization.experiment.tools.ExperimentSlider::saveToServer(Ljava/lang/String;)(questionForm)
		}

		function loadExperimentToolsPage() {
			@explorviz.visualization.experiment.tools.ExperimentToolsPage::loadExpToolsPage()()
		}

		function getMaybeApplication() {
			return @explorviz.visualization.experiment.tools.ExperimentSlider::getMaybeApplication()()
		}

	}-*/;

	public static native void startTour() /*-{

		function onShownStep(tour) {

			$wnd.jQuery('.tour-backdrop').css("opacity", "0");
			$wnd.jQuery('.tour-step-background').css("box-shadow",
					"0px 0px 0px 4000px rgba(0, 0, 0, 0.5)");
			$wnd.jQuery('.tour-step-background').css("-moz-box-shadow",
					"0px 0px 0px 4000px rgba(0, 0, 0, 0.5)");
			$wnd.jQuery('.tour-step-background').css("-webkit-box-shadow",
					"0px 0px 0px 4000px rgba(0, 0, 0, 0.5)");
			$wnd.jQuery('.tour-step-background').css("background",
					"transparent");

		}

		function onHiddenStep(tour) {

			$wnd.jQuery('.tour-backdrop').css("opacity", "0.5");
			$wnd.jQuery('.tour-step-background').css("box-shadow",
					"0px 0px 0px 4000px rgba(0, 0, 0, 0)");
			$wnd.jQuery('.tour-step-background').css("-moz-box-shadow",
					"0px 0px 0px 4000px rgba(0, 0, 0, 0)");
			$wnd.jQuery('.tour-step-background').css("-webkit-box-shadow",
					"0px 0px 0px 4000px rgba(0, 0, 0, 0)");

		}

		var tour = new $wnd.Tour({
			storage : false,
			backdrop : true,
			steps : [ {
				element : "#expSliderInnerContainer",
				title : "Title 1",
				content : "Content 1",
				onShown : onShownStep,
				onHidden : onHiddenStep
			}, {
				element : "#exp_slider_question_questiontype_div",
				title : "Title 2",
				content : "Content 2",
				onShown : onShownStep,
				onHidden : onHiddenStep
			}, {
				element : "#exp_slider_question_landscape_div",
				title : "Title 3",
				content : "Content 3",
				onShown : onShownStep,
				onHidden : onHiddenStep
			} ]
		});

		tour.init();

		tour.start();

	}-*/;

	public static class OverlayJSObj extends JavaScriptObject {
		protected OverlayJSObj() {
		}

		public final native JavaScriptObject test() /*-{
			var obj = {};

			Object.keys(this).forEach(function(key) {
				obj[key] = this[key];
			});

			return obj;
		}-*/;

		public final native String[] getKeys() /*-{
			return Object.keys(this);
		}-*/;

		public final native String[] getValues() /*-{
			var arr = [];

			var length = Object.keys(this).length;

			for ( var key in this) {
				arr.push(this[key]);
			}

			return arr;
		}-*/;

	}

}
