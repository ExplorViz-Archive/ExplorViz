package explorviz.visualization.highlighting;

public class TraceReplayerJS {
	public static native void openDialog(String traceId, String tableInformation) /*-{
		$wnd.jQuery("#traceReplayerDialog").show();
		$wnd.jQuery("#traceReplayerDialog").dialog(
				{
					closeOnEscape : false,
					open : function(event, ui) {
						$wnd.jQuery(this).closest('.ui-dialog').find(
								'.ui-dialog-titlebar-close').hide();
					},
					resizable : false,
					title : 'Analyzing Trace ' + traceId,
					width : '30em',
					position : {
						my : 'left center',
						at : 'center center',
						of : $wnd.jQuery("#webglDiv")
					}
				}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();
		@explorviz.visualization.engine.navigation.Navigation::cancelTimers()();

		previousButton = '<button id="traceReplayPrevious" type="button" class="btn btn-default btn-sm" style="width:100%;"><span class="glyphicon glyphicon glyphicon-backward"></span> Previous </button>'
		playButton = '<button id="traceReplayStartPause" type="button" class="btn btn-default btn-sm" style="width:100%;"><span class="glyphicon glyphicon glyphicon-play"></span> Play</button>'
		nextButton = '<button id="traceReplayNext" type="button" class="btn btn-default btn-sm" style="width:100%;"><span class="glyphicon glyphicon glyphicon-forward"></span> Next</button>'

		optionalSelfEdges = '<tr><th>Self-Edges:</th><td><input type="checkbox" id="showSelfEdges" name="showSelfEdges" value="showSelfEdges"></td></tr>'

		$doc.getElementById("traceReplayerDialog").innerHTML = '<table id="traceReplayer" class="traceReplayer" cellspacing="0" style="width:100%;height:95%">'
				+ tableInformation
				+ '</table><table id="traceReplayerSelfEdges" class="traceReplayer" cellspacing="0" style="width:100%;height:95%">'
				+ optionalSelfEdges
				+ '</table>'
				+ '<hr style="margin-top: 1em;margin-bottom: 1em;"><table style="width:100%;"><tbody><tr style="width:100%;"><td style="width:33%;padding-right: 5px;">'
				+ previousButton
				+ '</td><td style="width:33%;padding-right: 5px;">'
				+ playButton
				+ '</td><td style="width:33%;">'
				+ nextButton
				+ '</td></tr></tbody></table>';

		var playing = false

		$wnd.jQuery("#traceReplayPrevious").click(function() {
			@explorviz.visualization.highlighting.TraceReplayer::previous()();
		});
		$wnd
				.jQuery("#traceReplayStartPause")
				.click(
						function() {
							if (!playing) {
								playing = true
								$wnd
										.jQuery("#traceReplayStartPause")
										.html(
												'<span class="glyphicon glyphicon glyphicon-pause"></span> Pause')
								@explorviz.visualization.highlighting.TraceReplayer::play()();
							} else {
								@explorviz.visualization.highlighting.TraceReplayer::pause()();
								$wnd
										.jQuery("#traceReplayStartPause")
										.html(
												'<span class="glyphicon glyphicon glyphicon-play"></span> Play')
								playing = false
							}
						});
		$wnd.jQuery("#traceReplayNext").click(function() {
			@explorviz.visualization.highlighting.TraceReplayer::next()();
		});

		$wnd
				.jQuery("#showSelfEdges")
				.change(
						function() {
							if (this.checked) {
								@explorviz.visualization.highlighting.TraceReplayer::showSelfEdges()();
							} else {
								@explorviz.visualization.highlighting.TraceReplayer::hideSelfEdges()();
							}
						});
	}-*/;

	public static native void updateInformation(String tableInformation) /*-{
		$doc.getElementById("traceReplayer").innerHTML = tableInformation;
	}-*/;

	public static native void closeDialog() /*-{
		if ($wnd.jQuery('#traceReplayerDialog').parents('.ui-dialog:visible').length) {
			$wnd.jQuery("#traceReplayerDialog").dialog('close');
		}
	}-*/;
}
