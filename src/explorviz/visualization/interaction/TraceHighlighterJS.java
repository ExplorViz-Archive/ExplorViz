package explorviz.visualization.interaction;

public class TraceHighlighterJS {
	public static native void openDialog(String application) /*-{
		$wnd.jQuery("#traceHighlighterDialog").show();
		$wnd.jQuery("#traceHighlighterDialog").dialog({
			closeOnEscape : true,
			modal : true,
			title : 'Choose Trace',
			width : '40%',
			height : $wnd.jQuery("#view").innerHeight() - 100,
			zIndex : 99999999,
			position : {
				my : 'center top',
				at : 'center top',
				of : $wnd.jQuery("#view")
			}
		});

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("traceHighlighterDialog").innerHTML = 'x'
	}-*/;
}
