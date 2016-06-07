package explorviz.visualization.experiment;

import com.google.gwt.core.client.JavaScriptObject;

public class NewExperimentJS {

	public static native void init() /*-{
		var toggle = [ slideOut, slideIn ], c = 0;

		var height = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;
		$doc.getElementById("expSliderInnerContainer").style.height = height
				+ 'px';
		$doc.getElementById("expSliderForm").style.maxHeight = (height - 100)
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

		$wnd.jQuery("#expSaveBtn").on("click", function() {
			$wnd.jQuery("#qtType").val(1);
			@explorviz.visualization.experiment.NewExperiment::getNextQuestion()()
		});

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
			$wnd.jQuery("#qtType").val(1);
			@explorviz.visualization.experiment.NewExperiment::getNextQuestion()()
		});

		$wnd.jQuery("#showPrevQuest").on("click", function() {

		});

	}-*/;

	public static native void setupAnswerHandler(int inputID) /*-{

		var inputID = "#correctAnswer" + inputID;

		$wnd
				.jQuery(inputID)
				.on(
						"keyup change",
						function() {
							$wnd.jQuery(inputID).off("keyup change");
							@explorviz.visualization.experiment.NewExperiment::numOfCorrectAnswers += 1;
							var i = @explorviz.visualization.experiment.NewExperiment::numOfCorrectAnswers;
							var input = $doc.createElement("input");
							input.id = "correctAnswer" + i;
							input.name = "correctAnswer" + i;
							$wnd.jQuery("#freeTextAnswers").append("<br>");
							$wnd.jQuery("#freeTextAnswers").append(input);
							@explorviz.visualization.experiment.NewExperimentJS::setupAnswerHandler(I)(i);
						});

	}-*/;

	public static native void setSelectedForSelect(int value) /*-{

		$wnd.jQuery("#qtType").val(value);

	}-*/;

	public static native void saveQuestion() /*-{

		var form = $wnd.jQuery("#expQuestionForm").serializeArray();

		@explorviz.visualization.experiment.NewExperiment::createXML(Lexplorviz/visualization/experiment/NewExperimentJS$MyJsArray;)(form);

	}-*/;

	public static class MyJsArray extends JavaScriptObject {
		protected MyJsArray() {
		}

		public final native int length() /*-{
			return this.length;
		}-*/;

		public final native String getValue(int i) /*-{
			return this[i].value;
		}-*/;
	}
}
