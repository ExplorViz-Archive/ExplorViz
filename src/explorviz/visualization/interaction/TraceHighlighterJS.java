package explorviz.visualization.interaction;

public class TraceHighlighterJS {
	public static native void openDialog(String tableContent) /*-{
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
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();
		@explorviz.visualization.engine.navigation.Navigation::cancelTimers()();

		$doc.getElementById("traceHighlighterDialog").innerHTML = '<table id="traceChooser" class="hover" cellspacing="0" style="width:100%;height:95%">'
				+ tableContent + '</table>';

		$wnd.jQuery("#traceChooser").DataTable({
			"dom" : '<"top"f>rt<"bottom"ip><"clear">',
			ordering : true,
			"order": [[ 0, "asc" ]],
			paging : true,
		});

		$wnd
				.jQuery("[id^=choose-trace-button]")
				.click(
						function() {
							@explorviz.visualization.interaction.TraceHighlighter::choosenOneTrace(Ljava/lang/String;)($wnd
				.jQuery(this).attr("traceId"))
							$wnd.jQuery("#traceHighlighterDialog").dialog(
									'close');
						});
	}-*/;
}
