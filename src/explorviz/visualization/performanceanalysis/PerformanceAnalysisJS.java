package explorviz.visualization.performanceanalysis;

/**
 *
 * @author Daniel Jaehde
 *
 */
public class PerformanceAnalysisJS {

	public static native void showDialog(String applicationName) /*-{
		$wnd.jQuery("#performanceAnalysisDialog").show();
		$wnd
				.jQuery("#performanceAnalysisDialog")
				.dialog(
						{
							closeOnEscape : true,
							modal : false,
							resizable : false,
							title : 'Performance Analysis for '
									+ applicationName,
							width : 590,
							height : 130,
							position : {
								my : 'center center',
								at : 'center top',
								of : $wnd.jQuery("#view"),
								close : function(ev, ui) {
									@explorviz.visualization.performanceanalysis.PerformanceAnalysis::setPerformanceAnalysisMode(Z)(false);
									$(this).remove();
								},
							}
						}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();
		@explorviz.visualization.engine.navigation.Navigation::cancelTimers()();

		showOnlyCommunicationsAboveXmsec = '<tr><th>Show only communications above X msec:&nbsp;&nbsp;</th><td><input type="text" id="comoverXmsec" name="comoverXmsec" value="0"> </td><td>&nbsp;&nbsp;<button style="width:60px;" id="toggleBtn" name="toggleBtn">Toggle</button></td></tr>'
		searchMethod = '<tr><th>Search for a method</th><td><input style="width:170px;" type="text" id="searchedMethod" name="searchedMethod"></td><td>&nbsp;&nbsp;<button style="width:60px;" id="searchButton" name="searchButton">Search </button> </td></tr>'

		$doc.getElementById("performanceAnalysisDialog").innerHTML = '<table style="width:99%">'
				+ showOnlyCommunicationsAboveXmsec + searchMethod + '</table>'

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
													height : 530,
													beforeClose : function(
															event, ui) {
														@explorviz.visualization.performanceanalysis.PerformanceAnalysis::showAllCommunications()();
													},
													position : {
														my : 'left top',
														at : 'left center',
														of : $wnd
																.jQuery("#view")
													},
												}).focus();
							}
						});
	}-*/
	;
}