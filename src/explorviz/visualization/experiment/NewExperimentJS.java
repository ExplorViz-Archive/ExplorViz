package explorviz.visualization.experiment;

public class NewExperimentJS {

	public static native void init() /*-{
		var toggle = [ slideOut, slideIn ], c = 0;

		var height = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;
		$doc.getElementById("expSliderInnerContainer").style.height = height + 'px';
		$doc.getElementById("expSliderForm").style.maxHeight = (height - 100) + 'px';

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
				.bind('click', @explorviz.visualization.experiment.NewExperiment::getNextQuestion());

		$wnd.jQuery("#qtType").on("change", function() {
			var value = $wnd.jQuery(this).val();
			@explorviz.visualization.experiment.NewExperiment::createQuestForm(I)(value)
		});

	}-*/;

	public static native void setupOptButtonHandlers() /*-{

		$wnd.jQuery("#closeExp").on("click", function() {
			alert("Now shutdown your computer!")
		});

		$wnd.jQuery("#nextQuestion").on("click", function() {
			$wnd.jQuery("#qtType").val("-1");
			@explorviz.visualization.experiment.NewExperiment::getNextQuestion()()
		});

		$wnd.jQuery("#showPrevQuest").on("click", function() {

		});

	}-*/;

	public static native void setupAnswerHandler(int inputID) /*-{

		var inputID = "#correctAnswer" + inputID;

		$wnd.jQuery(inputID).on("keyup change", function() {
			$wnd.jQuery(inputID).off("keyup change");
			@explorviz.visualization.experiment.NewExperiment::numOfCorrectAnswers += 1;
			var i = @explorviz.visualization.experiment.NewExperiment::numOfCorrectAnswers;
			var input = $doc.createElement("input");
			input.id = "correctAnswer" + i;
			$wnd.jQuery("#freeTextAnswers").append("<br>");
			$wnd.jQuery("#freeTextAnswers").append(input);
			@explorviz.visualization.experiment.NewExperimentJS::setupAnswerHandler(I)(i);
		});

	}-*/;
}
