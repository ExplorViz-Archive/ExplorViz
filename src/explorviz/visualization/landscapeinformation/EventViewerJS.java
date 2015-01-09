package explorviz.visualization.landscapeinformation;

public class EventViewerJS {
	public static native void openDialog(String application) /*-{
		$wnd.jQuery("#codeViewerDialog").show();
		$wnd.jQuery("#codeViewerDialog").dialog({
			closeOnEscape : true,
			modal : true,
			resizable : false,
			title : 'Code Viewer for ' + application,
			width : '70%',
			height : Math.max($wnd.jQuery("#view").innerHeight() - 100, 300),
			position : {
				my : 'center top',
				at : 'center top',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("codeViewerDialog").innerHTML = '<div id="codetreeview-wrapper"><div id="codetreeview"></div></div><div id="codeview-wrapper"><h1 id="codeview-filename"></h1><div id="codeview" style="height:100%"></div></div>'
	}-*/;
}
