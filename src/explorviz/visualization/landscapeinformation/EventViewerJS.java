package explorviz.visualization.landscapeinformation;

public class EventViewerJS {
	public static native void openDialog() /*-{
		$wnd.jQuery("#eventViewerDialog").show();
		$wnd.jQuery("#eventViewerDialog").dialog({
			closeOnEscape : true,
			modal : false,
			resizable : true,
			title : 'Event Viewer',
			width : 700,
			height : 300,
			position : {
				my : 'right center',
				at : 'right center',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("eventViewerDialog").innerHTML = '<div id="eventViewerText" readonly style="width: 100%; height: 98%;"></div>'
	}-*/;

	public static native void setEventText(String text) /*-{
		if ($doc.getElementById("eventViewerText"))
			$doc.getElementById("eventViewerText").innerHTML = text
	}-*/;
}
