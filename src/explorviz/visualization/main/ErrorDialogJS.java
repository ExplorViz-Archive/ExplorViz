package explorviz.visualization.main;

public class ErrorDialogJS {
	public static native void showErrorDialog(final String explanation, final String detailedMessage) /*-{
		$wnd.jQuery("#errorDialog").show();
		$wnd.jQuery("#errorDialog").dialog({
			closeOnEscape : true,
			modal : true,
			title : 'Sorry, there was an error',
			width : 400,
			height : 250,
			position : {
				my : 'center center',
				at : 'center center',
				of : $wnd.jQuery("#view")
			}
		});

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("errorDialog").innerHTML = explanation
				+ "<br><br>Detailed message is:<br>" + detailedMessage
	}-*/;
}
