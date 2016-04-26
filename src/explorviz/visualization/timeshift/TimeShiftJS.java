package explorviz.visualization.timeshift;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class TimeShiftJS {
	public static native void init() /*-{
		var chart

		$wnd.jQuery.fn.updateTimeshiftChart = function(data) {
			$wnd.nv
					.addGraph(function() {
						$wnd.jQuery("#timeshiftChart").off("click touchstart");
						$wnd.jQuery("#timeshiftChartDiv svg").empty();			

						chart = $wnd.nv.models.lineChart().margin({
							left : 90,
							right : 60,
							top : 10,
							bottom : 40
						}).height(100).showLegend(false).tooltips(false)
								.useInteractiveGuideline(false).forceY("0");					

						chart.xAxis.axisLabel("Time").tickFormat(function(d) {
							return $wnd.d3.time.format('%H:%M:%S')(new Date(d))
						});

						chart.yAxis.axisLabel("Method calls").tickFormat(
								$wnd.d3.format("0,.2s"));						
						
						var xScale = chart.xAxis.scale();
						var x = $wnd.d3.scale.linear().domain([-width / 2, width / 2]).range([0, width]);	
						
						var zoom = $wnd.d3.behavior.zoom().x(x).scaleExtent([1, 1]).on("zoom", panning); 									
								
						$wnd.d3.select("#timeshiftChartDiv svg").datum(data)
								.call(chart).call(zoom);

						$wnd.nv.utils.windowResize(function() {
							chart.update();
						});
						
						var width = 960 - chart.margin.left - chart.margin.right;									
						  
						function panning() {
						  console.log("panning");
						  // svg.select(".x.axis").call(xAxis);	
						  //$wnd.d3.select("#timeshiftChartDiv svg").select(".x.axis").scale(x).tickSize(-100);
						  chart.xAxis.scale(x).tickSize(-100);
						  chart.update();
						}

						$wnd
								.jQuery("#timeshiftChart")
								.bind("click touchstart",
										function(e) {
											if (e.target !== undefined
													&& e.target.__data__ !== undefined
													&& e.target.__data__.data !== undefined) {
												@explorviz.visualization.landscapeexchange.LandscapeExchangeManager::stopAutomaticExchange(Ljava/lang/String;)(e.target.__data__.data["point"][4].x.toString())
												@explorviz.visualization.interaction.Usertracking::trackFetchedSpecifcLandscape(Ljava/lang/String;)(e.target.__data__.data["point"][4].x.toString());
												@explorviz.visualization.landscapeexchange.LandscapeExchangeManager::fetchSpecificLandscape(Ljava/lang/String;)(e.target.__data__.data["point"][4].x.toString());
											}
										});

						return chart;
					});
		}

		$wnd.jQuery(this).updateTimeshiftChart([ {
			key : "Timeseries",
			values : [],
			color : "#366eff",
			area : true
		} ]);
	}-*/;

	public static void updateTimeshiftChart(final Map<Long, Long> data) {
		final JavaScriptObject jsObj = convertToJSHashMap(data);

		nativeUpdateTimeshiftChart(jsObj);
	}

	private static JavaScriptObject convertToJSHashMap(final Map<Long, Long> data) {
		final JSONObject obj = new JSONObject();
		for (final Entry<Long, Long> entry : data.entrySet()) {
			obj.put(entry.getKey().toString(), new JSONString(entry.getValue().toString()));
		}

		final JavaScriptObject jsObj = obj.getJavaScriptObject();
		return jsObj;
	}

	public static native void nativeUpdateTimeshiftChart(JavaScriptObject jsObj) /*-{
		var keys = Object.keys(jsObj);

		var series1 = [];
		keys.forEach(function(entry) {
			series1.push({
				x : Number(entry),
				y : Number(jsObj[entry])
			});
		})

		$wnd.jQuery(this).updateTimeshiftChart([ {
			key : "Timeseries",
			values : series1,
			color : "#366eff",
			area : true
		} ]);
	}-*/;
}
