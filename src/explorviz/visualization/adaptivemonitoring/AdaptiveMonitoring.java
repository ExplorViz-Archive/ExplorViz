package explorviz.visualization.adaptivemonitoring;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import explorviz.shared.adaptivemonitoring.AdaptiveMonitoringPattern;

public class AdaptiveMonitoring {
	private static AdaptiveMonitoringServiceAsync adaptiveMonitoringService;

	public static void init() {
		adaptiveMonitoringService = createAsyncService();
	}

	public static void openDialog() {
		adaptiveMonitoringService
				.getAdaptiveMonitoringPatterns(new GetAdaptiveMonitoringPatternsCallback());
	}

	private static AdaptiveMonitoringServiceAsync createAsyncService() {
		final AdaptiveMonitoringServiceAsync service = GWT.create(AdaptiveMonitoringService.class);
		final ServiceDefTarget endpoint = (ServiceDefTarget) service;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "adaptivemonitoring");

		return service;
	}

	private static class GetAdaptiveMonitoringPatternsCallback implements
			AsyncCallback<List<AdaptiveMonitoringPattern>> {
		@Override
		public void onFailure(final Throwable caught) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSuccess(final List<AdaptiveMonitoringPattern> patterns) {
			AdaptiveMonitoringJS.showDialog(patterns);
		}
	}
}
