package explorviz.server.experiment;

import explorviz.server.landscapeexchange.LandscapeExchangeServiceImpl;
import explorviz.shared.model.Landscape;

public class TutorialLandscapeExchangeServiceImpl extends LandscapeExchangeServiceImpl {

	private static final long serialVersionUID = 1L;

	public Landscape getCurrentLandscape() {
		return TutorialLandscapeCreator.createTutorialLandscape();
	}

	public Landscape getLandscape(final long timestamp) {
		// TODO different landscapes for different times
		return TutorialLandscapeCreator.createTutorialLandscape();
	}
}
