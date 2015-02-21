package explorviz.visualization.adaptivemonitoring;

import java.util.List;

import explorviz.shared.adaptivemonitoring.AdaptiveMonitoringPattern;

/**
 * @author Soenke Beyer
 *
 */
public class AdaptiveMonitoringJS {
	public static native void showDialog(final List<AdaptiveMonitoringPattern> patterns,
			final String applicationName) /*-{
		$wnd.jQuery("#adaptiveMonitoringDialog").show();
		$wnd.jQuery("#adaptiveMonitoringDialog").dialog({
			title : "Adaptive Monitoring for " + applicationName,
			resizable : false,
		});

		var array = patterns.@java.util.List::toArray()();

		$wnd.jQuery("#adaptiveMonitoringDialog").empty();

		//Add monitor to the list
		$wnd
				.jQuery("#adaptiveMonitoringDialog")
				.append(
						'<form name="EingabeMonitor" action="">'
								+ '<table border="1"><td><input type="button" class="button"'
								+ ' onclick=addAdaptiveMonitoringPattern(this.form.inputNewMonitor.value)'
								+ ' value="Add Monitor"></td>'
								+ '<td><input type="text" name="inputNewMonitor"</td></table>'
								+ '</form>' + '<p> </p>');

		//Table for the monitornames and the monitorstate
		//test(this.form.inputNewMonitor.value)
		$wnd
				.jQuery("#adaptiveMonitoringDialog")
				.append(
						'<table id="thetable" style="height: 400px; overflow: scroll;"'
								+ 'class="table table-striped" border="1" ><tr>'
								+ '<th style="width: 300px;" bgcolor="#EEEEEE">Name</th>'
								+ '<th style="width: 300px;" bgcolor="#EEEEEE">Status</th>'
								+ '<th style="width: 300px;" bgcolor="#EEEEEE">Remove</th></tr>'
								+ printTable());

		// PrintTable creates the table with all monitors and the options for the monitors
		function printTable() {
			// The table will be filled with the Name,Status and Remove of the monitor.
			var tabelle = '';
			for (i = 0; i < array.length; i++) {
				tabelle += '<td style="width: 300px;" bgcolor="#EEEEEE">'
						+ array[i]["_pattern"] + '</td>';
				if (array[i]["_active"]) {
					// Adds all elements which are active.
					tabelle += '<td> <input id="'
							+ array[i]["_pattern"]
							// Displays the checkbox to activte/deactivate the monitor.
							+ '</td><td>" <input type=checkbox checked onclick=setMonitorValue('
							+ i + ')><\/td>'
							// Displays the removebutton.
							+ '</td><td> <input type=button value="remove"'
							+ ' onclick=removeAdaptiveMonitoringPattern('
							+ array[i]["_pattern"] + ')><\/td></tr>';
				} else {
					// Adds all elements which are not active.
					tabelle += '<td> <input id="'
							+ array[i]["_pattern"]
							// Displays the checkbox to activte/deactivate the monitor.
							+ '</td><td>" <input type=checkbox onclick=setMonitorValue('
							+ i + ')><\/td>'
							// Displays the removebutton.
							+ '</td><td> <input type=button value="remove"'
							+ ' onclick=removeAdaptiveMonitoringPattern('
							+ array[i]["_pattern"] + ')><\/td></tr>';
				}
			}
			return tabelle;
		}

		// function to change the Monitor. Monitor can be active or not active
		$wnd.window.setMonitorValue = function(i) {
			if (array[i]["_active"]) {
				array[i]["_active"] = false;
			} else {
				array[i]["_active"] = true;
			}
			// Returns the actual state of the monitor. The user can check
			// that the monitor has the right state.
			console.log(array[i]["_active"]);
		}

		// Function to add a new monitor.
		$wnd.window.addAdaptiveMonitoringPattern = function(stringToAdd) {
			@explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring::addPattern(Ljava/lang/String;)(stringToAdd)
		}

		// Function to remove a monitor.
		$wnd.window.removeAdaptiveMonitoringPattern = function(stringToRemove) {
			@explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring::removePattern(Ljava/lang/String;)(stringToRemove)
		}
	}-*/
			;
}