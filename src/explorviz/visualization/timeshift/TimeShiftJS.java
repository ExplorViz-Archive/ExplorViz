package explorviz.visualization.timeshift;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class TimeShiftJS {
	public static native void init() /*-{
		function initData() {
			return [ {
				key : "Timeseries",
				values : [],
				color : "#366eff",
				area : true
			} ];
		}

		var chart

		$wnd.jQuery.fn.updateTimeshiftChart = function(data) {
			$wnd.nv.addGraph(function() {
				$wnd.jQuery("#timeshiftChart").off("click");
				$wnd.jQuery("#timeshiftChartDiv svg").empty();

				chart = $wnd.nv.models.lineChart().margin({
					left : 90,
					right : 60,
					top : 10,
					bottom : 40
				}).height(100).showLegend(false).tooltips(false)
						.useInteractiveGuideline(false);

				chart.xAxis.axisLabel("Time").tickFormat(function(d) {
					return $wnd.d3.time.format('%H:%M:%S')(new Date(d))
				});

				chart.yAxis.axisLabel("Activity").tickFormat(
						$wnd.d3.format("d"));

				$wnd.d3.select("#timeshiftChartDiv svg").datum(data)
						.call(chart);

				$wnd.nv.utils.windowResize(function() {
					chart.update();
				});

				$wnd.jQuery("#timeshiftChart").click(
						function(e) {
							if (e.target !== undefined
									&& e.target.__data__ !== undefined
									&& e.target.__data__.data !== undefined)
								alert(e.target.__data__.data["point"][4].x);
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
		final JSONObject obj = new JSONObject();
		for (final Entry<Long, Long> entry : data.entrySet()) {
			obj.put(entry.getKey().toString(), new JSONString(entry.getValue().toString()));
		}

		final JavaScriptObject jsObj = obj.getJavaScriptObject();

		nativeUpdateTimeshiftChart(jsObj);
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
