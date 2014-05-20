package explorviz.visualization.experiment.landscapeexchange;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.model.Landscape;
import explorviz.visualization.landscapeexchange.LandscapeExchangeService;

@RemoteServiceRelativePath("tutoriallandscapeexchange")
public interface TutorialLandscapeExchangeService extends LandscapeExchangeService {
	public Landscape getCurrentLandscape();

	void resetLandscape();

	public Landscape getLandscape(long timestamp);
}