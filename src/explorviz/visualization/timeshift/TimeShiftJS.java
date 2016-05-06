package explorviz.visualization.timeshift;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class TimeShiftJS {
	public static native void init() /*-{
		var graph = new $wnd.SimpleGraph("timeshiftChartDiv", {
			"xmax" : 30,
			"xmin" : 0,
			"ymax" : 40,
			"ymin" : 0,
			"xlabel" : "Time",
			"ylabel" : "Method Calls"
		});
	}-*/;

	public static void updateTimeshiftChart(final Map<Long, Long> data) {
		// final JavaScriptObject jsObj = convertToJSHashMap(data);
		//
		// nativeUpdateTimeshiftChart(jsObj);
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
//		var keys = Object.keys(jsObj);
//
//		var series1 = [];
//		keys.forEach(function(entry) {
//			series1.push({
//				x : Number(entry),
//				y : Number(jsObj[entry])
//			});
//		})
//
//		$wnd.jQuery(this).updateTimeshiftChart([ {
//			key : "Timeseries",
//			values : series1,
//			color : "#366eff",
//			area : true
//		} ]);
	}-*/;
}
