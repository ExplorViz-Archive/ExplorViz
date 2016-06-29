package explorviz.visualization.landscapeexchange;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.model.Landscape;

public interface LandscapeExchangeServiceAsync {
	void getCurrentLandscape(AsyncCallback<Landscape> callback);

	void resetLandscape(AsyncCallback<Void> callback);

	void getLandscape(long timestamp, AsyncCallback<Landscape> callback);

	void getLandscapeByTimestampAndActivity(long timestamp, long activity,
			AsyncCallback<Landscape> callback);
}
