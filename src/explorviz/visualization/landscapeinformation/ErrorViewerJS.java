package explorviz.visualization.landscapeinformation;

public class ErrorViewerJS {
	public static native void openDialog() /*-{
		$wnd.jQuery("#exceptionViewerDialog").show();
		$wnd.jQuery("#exceptionViewerDialog").dialog({
			closeOnEscape : true,
			modal : false,
			resizable : true,
			title : 'Exception Viewer',
			width : 1280,
			height : 400,
			position : {
				my : 'right center',
				at : 'right center',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("exceptionViewerDialog").innerHTML = '<div id="errorViewerText" readonly style="width: 100%; height: 98%;"></div>'
	}-*/;

	public static native void setErrorText(String text) /*-{
		if ($doc.getElementById("errorViewerText"))
			$doc.getElementById("errorViewerText").innerHTML = text
	}-*/;
}
