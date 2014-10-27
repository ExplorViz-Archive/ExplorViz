package explorviz.plugin.anomalydetection;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class OPADxClientSideJS {
	public static native void showTimeSeriesDialog(final String elementName) /*-{
		$wnd.jQuery("#timeSeriesDialog").show();
		$wnd.jQuery("#timeSeriesDialog").dialog({
			closeOnEscape : true,
			modal : true,
			resizable : false,
			title : 'Time series for ' + elementName,
			width : '70%',
			height : 430,
			position : {
				my : 'center center',
				at : 'center center',
				of : $doc
			}
		}).focus();

		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();

		$doc.getElementById("timeSeriesDialog").innerHTML = "<div id='anomalyChartDiv' style='height:100px;'><svg id='anomalyChart'></svg></div><div id='responseTimesChartDiv' style='height:250px;'><svg id='responseTimesChart'></svg></div>";

		@explorviz.plugin.anomalydetection.OPADxClientSideJS::init()();
	}-*/;

	public static native void init() /*-{
		var chart

		$wnd.jQuery.fn.makeDashed = function() {
			$wnd.d3.select("#anomalyChartDiv svg").selectAll(
					'.nv-linesWrap .nv-group').filter(function(g) {
				return g.key != 'Timeseries'
			}).selectAll('.nv-line').style('stroke-dasharray', ('10, 10'))
		}

		$wnd.jQuery.fn.updateAnomalyChart = function(data) {
			$wnd.nv.addGraph(function() {
				$wnd.jQuery("#anomalyChartDiv svg").empty();

				chart = $wnd.nv.models.lineChart().margin({
					left : 90,
					right : 60,
					top : 10,
					bottom : 5
				}).height(100).showLegend(false).tooltips(true)
						.useInteractiveGuideline(false).forceY([ 0, 1 ]);

				chart.xAxis.axisLabel("").tickFormat(function(d) {
					return $wnd.d3.time.format('%H:%M:%S')(new Date(d))
				});

				chart.yAxis.axisLabel("Anomaly Score").tickFormat(
						$wnd.d3.format("0,.2s"));

				chart.lines.interactive(false);

				$wnd.d3.select("#anomalyChartDiv svg").datum(data).call(chart);

				$wnd.jQuery(this).makeDashed();

				$wnd.d3.select("#anomalyChartDiv svg").append("svg:image")
						.attr("xlink:href", "images/logos/error.png").attr(
								"width", 16).attr("height", 16).attr("x", 86)
						.attr("y", 10);

				$wnd.d3.select("#anomalyChartDiv svg").append("svg:image")
						.attr("xlink:href", "images/logos/warning.png").attr(
								"width", 16).attr("height", 16).attr("x", 86)
						.attr("y", 35);

				return chart;
			});
		}

		$wnd.jQuery(this).updateAnomalyChart([ {
			key : "Timeseries",
			values : [],
			color : "#366eff",
			area : false
		} ]);

		$wnd.jQuery.fn.updateResponseTimesChart = function(data, maxYValue) {
			$wnd.nv.addGraph(function() {
				$wnd.jQuery("#responseTimesChartDiv svg").empty();

				chart = $wnd.nv.models.linePlusBarChart().margin({
					left : 90,
					right : 60,
					top : 15,
					bottom : 40
				}).height(250).showLegend(false);

				chart.xAxis.axisLabel("Time").tickFormat(function(d) {
					return $wnd.d3.time.format('%H:%M:%S')(new Date(d))
				});

				chart.y1Axis.axisLabel("Average Response Time").tickFormat(
						$wnd.d3.format("0,.2s"));

				chart.y2Axis.axisLabel("Average Response Time").tickFormat(
						$wnd.d3.format("0,.2s"));

				chart.lines.interactive(false);
				chart.lines.forceY([ 0, maxYValue ]);
				chart.bars.interactive(false);
				chart.bars.forceY([ 0, maxYValue ]);

				$wnd.d3.select("#responseTimesChartDiv svg").datum(data)
						.transition().duration(0).call(chart);

				$wnd.d3.select('.nv-y2.nv-axis').remove();

				return chart;
			});
		}

		$wnd.jQuery(this).updateResponseTimesChart([ {
			key : "ReponseTimes",
			values : [],
			color : "#888888",
			bar : true
		}, {
			key : "PredictedTimes",
			values : [],
			color : "#00FF00"
		} ], 1);
	}-*/;

	public static void updateAnomalyAndReponseTimesChart(final Map<Long, Float> dataAnomaly,
			final Map<Long, Float> dataResponseTimes, final Map<Long, Float> dataPredictedTimes) {
		final JavaScriptObject jsObj = convertToJSHashMap(dataAnomaly);
		nativeUpdateAnomalyChart(jsObj);

		final JavaScriptObject jsObjRT = convertToJSHashMap(dataResponseTimes);
		final JavaScriptObject jsObjPT = convertToJSHashMap(dataPredictedTimes);
		nativeUpdateResponseTimesChart(jsObjRT, jsObjPT);
	}

	private static JavaScriptObject convertToJSHashMap(final Map<Long, Float> data) {
		final JSONObject obj = new JSONObject();
		for (final Entry<Long, Float> entry : data.entrySet()) {
			obj.put(entry.getKey().toString(), new JSONString(entry.getValue().toString()));
		}

		final JavaScriptObject jsObj = obj.getJavaScriptObject();
		return jsObj;
	}

	public static native void nativeUpdateAnomalyChart(JavaScriptObject jsObj) /*-{
		var keys = Object.keys(jsObj);

		var series1 = [];
		var warningSeries = [];
		var errorSeries = [];
		keys.forEach(function(entry) {
			series1.push({
				x : Number(entry),
				y : Number(jsObj[entry])
			});
			warningSeries.push({
				x : Number(entry),
				y : 0.5
			});
			errorSeries.push({
				x : Number(entry),
				y : 0.8
			});
		})

		$wnd.jQuery(this).updateAnomalyChart([ {
			key : "ErrorLine",
			values : errorSeries,
			color : "#FF0000",
			area : false
		}, {
			key : "WarningLine",
			values : warningSeries,
			color : "#fccd00",
			area : false
		}, {
			key : "Timeseries",
			values : series1,
			color : "#366eff",
			area : false
		} ]);
	}-*/;

	public static native void nativeUpdateResponseTimesChart(JavaScriptObject jsObjReponseTimes,
			JavaScriptObject jsObjPredictedTimes) /*-{
		maxYValue = 1;

		var responseSeries = [];
		var keysResponseTimes = Object.keys(jsObjReponseTimes);
		keysResponseTimes.forEach(function(entry) {
			responseSeries.push({
				x : Number(entry),
				y : Number(jsObjReponseTimes[entry])
			});
			if (maxYValue < Number(jsObjReponseTimes[entry])) {
				maxYValue = Number(jsObjReponseTimes[entry])
			}
		})

		var predictedSeries = [];
		var keysPredictedTimes = Object.keys(jsObjPredictedTimes);
		keysPredictedTimes.forEach(function(entry) {
			predictedSeries.push({
				x : Number(entry),
				y : Number(jsObjPredictedTimes[entry])
			});
			if (maxYValue < Number(jsObjPredictedTimes[entry])) {
				maxYValue = Number(jsObjPredictedTimes[entry])
			}
		})

		$wnd.jQuery(this).updateResponseTimesChart([ {
			"bar" : true,
			key : "ReponseTimes",
			values : responseSeries,
			color : "#d3d3d3",
		}, {
			key : "PredictedTimes",
			values : predictedSeries,
			color : "#008000"
		} ], maxYValue);
	}-*/;
}
