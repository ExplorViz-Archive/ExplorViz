package explorviz.visualization.timeshift;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class TimeShiftJS {
	public static native void init() /*-{

		var firstDataDone = false;
		var dataPointPixelRatio = 17;
		var panning = false;
		
		var dataBegin = 0;		
		var dataEnd = 0;

		Date.prototype.ddhhmmss = function() {
			var weekday = new Array(7);
			weekday[0] = "Su";
			weekday[1] = "Mo";
			weekday[2] = "Tu";
			weekday[3] = "We";
			weekday[4] = "Th";
			weekday[5] = "Fr";
			weekday[6] = "Sa";
			var n = weekday[this.getDay()];

			var hh = this.getHours().toString();
			var mm = this.getMinutes().toString();
			var ss = this.getSeconds().toString();

			return n + " " + (hh[1] ? hh : "0" + hh[0]) + ":" + (mm[1] ? mm : "0" + mm[0]) + ":"
					+ (ss[1] ? ss : "0" + ss[0]);
		};

		var timeshiftChartDiv = $wnd.jQuery("#timeshiftChartDiv");

		//$doc.getElementById('timeshiftChartDiv').style.height = '100px';
		//$doc.getElementById('timeshiftChartDiv').style.width = '500px';

		var dataSet = [ {
			data : [],
			label : "Method Calls",
			color : '#058DC7'
		} ];

		var options = {
			series : {
				lines : {
					show : true,
					fill : true,
					fillColor : "rgba(86, 137, 191, 0.8)"
				},
				points : {
					show : true,
					radius : 2
				},
				downsample : {
					threshold : 0
				// 0 disables downsampling for this series.
				},
				shadowSize : 0,
				highlightColor: "rgba(255, 0, 0, 0.8)"
			},
			axisLabels : {
				show : true
			},
			legend : {
				show : false
			},
			grid : {
				hoverable : true,
				clickable : true
			},
			xaxis : {
				axisLabel: "Time",
				mode : "time",
				timezone : "browser"
			},
			yaxis : {
				position: 'left',
				axisLabel: 'Method Calls',
				tickFormatter: function(x) {return x >= 1000 ? (x/1000)+"k": x;},
				ticks : 1,
				tickDecimals : 0,
				zoomRange : false	
			},
			zoom : {
				interactive : true
			},
			pan : {
				interactive : true,
				cursor: "default",
				frameRate: 60
			}
		};

		var plot = $wnd.$.plot(timeshiftChartDiv, dataSet, options);

		function addTooltipDiv() {
			$wnd.jQuery("<div id='timeshiftTooltip'></div>").css({
				position : "absolute",
				display : "none",
				border : "1px solid #fdd",
				padding : "2px",
				"background-color" : "#fee",
				opacity : 0.80
			}).appendTo("#timeshiftChartDiv");
		}

		addTooltipDiv();

		$wnd.jQuery("#timeshiftChartDiv").bind("dblclick", onDblclick);
		$wnd.jQuery("#timeshiftChartDiv").bind("plotzoom", onZoom);
		$wnd.jQuery("#timeshiftChartDiv").bind("plothover", onHover);
		$wnd.jQuery("#timeshiftChartDiv").bind("plotclick", onPointSelection);
		$wnd.jQuery("#timeshiftChartDiv").bind("plotpan", onPanning);
		$wnd.jQuery("#timeshiftChartDiv").bind("mouseup", onPanningEnd);		
		
		//Due to no difference between panning and click events, we need to differentiate.
		function onPanningEnd() {		
			setTimeout(function() {
				panning = false; 
				}
				, 100);			
		}
		
		function onPanning() {
			panning = true;	
		}

		function onPointSelection(event, pos, item) {			
			if (item && !panning) {		
				plot.unhighlight();
				plot.highlight(item.series, item.datapoint);
				@explorviz.visualization.landscapeexchange.LandscapeExchangeManager::stopAutomaticExchange(Ljava/lang/String;)(item.datapoint[0].toString());
				@explorviz.visualization.interaction.Usertracking::trackFetchedSpecifcLandscape(Ljava/lang/String;)(item.datapoint[0].toString());
				@explorviz.visualization.landscapeexchange.LandscapeExchangeManager::fetchSpecificLandscape(Ljava/lang/String;)(item.datapoint[0].toString());
			}
		}

		function onDblclick() {
			
			options.xaxis.min = dataSet[0].data[0][0];
			options.xaxis.max = dataSet[0].data[dataSet[0].data.length - 1][0];
			
			var innerWidth = $wnd.innerWidth;
			
			options.series.downsample.threshold = parseInt(innerWidth / dataPointPixelRatio);
			
			redraw();
		}

		function onHover(event, pos, item) {			
			if (item) {
				var x = item.datapoint[0];
				var y = item.datapoint[1];
				// view-div offset
				var offset = $wnd.jQuery("#view").offset().top;
				// timechart-div offset
				offset += $wnd.jQuery("#timeshiftChartDiv").position().top;
				showTooltip(item.pageX + 15, (item.pageY - offset), "Method Calls: " + y);
			} else {
				$wnd.jQuery("#timeshiftTooltip").hide();
			}
		}

		function onZoom() {
			//console.log("zooming");
		}

		function showTooltip(x, y, contents) {
			$wnd.jQuery("#timeshiftTooltip").html(contents).css({
				top : y,
				left : x
			}).fadeIn(200);
		}
		
		function setDatapointsAndOptions(convertedValues, convDataLength) {
			var innerWidth = $wnd.innerWidth;
			var dataSetLength = dataSet[0].data.length;	
			
			var numberOfPointsToShow = parseInt(innerWidth / dataPointPixelRatio);
			
			dataBegin = dataSetLength - numberOfPointsToShow <= 0 ? 
				0 : (dataSetLength - numberOfPointsToShow) ;			
			
			dataEnd = dataSetLength-1;
			
//			console.log("test");
			var newXMin = dataSet[0].data[dataBegin][0];
//			console.log("newxMin: " + newXMin);
			var newXMax = dataSet[0].data[dataEnd][0];
//			console.log("newXMax: " + newXMax);
			var oldXMin = firstDataDone ? dataSet[0].data[0][0] : newXMin;
//			console.log("oldXMin: " + oldXMin);
			var newYMax = Math.max.apply(Math, convertedValues.map(function(o) {
				return o[1];
			}));
//			console.log("newYMax: " + newYMax);

			if (!firstDataDone)
				firstDataDone = true

			options.xaxis.min = newXMin;
			options.xaxis.max = newXMax;			

			options.xaxis.panRange = [ oldXMin, dataSet[0].data[dataSetLength-1][0] ];
			options.yaxis.panRange = [ 0, newYMax ];				
			options.yaxis.max = newYMax;		
			options.series.downsample.threshold = 0;	
		}
		
		function redraw() {
			if(!panning) {
				console.log(dataSet);
				plot = $wnd.$.plot(timeshiftChartDiv, dataSet, options);
				addTooltipDiv();
			}
		}	

		$wnd.jQuery.fn.updateTimeshiftChart = function(data) {
				
			var values = data[0].values;
			
			var convertedValues = values.map(function(o) {
				return [ o.x, o.y ];
			});
			
			var newElemObj = values[values.length - 1];
			var newElem = [newElemObj.x, newElemObj.y];				
			
			dataSet[0].data.push(newElem);
			//dataSet[0].data = dataSet[0].data.concat(newElem);
			//dataSet[0].data = convertedValues;

			var convDataLength = convertedValues.length;
			
			console.log(convDataLength);
			
			if(convDataLength > 0) {
				setDatapointsAndOptions(convertedValues, convDataLength);
				redraw();
			}
		}
		
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
		});

		$wnd.jQuery(this).updateTimeshiftChart([ {
			key : "Timeseries",
			values : series1,
			color : "#366eff",
			area : true
		} ]);
	}-*/;
}
