package explorviz.visualization.experiment.tools;

public class ExperimentToolsPageJS {

	public static native void prepareModal() /*-{

		var modal = "<div class='modal fade' id='myModal' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>"
				+ "<div class='modal-dialog modal-dialog-center' role='document'> <div class='modal-content'>"
				+ "<div class='modal-header'>"
				+ "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>"
				+ "<span aria-hidden='true'>&times;</span>"
				+ "</button>"
				+ "<h4 class='modal-title' id='myModalLabel'>Modal title</h4>"
				+ "</div>"
				+ "<div class='modal-body'>"
				+ "...</div>"
				+ "<div class='modal-footer'>"
				+ "<button type='button' class='btn btn-secondary' data-dismiss='modal'>Close</button>"
				+ "</div></div></div></div>"

		$doc.body.innerText.concat(modal);

	}-*/;

}
