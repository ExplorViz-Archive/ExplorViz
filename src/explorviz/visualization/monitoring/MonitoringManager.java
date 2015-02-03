package explorviz.visualization.monitoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class MonitoringManager {
	private static MonitoringServiceAsync metaMonitoringService;

	private static boolean MONITORING_ENABLED = true;

	public static void init() {
		if (MONITORING_ENABLED) {
			metaMonitoringService = createAsyncService();
			AspectWeaver.weave();
		}
	}

	private static MonitoringServiceAsync createAsyncService() {
		final MonitoringServiceAsync metaMonitoringService = GWT.create(MonitoringService.class);
		final ServiceDefTarget endpoint = (ServiceDefTarget) metaMonitoringService;
		final String moduleRelativeURL = GWT.getModuleBaseURL() + "monitoring";
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		return metaMonitoringService;
	}

	public static void sendRecordBundle(final String bundle) {
		metaMonitoringService.sendRecordBundle(bundle, new AsyncCallback<Void>() {
			@Override
			public void onFailure(final Throwable caught) {
			}

			@Override
			public void onSuccess(final Void result) {
			}

		});
	}
}
