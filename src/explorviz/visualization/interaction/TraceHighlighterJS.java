package explorviz.visualization.interaction;

public class TraceHighlighterJS {
	public static native void openDialog() /*-{
		$wnd.jQuery("#traceHighlighterDialog").show();
		$wnd.jQuery("#traceHighlighterDialog").dialog({
			closeOnEscape : true,
			modal : true,
			title : 'Choose Trace',
			width : 400,
			height : Math.max($wnd.jQuery("#view").innerHeight() - 250, 400),
			zIndex : 99999999,
			position : {
				my : 'center center',
				at : 'center center',
				of : $wnd.jQuery("#view")
			}
		});

		$wnd.jQuery("#traceHighlighterDialog").dialog('option', 'buttons', {
			'OK' : function() {
				$wnd.jQuery("#traceHighlighterDialog").dialog('close');
				@explorviz.visualization.interaction.TraceHighlighter::choosenOneTrace()()
			}
		});

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("traceHighlighterDialog").innerHTML = '<select name="traceChooser" size="15" style="width:100%;"><option>A - B (Calls: 300, Id: 1002)</option><option>C - B (Calls: 340, Id: 1003)</option></select>'
	}-*/;
}
