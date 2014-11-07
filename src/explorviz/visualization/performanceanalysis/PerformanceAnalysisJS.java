package explorviz.visualization.performanceanalysis;

public class PerformanceAnalysisJS {
	public static native void showDialog(String applicationName) /*-{
		$wnd.jQuery("#performanceAnalysisDialog").show();
		$wnd.jQuery("#performanceAnalysisDialog").dialog({
			closeOnEscape : true,
			modal : false,
			resizable : false,
			title : 'Performance Analysis for ' + applicationName,
			width : 500,
			height : 300,
			position : {
				my : 'left top',
				at : 'left center',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();
		@explorviz.visualization.engine.navigation.Navigation::cancelTimers()();

		showOnlyCommunicationsAbove100msec = '<tr><th>Show only commu above 100msec:</th><td><input type="checkbox" id="showOnlyCommunicationsAbove100msec" name="showOnlyCommunicationsAbove100msec" value="showOnlyCommunicationsAbove100msec"></td></tr>'

		$doc.getElementById("performanceAnalysisDialog").innerHTML = '<table>'
				+ showOnlyCommunicationsAbove100msec + '</table>'

		$wnd
				.jQuery("#showOnlyCommunicationsAbove100msec")
				.change(
						function() {
							if (this.checked) {
								@explorviz.visualization.performanceanalysis.PerformanceAnalysis::showOnlyCommunicationsAbove100msec()();
							} else {
								@explorviz.visualization.performanceanalysis.PerformanceAnalysis::showAllCommunications()();
							}
						});
	}-*/
	;
}