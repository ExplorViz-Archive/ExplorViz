package explorviz.visualization.experiment.landscapeexchange;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.model.Landscape;
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync;

public interface TutorialLandscapeExchangeServiceAsync extends LandscapeExchangeServiceAsync {

	void getCurrentLandscape(AsyncCallback<Landscape> callback);

	void getLandscape(long timestamp, AsyncCallback<Landscape> callback);

	void resetLandscape(AsyncCallback<Void> callback);

}
