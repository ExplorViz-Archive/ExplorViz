package explorviz.visualization.experiment;

public class ExperimentToolsJS {
	public static native void init() /*-{
		// Form example with serialization: 
		// src/explorviz/visualization/view/ManageUsersAndRolesPage.java and
		// src/explorviz/visualization/view/ManageUsersAndRolesPageJS.java

		$wnd.jQuery("#startExperimentBtn").on("click touchstart", function() {
			// start ma god damn experiment
		});

		$wnd.jQuery("#newExperimentBtn").on("click touchstart", function() {
			@explorviz.visualization.experiment.ExperimentToolsPage::showNewExpWindow()()
		});

		$wnd
				.jQuery("#questionsSelect")
				.change(function() {
					var id = $wnd.jQuery('#questionsSelect option:selected').attr('id');
					var questionText = @explorviz.visualization.experiment.ExperimentToolsPage::getQuestionText(I)(id)

					$wnd.jQuery("#questionTextLabel").text(questionText)

				});

	}-*/;
}
