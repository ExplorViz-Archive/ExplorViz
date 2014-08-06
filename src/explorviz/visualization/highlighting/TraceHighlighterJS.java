package explorviz.visualization.highlighting;

public class TraceHighlighterJS {
	public static native void openDialog(String tableContent, boolean tutorial) /*-{
		$wnd.jQuery("#traceHighlighterDialog").show();
		$wnd.jQuery("#traceHighlighterDialog").dialog({
			closeOnEscape : true,
			modal : true,
			title : 'Choose Trace',
			width : '80%',
			resizable: false,
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
			"order": [[ 0, "asc" ], [ 2, "asc" ]],
			paging : true,
		});
		
		$wnd.jQuery("#traceChooser").on('draw.dt', function () {
			$wnd
				.jQuery("[id^=choose-trace-button]")
				.click(
						function() {
							@explorviz.visualization.highlighting.TraceHighlighter::choosenOneTrace(Ljava/lang/String;Ljava/lang/String;)($wnd
				.jQuery(this).attr("traceId"), $wnd
				.jQuery(this).attr("orderId"))
							$wnd.jQuery("#traceHighlighterDialog").dialog(
									'close');
						})} );

		$wnd
				.jQuery("[id^=choose-trace-button]")
				.click(
						function() {
							@explorviz.visualization.highlighting.TraceHighlighter::choosenOneTrace(Ljava/lang/String;Ljava/lang/String;)($wnd
				.jQuery(this).attr("traceId"), $wnd
				.jQuery(this).attr("orderId"))
							$wnd.jQuery("#traceHighlighterDialog").dialog(
									'close');
						});
		if(tutorial){
			alert("show arrow");
			@explorviz.visualization.experiment.ExperimentJS::showChooseTraceArrow()();
		}
	}-*/;
}
