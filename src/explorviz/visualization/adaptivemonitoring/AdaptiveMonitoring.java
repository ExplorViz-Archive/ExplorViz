package explorviz.visualization.adaptivemonitoring;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import explorviz.shared.adaptivemonitoring.AdaptiveMonitoringPattern;
import explorviz.shared.model.Application;
import explorviz.visualization.main.ErrorDialog;

public class AdaptiveMonitoring {
	private static AdaptiveMonitoringServiceAsync adaptiveMonitoringService;

	public static void init() {
		adaptiveMonitoringService = createAsyncService();
	}

	public static void openDialog(final Application application) {
		adaptiveMonitoringService
				.getAdaptiveMonitoringPatterns(new GetAdaptiveMonitoringPatternsCallback(
						application));
	}

	public static void addPattern(final String regExpression) {
		final AdaptiveMonitoringPattern pattern = new AdaptiveMonitoringPattern();
		pattern.setActive(true);
		pattern.setPattern(regExpression);

		adaptiveMonitoringService.addPattern(pattern, new AddPatternCallback());
	}

	public static void removePattern(final String regExpression) {
		final AdaptiveMonitoringPattern pattern = new AdaptiveMonitoringPattern();
		pattern.setPattern(regExpression);

		adaptiveMonitoringService.removePattern(pattern, new RemovePatternCallback());
	}

	private static AdaptiveMonitoringServiceAsync createAsyncService() {
		final AdaptiveMonitoringServiceAsync service = GWT.create(AdaptiveMonitoringService.class);
		final ServiceDefTarget endpoint = (ServiceDefTarget) service;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "adaptivemonitoring");

		return service;
	}

	private static class GetAdaptiveMonitoringPatternsCallback implements
			AsyncCallback<List<AdaptiveMonitoringPattern>> {
		Application currentApplication;

		public GetAdaptiveMonitoringPatternsCallback(final Application app) {
			currentApplication = app;
		}

		@Override
		public void onFailure(final Throwable caught) {
			ErrorDialog.showError(caught);
		}

		@Override
		public void onSuccess(final List<AdaptiveMonitoringPattern> patterns) {
			AdaptiveMonitoringJS.showDialog(patterns, currentApplication.getName());
		}
	}

	private static class AddPatternCallback implements AsyncCallback<Boolean> {
		@Override
		public void onFailure(final Throwable caught) {
			ErrorDialog.showError(caught);
		}

		@Override
		public void onSuccess(final Boolean success) {
		}
	}

	private static class RemovePatternCallback implements AsyncCallback<Boolean> {
		@Override
		public void onFailure(final Throwable caught) {
			ErrorDialog.showError(caught);
		}

		@Override
		public void onSuccess(final Boolean success) {
		}
	}
}
