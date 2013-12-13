package explorviz.visualization.landscapeexchange;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.model.Landscape;

public interface LandscapeExchangeServiceAsync {
	void getLandscape(AsyncCallback<Landscape> callback);

	void resetLandscape(AsyncCallback<Void> callback);
}
