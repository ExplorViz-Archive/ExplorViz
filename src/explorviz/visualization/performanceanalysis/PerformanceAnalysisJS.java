package explorviz.visualization.performanceanalysis;

public class PerformanceAnalysisJS {
	// private static final Logger log = Logger.getLogger("JSDebug");

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

		showOnlyCommunicationsAboveXmsec = '<tr><th>Show only commu above Xmsec:</th><td><input type="text" id="comoverXmsec" name="comoverXmsec" value="0"> </td><td><input type="checkbox" id="showOnlyCommunicationsAboveXmsec" name="showOnlyCommunicationsAboveXmsec" value="showOnlyCommunicationsAboveXmsec"></td></tr>'
		showCallingCardinalityinApplication = '<tr><th>Show cardinality of application calls</th><td><button id="callsButton" name="callsButton">Show </button> </td></tr>'

		$doc.getElementById("performanceAnalysisDialog").innerHTML = '<table>'
				+ showOnlyCommunicationsAboveXmsec
				+ showCallingCardinalityinApplication + '</table>'

		$wnd
				.jQuery("#showOnlyCommunicationsAboveXmsec")
				.change(
						function() {
							if (this.checked) {
								var inputValue = parseInt($wnd.jQuery(
										"#comoverXmsec").val());
								@explorviz.visualization.performanceanalysis.PerformanceAnalysis::showOnlyCommunicationsAboveXms(I)(inputValue);
							} else {
								@explorviz.visualization.performanceanalysis.PerformanceAnalysis::showAllCommunications()();
							}
						});
		$wnd
				.jQuery("#callsButton")
				.click(
						function() {
							var calls = @explorviz.visualization.performanceanalysis.PerformanceAnalysis::getCallingCardinalityForMethods()();
							alert("Overall calls in application: " + calls);
						});
	}-*/
	;
}