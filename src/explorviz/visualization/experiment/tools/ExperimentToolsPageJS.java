package explorviz.visualization.experiment.tools;

public class ExperimentToolsPageJS {

	public static native void prepareModal(String modalExpDetails,
			String modalExpUserManagement) /*-{

		if ($wnd.jQuery("#modalExpDetails").length == 0) {
			$wnd.jQuery("body").prepend(modalExpDetails);
		}

		if ($wnd.jQuery("#modalExpUserManagement").length == 0) {
			$wnd.jQuery("body").prepend(modalExpUserManagement);
		}

	}-*/;

	public static native void showDetailModal(String detailsTable)/*-{

		$wnd.jQuery("#exp-modal-details-body").html(detailsTable);
		$wnd.jQuery("#modalExpDetails").modal("show");

	}-*/;

	public static native void showUserModal(String details)/*-{

		$wnd.jQuery("#exp-modal-user-body").html(details);
		$wnd.jQuery("#modalExpUserManagement").modal("show");

	}-*/;

}
