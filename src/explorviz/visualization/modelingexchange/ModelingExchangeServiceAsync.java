package explorviz.visualization.modelingexchange;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.model.Landscape;
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync;

public interface ModelingExchangeServiceAsync extends LandscapeExchangeServiceAsync {
	void getCurrentLandscape(AsyncCallback<Landscape> callback);

	void saveLandscape(Landscape landscape, AsyncCallback<Void> callback);
}
