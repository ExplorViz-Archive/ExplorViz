package explorviz.visualization.interaction;

public class TraceHighlighterJS {
	public static native void openDialog(String selectOptions) /*-{
		$wnd.jQuery("#traceHighlighterDialog").show();
		$wnd.jQuery("#traceHighlighterDialog").dialog({
			closeOnEscape : true,
			modal : true,
			title : 'Choose Trace',
			width : '80%',
			height : Math.max($wnd.jQuery("#view").innerHeight() - 250, 400),
			position : {
				my : 'center center',
				at : 'center center',
				of : $wnd.jQuery("#view")
			}
		});

		$wnd.jQuery("#traceHighlighterDialog").dialog('option', 'buttons', {
			'OK' : function() {
				@explorviz.visualization.interaction.TraceHighlighter::choosenOneTrace(Ljava/lang/String;)($wnd.jQuery("#traceChooser").val())
				$wnd.jQuery("#traceHighlighterDialog").dialog('close');
			}
		});

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("traceHighlighterDialog").innerHTML = '<select id="traceChooser" name="traceChooser" size="15" style="width:100%;height:80%">'
				+ selectOptions + '</select>'
	}-*/;
}
