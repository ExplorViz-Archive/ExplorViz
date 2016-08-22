package explorviz.visualization.experiment.tools;

public class ExperimentToolsPageJS {

	public static native void prepareModal() /*-{

		var modalExpDetails = "<div class='modal fade' id='modalExpDetails' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>"
				+ "<div class='modal-dialog modal-dialog-center' role='document'> <div class='modal-content'>"
				+ "<div class='modal-header'>"
				+ "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>"
				+ "<span aria-hidden='true'>&times;</span>"
				+ "</button>"
				+ "<h4 class='modal-title' id='myModalLabel'>Experiment details</h4>"
				+ "</div>"
				+ "<div id='exp-modal-details-body' class='modal-body'>"
				+ "CONTENT HERE"
				+ "</div>"
				+ "<div class='modal-footer'>"
				+ "<button type='button' class='btn btn-secondary' data-dismiss='modal'>Close</button>"
				+ "</div></div></div></div>";

		if ($wnd.jQuery("#modalExpDetails").length == 0) {
			$wnd.jQuery("body").prepend(modalExpDetails);
		}

		var modalExpUserManagement = "<div class='modal fade' id='modalExpUserManagement' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>"
				+ "<div class='modal-dialog modal-dialog-center' role='document'> <div class='modal-content'>"
				+ "<div class='modal-header'>"
				+ "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>"
				+ "<span aria-hidden='true'>&times;</span>"
				+ "</button>"
				+ "<h4 class='modal-title' id='myModalLabel'>User management</h4>"
				+ "</div>"
				+ "<div id='exp-modal-user-body' class='modal-body'>"
				+ "CONTENT HERE"
				+ "</div>"
				+ "<div class='modal-footer'>"
				+ "<button type='button' class='btn btn-secondary' data-dismiss='modal'>Close</button>"
				+ "</div></div></div></div>";

		if ($wnd.jQuery("#modalExpUserManagement").length == 0) {
			$wnd.jQuery("body").prepend(modalExpUserManagement);
		}

	}-*/;

	public static native void showDetailModal(String details)/*-{

		$wnd.jQuery("#exp-modal-details-body").html(details);
		$wnd.jQuery("#modalExpDetails").modal("show");

	}-*/;

	public static native void showUserModal(String details)/*-{

		$wnd.jQuery("#exp-modal-user-body").html(details);
		$wnd.jQuery("#modalExpUserManagement").modal("show");

	}-*/;

}
