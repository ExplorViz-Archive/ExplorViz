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

		showOnlyCommunicationsAboveXmsec = '<tr><th>Show only commu above Xmsec:</th><td><input type="text" id="comoverXmsec" name="comoverXmsec" value="0"> </td><td><input type="checkbox" id="showOnlyCommunicationsAboveXmsec" name="showOnlyCommunicationsAboveXmsec" value="showOnlyCommunicationsAboveXmsec"></td></tr>'
		showCallingCardinalityinApplication = '<tr><th>Show cardinality of application calls</th><td><button id="callsButton" name="callsButton">Show </button> </td></tr>'
		searchMethod = '<tr><th>Search and evaluate a method</th><td><input type="text" id="searchedMethod" name="searchedMethod"> </td><td><button id="searchButton" name="searchButton">Search </button> </td></tr>'

		$doc.getElementById("performanceAnalysisDialog").innerHTML = '<table>'
				+ showOnlyCommunicationsAboveXmsec
				+ showCallingCardinalityinApplication + searchMethod
				+ '</table>'

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

		$wnd
				.jQuery("#searchButton")
				.click(
						function() {
							var methodName = $wnd.jQuery("#searchedMethod")
									.val();
							//check if string is empty, null or undefined		
							if (!methodName || 0 === methodName.length) {
								alert("Missing method to search");
							} else {
								var methodArray = @explorviz.visualization.performanceanalysis.PerformanceAnalysis::searchMethod(Ljava/lang/String;)(methodName);
								//putting this array in new dialog
								var resultTabular = "<tr><td>Source</td><td>Target</td><td>Calls</td></tr>";
								var len = methodArray.length;
								//iterating +3 because its always source, target and calls
								for (var i = 0; i < len; i += 3) {
									var row = "<tr><td>" + methodArray[i]
											+ "</td><td>" + methodArray[i + 1]
											+ "</td><td>" + methodArray[i + 2]
											+ "</td></tr>";
									resultTabular += row;
								}
								//alert("Tabellenstring: " + resultTabular);
								//table to show is finished
								$doc.getElementById("searchDialog").innerHTML = '<table>'
										+ resultTabular + '</table>';
								$wnd.jQuery("#searchDialog").show();
								$wnd.jQuery("#searchDialog").dialog(
										{
											closeOnEscape : true,
											modal : false,
											resizable : true,
											title : 'Search results for '
													+ $wnd.jQuery(
															"#searchedMethod")
															.val(),
											width : 700,
											height : 300,
											position : {
												my : 'left top',
												at : 'left center',
												of : $wnd.jQuery("#view")
											}
										}).focus();
							}
						});
	}-*/
	;
}