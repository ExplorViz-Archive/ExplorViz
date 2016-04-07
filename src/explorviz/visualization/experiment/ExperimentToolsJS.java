package explorviz.visualization.experiment;

public class ExperimentToolsJS {
	public static native void init() /*-{
		// Form example, see: src/explorviz/visualization/view/ManageUsersAndRolesPageJS.java		

		$wnd.jQuery("#startExperimentBtn").on("click touchstart", function() {
			// start ma god damn experiment
		});

		$wnd.jQuery("#newExperimentBtn").on("click touchstart", function() {
			@explorviz.visualization.experiment.ExperimentToolsPage::showNewExpWindow()()
		});

	}-*/;
}
