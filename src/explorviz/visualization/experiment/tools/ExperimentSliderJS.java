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

		var tour = new $wnd.Tour(
				{
					storage : false,
					backdrop : true,
					onShown : onShownStep,
					onHidden : onHiddenStep,
					steps : [
							{
								element : "body",
								title : "Welcome",
								content : "Welcome to the experiment tools. This tour will guide you through the importand steps for creating a new questionnaire",
								placement : "top",
								orphan : true
							},
							{
								element : "#view",
								title : "Landscape and Application View",
								content : "This is the landscape view. Clicking on an violet object opens the application view.",
								placement : "top",
							},
							{
								element : "#expSliderLabel",
								title : "Title",
								content : "A click on 'Question Interface' opens the question dialog form.",
								placement : "left",
								reflex : true,
								onNext : function(tour) {
									$wnd.jQuery('#expSliderLabel').click();
								}
							},
							{
								element : "#expSliderInnerContainer",
								title : "Title 1",
								content : "This is the question dialog form.",
								placement : "left",
								onPrev : function(tour) {
									$wnd.jQuery('#expSliderLabel').click();
								},
							},
							{
								element : "#exp_slider_question_questiontype_div",
								title : "Type of Question",
								content : "Choose between a free text and a multiple choice question.",
								placement : "left"
							},
							{
								element : "#exp_slider_question_landscape_div",
								title : "Landscape Chooser",
								content : "Choose the landscape for your question. The landscape view on the left shows your pick",
								placement : "left"
							},
							{
								element : "#exp_slider_question_form",
								title : "Question Form",
								content : "Define the question text, the determined working time and the possible or correct answers here. In multiple-choice questions you can also define the right answers. This information is used by the reviser",
								placement : "left"
							},
							{
								element : "#expSliderButton",
								title : "Navigation Buttons",
								content : "Navigate through your questions. The questions are saved on the server everytime you go back or forward.",
								placement : "left"
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
