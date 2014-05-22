package explorviz.visualization.adaptivemonitoring;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.adaptivemonitoring.AdaptiveMonitoringPattern;

public interface AdaptiveMonitoringServiceAsync {

	public void getAdaptiveMonitoringPatterns(
			AsyncCallback<List<AdaptiveMonitoringPattern>> callback);

	void addPattern(AdaptiveMonitoringPattern pattern, AsyncCallback<Boolean> callback);

	void removePattern(AdaptiveMonitoringPattern pattern, AsyncCallback<Boolean> callback);
}
