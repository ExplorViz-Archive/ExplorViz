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

		$wnd
				.jQuery("#expSaveBtn")
				.on(
						"click",
						function() {
							@explorviz.visualization.experiment.NewExperimentJS::saveQuestion()();
						});

		$wnd
				.jQuery("#expBackBtn")
				.on(
						"click",
						function() {
							var tempForm = $wnd.jQuery("#expQuestionForm")
									.serializeArray();
							@explorviz.visualization.experiment.NewExperiment::updateOrSaveBuffer(Lexplorviz/visualization/experiment/NewExperimentJS$ExplorVizJSArray;)(tempForm);
							@explorviz.visualization.experiment.NewExperiment::questionPointer -= 1
							var previousForm = @explorviz.visualization.experiment.NewExperiment::questionBuffer[@explorviz.visualization.experiment.NewExperiment::questionPointer];
							var numberOfAnswers = previousForm.length - 4;
							@explorviz.visualization.experiment.NewExperiment::createQuestForm(II)(1,numberOfAnswers);
							setFormData(previousForm);
						});

		$wnd.jQuery("#qtType").on("change", function() {
			var value = $wnd.jQuery(this).val();
			@explorviz.visualization.experiment.NewExperiment::createQuestForm(II)(value,1)
		});

		function setFormData(tempForm) {
			var length = tempForm.length;

			for (i = 0; i < length; i++) {
				$wnd.jQuery("#" + tempForm[i].name).val(tempForm[i].value);
			}
		}

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

							var inputDiv = $doc.createElement("div");
							inputDiv.id = "answer" + i;
							inputDiv.name = "answer" + i;

							var inputText = $doc.createElement("input");
							inputText.id = "correctAnswer" + i;
							inputText.name = "correctAnswer" + i;

							$wnd.jQuery("#answers").append("<br>");
							inputDiv.appendChild(inputText);

							if ($wnd.jQuery("#qtType").val() == "2") {
								var inputBox = $doc.createElement("input");
								inputBox.type = "checkbox";
								inputBox.id = "correctAnswerCheckbox" + i;
								inputBox.name = "correctAnswerCheckbox" + i;
								inputBox.style.marginLeft = "4px";
								inputDiv.appendChild(inputBox);
							}

							$wnd.jQuery("#answers").append(inputDiv);

							@explorviz.visualization.experiment.NewExperimentJS::setupAnswerHandler(I)(i);
						});

	}-*/;

	public static native void setSelectedForSelect(int value) /*-{

		$wnd.jQuery("#qtType").val(value);

	}-*/;

	public static native void saveQuestion() /*-{

		var formCompleted = true;
		var questionPointer = @explorviz.visualization.experiment.NewExperiment::questionPointer;

		if (questionPointer >= 0) {
			var form = $wnd.jQuery("#expQuestionForm").serializeArray();
			formCompleted = @explorviz.visualization.experiment.NewExperiment::saveToServer(Lexplorviz/visualization/experiment/NewExperimentJS$ExplorVizJSArray;)(form);
		}

		if (formCompleted) {
			$wnd.jQuery("#qtType").val(1);
			@explorviz.visualization.experiment.NewExperiment::getNextQuestion()();
		} else {
			alert("Please fill out all values. You need at least one answer.");
		}
	}-*/;

	public static class ExplorVizJSArray extends JavaScriptObject {
		protected ExplorVizJSArray() {
		}

		public final native int length() /*-{
			return this.length;
		}-*/;

		public final native String getValue(int i) /*-{
			return this[i].value;
		}-*/;
	}

	// not used atm
	// public static native void setupOptButtonHandlers() /*-{
	//
	// $wnd.jQuery("#nextQuestion").on("click", function() {
	// $wnd.jQuery("#qtType").val(1);
	// @explorviz.visualization.experiment.NewExperiment::getNextQuestion()()
	// });
	//
	// }-*/;
}
