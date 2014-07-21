package explorviz.visualization.main;

public class AlertDialogJS {
	public static native void showAlertDialog(final String title, final String message) /*-{
		$wnd.jQuery("#errorDialog").show();
		$wnd.jQuery("#errorDialog").dialog({
			closeOnEscape : true,
			modal : true,
			title : title,
			resizable : false,
			width : 400,
			height : 150,
			position : {
				my : 'center center',
				at : 'center center',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("errorDialog").innerHTML = message
	}-*/;
}
