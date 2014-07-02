package explorviz.visualization.adaptivemonitoring;

import java.util.List;

import explorviz.shared.adaptivemonitoring.AdaptiveMonitoringPattern;

public class AdaptiveMonitoringJS {
	public static native void showDialog(final List<AdaptiveMonitoringPattern> patterns,
			final String applicationName) /*-{
		$wnd.jQuery("#adaptiveMonitoringDialog").show();
		@explorviz.visualization.engine.popover.PopoverService::hidePopover()();
		$wnd.jQuery("#adaptiveMonitoringDialog").dialog({
			title : "Adaptive Monitoring for " + applicationName
		});
		//Testcontent for the MonitorList
		addAdaptiveMonitoringPattern("get");
		addAdaptiveMonitoringPattern("set");
		addAdaptiveMonitoringPattern("main");
		array[0]["_active"] = false;
		array[5]["_active"] = false;
		array[7]["_active"] = false;

		var array = patterns.@java.util.List::toArray()();

		$wnd.jQuery("#adaptiveMonitoringDialog").empty();
		$wnd.jQuery("#adaptiveMonitoringDialog").append(
				"Elements in the List: " + patterns.@java.util.List::size()());

		//Add monitor to the list
		$wnd
				.jQuery("#adaptiveMonitoringDialog")
				.append(
						"<p>"
								+ '<input type="button" class="button" value="Add Monitor">'
								+ '<input class="form-control" type="text" size="50" name="addMonitor" class="display">'
								+ "</p>");

		//Remove Monitor from list
		$wnd
				.jQuery("#adaptiveMonitoringDialog")
				.append(
						'<p>'
								+ '<input type="button" class="button" value="Remove Monitor">'
								+ '<input class="form-control" type="text" size="50" name="removeMonitor" class="display">'
								+ '</p>');

		//Table for the monitornames and the monitorstate
		$wnd
				.jQuery("#adaptiveMonitoringDialog")
				.append(
						'<table  style="height: 400px;  overflow:scroll;" class="table table-striped" border="1" ><tr>'
								+ '<th style="width: 300px;" bgcolor="#EEEEEE">Monitor-Name</th>'
								+ '<th style="width: 300px;" bgcolor="#EEEEEE">Status</th></tr>'
								+ printTable());

		function printTable() {
			var tabelle = '<td style="width: 300px;" bgcolor="#EEEEEE">'
					+ array[0]["_pattern"]
					+ '</td><td> <input type=checkbox><\/td></tr>';

			for (i = 1; i < array.length; i++) {
				tabelle += '<td style="width: 300px;" bgcolor="#EEEEEE">'
						+ array[i]["_pattern"] + '</td>';
				if (array[i]["_active"]) {
					tabelle += '<td> <input id="' + array[i]["_pattern"]
							+ '" type=checkbox checked><\/td></tr>';
				} else {
					tabelle += '<td> <input id="' + array[i]["_pattern"]
							+ '" type=checkbox><\/td></tr>';
				}
			}

			return tabelle;

		}

		function addAdaptiveMonitoringPattern(stringToAdd) {
			@explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring::addPattern(Ljava/lang/String;)(stringToAdd)
		}

		function removeAdaptiveMonitoringPattern(stringToRemove) {
			@explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring::addPattern(Ljava/lang/String;)(stringToRemove)
		}
	}-*/
	;
}
