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
		showCallingCardinalityinApplication = '<tr><th>Show cardinalities of application calls</th><td></td><td><button id="callsButton" name="callsButton">Show </button> </td></tr>'
		searchMethod = '<tr><th>Search and evaluate a method</th><td><input type="text" id="searchedMethod" name="searchedMethod"> </td><td><button id="searchButton" name="searchButton">Search </button> </td></tr>'

		$doc.getElementById("performanceAnalysisDialog").innerHTML = '<table>'
				+ showOnlyCommunicationsAboveXmsec
				+ showCallingCardinalityinApplication + searchMethod
				+ '</table>'

		//get spinner for ms-input
		$wnd.jQuery("#comoverXmsec").spinner();
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
							var callsArray = @explorviz.visualization.performanceanalysis.PerformanceAnalysis::getCallingCardinalityForMethods()();
							var allCalls = 0;
							//putting this array in new dialog
							var resultTabular = "";
							var len = callsArray.length;
							if (len > 0) {
								resultTabular += "<tr><td>Name of method</td><td>Class of method</td><td>Overall Calls</td></tr>";
								//triplets -> i += 3
								for (var i = 0; i < len; i += 3) {
									var row = "<tr><td>" + callsArray[i]
											+ "</td><td>" + callsArray[i + 1]
											+ "</td><td>" + callsArray[i + 2]
											+ "</td></tr>";
									resultTabular += row;
									allCalls += callsArray[i + 2];
								}
								resultTabular += '<tr><td></td><td align="right">Sum of all calls &rArr; </td><td>'
										+ allCalls + '</td></tr>';
							} else {
								alert("Something went horribly wrong or no method calls are present.");
							}
							//table to show is finished
							$doc.getElementById("callsDialog").innerHTML = '<table border = "1">'
									+ resultTabular + '</table>';
							$wnd.jQuery("#callsDialog").show();
							$wnd
									.jQuery("#callsDialog")
									.dialog(
											{
												closeOnEscape : true,
												modal : false,
												resizable : true,
												title : 'Table displaying all calls of the application',
												width : 800,
												height : 300,
												position : {
													my : 'left top',
													at : 'left center',
													of : $wnd.jQuery("#view")
												}
											}).focus();
						});

		$wnd
				.jQuery("#searchButton")
				.click(
						function() {
							var methodName = $wnd.jQuery("#searchedMethod")
									.val();
							//check if string is empty, null or undefined		
							if (!methodName || 0 === methodName.length) {//TODO
								alert("Missing method to search");
							} else {
								var methodArray = @explorviz.visualization.performanceanalysis.PerformanceAnalysis::searchMethod(Ljava/lang/String;)(methodName);
								//putting this array in new dialog
								var resultTabular = "";
								var len = methodArray.length;
								//if methods are found, put them into a table
								if (len > 0) {
									resultTabular += "<tr><td>Source</td><td>Target</td><td>Calls</td></tr>";
									//iterating +3 because its always source, target and calls
									for (var i = 0; i < len; i += 3) {
										var row = "<tr><td>" + methodArray[i]
												+ "</td><td>"
												+ methodArray[i + 1]
												+ "</td><td>"
												+ methodArray[i + 2]
												+ "</td></tr>";
										resultTabular += row;
									}
								} else {
									resultTabular = "No matching method-names found. Maybe leave the \"()\" out?";
								}

								//table to show is finished
								$doc.getElementById("searchDialog").innerHTML = '<table border = "1">'
										+ resultTabular + '</table>';
								//display custom dialog with results of the search
								$wnd.jQuery("#searchDialog").show();
								$wnd
										.jQuery("#searchDialog")
										.dialog(
												{
													closeOnEscape : true,
													modal : false,
													resizable : true,
													title : 'Search results for '
															+ $wnd
																	.jQuery(
																			"#searchedMethod")
																	.val(),
													width : 800,
													height : 300,
													beforeClose : function(
															event, ui) {
														@explorviz.visualization.performanceanalysis.PerformanceAnalysis::showAllCommunications()();
													},
													position : {
														my : 'left top',
														at : 'left center',
														of : $wnd
																.jQuery("#view")
													}
												}).focus();
							}
						});
	}-*/
	;
}