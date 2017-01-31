package explorviz.visualization.experiment.tools;

import com.google.gwt.core.client.JsArrayString;

public class ExperimentSliderJS {

	public static native void showSliderForExp(JsArrayString landscapeNames,
			String jsonQuestionnaire, boolean isWelcome, boolean preAndPostQuestions) /*-{
		var height = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

		//slider.js in war/js/
		$wnd.Slider(height, saveQuestion, landscapeNames, loadlandscape,
				jsonQuestionnaire, preAndPostQuestions,
				loadExperimentToolsPage, isWelcome, getMaybeApplication);

		function loadlandscape(timestamp, maybeApplication) {
			@explorviz.visualization.experiment.tools.ExperimentSlider::loadLandscape(Ljava/lang/String;Ljava/lang/String;)(timestamp, maybeApplication)
		}

		function saveQuestion(questionForm) {
			@explorviz.visualization.experiment.tools.ExperimentSlider::saveToServer(Ljava/lang/String;)(questionForm)
		}

		function loadExperimentToolsPage() {
			$wnd.jQuery("#experimentStartTour").hide();
			@explorviz.visualization.experiment.tools.ExperimentToolsPage::loadExpToolsPage()()
		}

		function getMaybeApplication() {
			return @explorviz.visualization.experiment.tools.ExperimentSlider::getMaybeApplication()()
		}
	}-*/;

	public static native void startTour() /*-{
		//not really understandable; when tourStarted exists, set it to false und reload and when it is on false, set it to true (constant toggling)

		// show tour only once for this compilation (and user)
		//
		// HINT: you can set storage : true in 
		// the tour constructor for browser wide 
		// storage (remove those if-statements below) 
		// => tutorial shows only once regardless
		// of the user

		if ($wnd.tourStarted && $wnd.tourStarted == true) {
			showTourButton();
			return;
		}

		if (!$wnd.tourStarted) {
			$wnd.tourStarted = true;
		}

		function showTourButton() {
			var $button = $wnd.jQuery("#experimentStartTour");

			$button.unbind('click');

			$button.show();

			$button.on("click", function(e) {
				$wnd.tourStarted = false;
				@explorviz.visualization.experiment.tools.ExperimentSlider::reloadPage()()
			});
		}

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
					onEnd : function(tour) {
						showTourButton();
					},
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
								content : "This is the landscape view. Clicking on an violet object opens the application view. If there is none, it will come up later.",
								placement : "top"
							},
							{
								element : "#expSliderLabel",
								title : "Question Interface",
								content : "A click on 'Question Interface' opens the question dialog form. Click it to continue.",
								placement : "left",
								reflex : true,
								backdropContainer : "#expSlider",
								onShown : function(tour) {
									disableNextButton();
									// temporally disable clicks on navbar
									$wnd
											.jQuery(
													'#bs-example-navbar-collapse-1')
											.css("pointer-events", "none");
								}
							},
							{
								element : "#expSliderLabel",
								backdropContainer : "#expSlider",
								onShown : function(tour) {
									$wnd.jQuery('#step-3').hide();
									setTimeout(
											function() {
												tour.next();
												$wnd
														.jQuery(
																'#bs-example-navbar-collapse-1')
														.css("pointer-events",
																"visible");
											}, 400);
								}
							},
							{
								element : "#exp_slider_question_questiontype_div",
								title : "Type of Question",
								content : "Choose between a free text and a multiple-choice question. In addition, there is a range of numbers question possible for pre- and postquestions",
								placement : "left"
							},
							{
								element : "#exp_slider_question_landscape_div",
								title : "Landscape Chooser",
								content : "Choose the landscape for your question. The landscape view on the left shows your current pick. This is only for normal questions possible.",
								placement : "left"
							},
							{
								element : "#exp_slider_question_form",
								title : "Question Form",
								content : "Define the question text, the determined working time and the possible or correct answers here. There is no working time in pre- or postquestions.",
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
								content : "In multiple-choice questions, the number of possible answers results in selectable options for probands. One correct answer (marked with a checkbox) results in radio buttons for probands, multiple correct answers in checkboxes. In case of pre- or postquestions are all answers considered correct.",
								placement : "left"
							},
							{
								element : "#expSliderButton",
								title : "Navigation Buttons",
								content : "Navigate through your questions. The questions are saved on the server everytime you switch between question-categories, go back, forward or exit.",
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

}
