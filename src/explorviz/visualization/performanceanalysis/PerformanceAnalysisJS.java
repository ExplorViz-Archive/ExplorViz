package explorviz.visualization.performanceanalysis;

/**
 *
 * @author Daniel Jaehde
 *
 */
public class PerformanceAnalysisJS {

	public static native void showDialog(String applicationName) /*-{
		$wnd.jQuery("#performanceAnalysisDialog").show();
		$wnd.jQuery("#performanceAnalysisDialog").dialog({
			closeOnEscape : true,
			modal : false,
			resizable : false,
			title : 'Performance Analysis for ' + applicationName,
			width : 600,
			height : 300,
			position : {
				my : 'left top',
				at : 'left center',
				of : $wnd.jQuery("#view")
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();
		@explorviz.visualization.engine.navigation.Navigation::cancelTimers()();

		showOnlyCommunicationsAboveXmsec = '<tr><th>Show only communications above X msec:</th><td><input type="text" id="comoverXmsec" name="comoverXmsec" value="0"> </td><td><button id="toggleBtn" name="toggleBtn">Toggle</button></td></tr>'
		showCallingCardinalityinApplication = '<tr><th>Show cardinalities of application calls</th><td></td><td><button id="callsButton" name="callsButton">Show </button> </td></tr>'
		searchMethod = '<tr><th>Search and evaluate a method</th><td><input type="text" id="searchedMethod" name="searchedMethod"> </td><td><button id="searchButton" name="searchButton">Search </button> </td></tr>'

		//build performance analysis window
		$doc.getElementById("performanceAnalysisDialog").innerHTML = '<table>'
				+ showOnlyCommunicationsAboveXmsec
				+ showCallingCardinalityinApplication + searchMethod
				+ '</table>'

		//get spinner for ms-input
		$wnd.jQuery("#comoverXmsec").spinner({
			min : 0,
			disabled : true,
			stop : function(event, ui) {
				showCommusOver(this.value);
			}
		});

		//function for displaying communications over ms-value
		function showCommusOver(inputValue) {
			if (inputValue > 0) {
				@explorviz.visualization.performanceanalysis.PerformanceAnalysis::showOnlyCommunicationsAboveXms(I)(inputValue);
			} else {
				@explorviz.visualization.performanceanalysis.PerformanceAnalysis::showAllCommunications()();
			}
		}

		//toggle button for ms-input
		$wnd
				.jQuery("#toggleBtn")
				.click(
						function() {
							if ($wnd.jQuery("#comoverXmsec").spinner("option",
									"disabled")) {
								$wnd.jQuery("#comoverXmsec").spinner("enable");
								showCommusOver($wnd.jQuery("#comoverXmsec")
										.spinner("value"));
							} else {
								$wnd.jQuery("#comoverXmsec").spinner("disable");
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
							var resultTabularBody = "<tbody>";
							var len = callsArray.length;
							if (len > 0) {
								//table head
								resultTabular += "<thead><tr><th>Name of method</th><th>Class of method</th><th>Overall Calls</th></tr></thead>";
								//table body (order is head -> foot -> body)
								//triplets -> i += 3
								for (var i = 0; i < len; i += 3) {
									var row = "<tr><td>" + callsArray[i]
											+ "</td><td>" + callsArray[i + 1]
											+ "</td><td>" + callsArray[i + 2]
											+ "</td></tr>";
									resultTabularBody += row;
									allCalls += callsArray[i + 2];
								}
								//body complete
								resultTabularBody += "</tbody>";
								//add tfoot with total
								resultTabular += '<tfoot><tr><td></td><td align="right">Sum of all calls &rArr; </td><td>'
										+ allCalls + '</td></tr></tfoot>';
								//add body
								resultTabular += resultTabularBody;
							} else {
								alert("Something went horribly wrong or no method calls are present.");
							}
							//table to show is finished
							$doc.getElementById("callsDialog").innerHTML = '<table id="callsTable" class="display" border = "1">'
									+ resultTabular + '</table>';
							$wnd.jQuery("#callsTable").DataTable();

							//display dialog
							$wnd.jQuery("#callsDialog").show();
							$wnd
									.jQuery("#callsDialog")
									.dialog(
											{
												closeOnEscape : true,
												modal : false,
												resizable : false,
												title : 'Table displaying all calls of the application',
												width : 1000,
												height : 600,
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
							if (!methodName || 0 === methodName.length) {
								alert("Missing method to search");
							} else {
								var methodArray = @explorviz.visualization.performanceanalysis.PerformanceAnalysis::searchMethod(Ljava/lang/String;)(methodName);
								//putting this array in new dialog
								var resultTabular = "";
								var len = methodArray.length;
								//if methods are found, put them into a table
								if (len > 0) {
									resultTabular += "<thead><tr><th>Source</th><th>Target</th><th>Calls</th></tr></thead><tbody>";
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
								$doc.getElementById("searchDialog").innerHTML = '<table id="searchTable" class="display" border ="1">'
										+ resultTabular + '</tbody></table>';
								$wnd.jQuery("#searchTable").DataTable();

								//display custom dialog with results of the search
								$wnd.jQuery("#searchDialog").show();
								$wnd
										.jQuery("#searchDialog")
										.dialog(
												{
													closeOnEscape : true,
													modal : false,
													resizable : false,
													title : 'Search results for '
															+ $wnd
																	.jQuery(
																			"#searchedMethod")
																	.val(),
													width : 1000,
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