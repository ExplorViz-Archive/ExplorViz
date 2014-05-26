package explorviz.visualization.adaptivemonitoring;

import java.util.List;

import explorviz.shared.adaptivemonitoring.AdaptiveMonitoringPattern;

public class AdaptiveMonitoringJS {
	public static native void showDialog(List<AdaptiveMonitoringPattern> patterns,
			String applicationName) /*-{
		$wnd.jQuery("#adaptiveMonitoringDialog").show();
		$wnd.jQuery("#adaptiveMonitoringDialog").dialog({
			title : "Adaptive Monitoring for " + applicationName
		});

		function addAdaptiveMonitoringPattern(stringToAdd) {
			@explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring::addPattern(Ljava/lang/String;)(stringToAdd)
		}

		function removeAdaptiveMonitoringPattern(stringToRemove) {
			@explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring::addPattern(Ljava/lang/String;)(stringToRemove)
		}
	}-*/;
}
