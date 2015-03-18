package explorviz.visualization.modelingexchange;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.model.Landscape;
import explorviz.visualization.landscapeexchange.LandscapeExchangeService;

@RemoteServiceRelativePath("modelingexchange")
public interface ModelingExchangeService extends LandscapeExchangeService {
	public Landscape getCurrentLandscape();

	public void saveLandscape(Landscape landscape);
}
