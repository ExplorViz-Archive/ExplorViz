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

		function disableNextButton() {
			$wnd
					.jQuery(
							".popover.tour-tour .popover-navigation .btn-group .btn[data-role=next]")
					.prop("disabled", true);
		}

		function showNextButton() {
			$wnd
					.jQuery(
							".popover.tour-tour .popover-navigation .btn-group .btn[data-role=next]")
					.prop("disabled", false);
		}

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
								content : "Welcome to the experiment tools. This tour will guide you through the important steps for creating a new questionnaire.",
								placement : "top"
							},
							{
								element : "#view",
								title : "Landscape and Application View",
								content : "This is the landscape view. Clicking on an violet object opens the application view.",
								placement : "top"
							},
							{
								element : "#expSliderLabel",
								title : "Title",
								content : "A click on 'Question Interface' opens the question dialog form. Click it to continue.",
								placement : "left",
								reflex : true,
								backdropContainer : "#expSlider",
								onShown : function(tour) {
									disableNextButton();
								}
							},
							{
								element : "#expSliderLabel",
								backdropContainer : "#expSlider",
								onShown : function(tour) {
									$wnd.jQuery('#step-3').hide();
									setTimeout(function() {
										tour.next();
									}, 400);
								}
							},
							{
								element : "#exp_slider_question_questiontype_div",
								title : "Type of Question",
								content : "Choose between a free text and a multiple-choice question.",
								placement : "left"
							},
							{
								element : "#exp_slider_question_landscape_div",
								title : "Landscape Chooser",
								content : "Choose the landscape for your question. The landscape view on the left shows your current pick.",
								placement : "left"
							},
							{
								element : "#exp_slider_question_form",
								title : "Question Form",
								content : "Define the question text, the determined working time and the possible or correct answers here. ",
								placement : "left"
							},
							{
								element : "#exp_slider_question_form",
								title : "Free text questions",
								content : "The number of correct answers in free text questions results in the number of empty input boxes for probands.",
								placement : "left"
							},
							{
								element : "#exp_slider_question_form",
								title : "Multiple-choice questions",
								content : "In multiple-choice questions, the number of possible answers results in selectable options for probands. One correct answer (marked with a checkbox) results in radio buttons for probands, multiple correct answers in checkboxes.",
								placement : "left"
							},
							{
								element : "#expSliderButton",
								title : "Navigation Buttons",
								content : "Navigate through your questions. The questions are saved on the server everytime you go back, forward or exit.",
								placement : "left"
							} ]
				});

		$wnd.jQuery('#expSliderLabel').on("click", function(e) {
			var step = parseInt($wnd.jQuery(this).data("go-to-step"));
			tour.goTo(step);
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
