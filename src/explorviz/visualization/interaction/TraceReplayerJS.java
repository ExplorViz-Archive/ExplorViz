package explorviz.visualization.interaction;

public class TraceReplayerJS {
	public static native void openDialog(String traceId, String tableInformation) /*-{
		$wnd.jQuery("#traceReplayerDialog").show();
		$wnd.jQuery("#traceReplayerDialog").dialog({
			closeOnEscape : true,
			title : 'Analyzing Trace ' + traceId,
			position : {
				my : 'center center',
				at : 'center center',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();
		@explorviz.visualization.engine.navigation.Navigation::cancelTimers()();

		previousButton = '<button id="traceReplayPrevious" type="button" class="btn btn-default btn-sm" style="width:100%;"><span class="glyphicon glyphicon glyphicon-backward"></span> Previous </button>'
		playButton = '<button id="traceReplayStartPause" type="button" class="btn btn-default btn-sm" style="width:100%;"><span class="glyphicon glyphicon glyphicon-play"></span> Play</button>'
		nextButton = '<button id="traceReplayNext" type="button" class="btn btn-default btn-sm" style="width:100%;"><span class="glyphicon glyphicon glyphicon-forward"></span> Next</button>'

		$doc.getElementById("traceReplayerDialog").innerHTML = '<table id="traceReplayer" cellspacing="0" style="width:100%;height:95%">'
				+ tableInformation
				+ '</table><hr style="margin-top: 1em;margin-bottom: 1em;"><table style="width:100%;"><tbody><tr style="width:100%;"><td style="width:33%;padding-right: 5px;">'
				+ previousButton
				+ '</td><td style="width:33%;padding-right: 5px;">'
				+ playButton
				+ '</td><td style="width:33%;">'
				+ nextButton
				+ '</td></tr></tbody></table>';

		var playing = false

		$wnd.jQuery("#traceReplayPrevious").click(function() {
			@explorviz.visualization.interaction.TraceReplayer::previous()();
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
								@explorviz.visualization.interaction.TraceReplayer::play()();
							} else {
								@explorviz.visualization.interaction.TraceReplayer::pause()();
								$wnd
										.jQuery("#traceReplayStartPause")
										.html(
												'<span class="glyphicon glyphicon glyphicon-play"></span> Play')
								playing = false
							}
						});
		$wnd.jQuery("#traceReplayNext").click(function() {
			@explorviz.visualization.interaction.TraceReplayer::next()();
		});
	}-*/;

	public static native void updateInformation(String tableInformation) /*-{
		$doc.getElementById("traceReplayer").innerHTML = tableInformation;
	}-*/;
}
