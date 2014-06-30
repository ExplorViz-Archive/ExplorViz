package explorviz.server.experiment;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.shared.model.Landscape;
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeService;

public class TutorialLandscapeExchangeServiceImpl extends RemoteServiceServlet implements
		TutorialLandscapeExchangeService {

	private static final long serialVersionUID = 1L;

	@Override
	public Landscape getCurrentLandscape() {
		return TutorialLandscapeCreator.createTutorialLandscape();
	}

	@Override
	public Landscape getCurrentLandscape2() {
		return TutorialLandscapeCreator.createTutorialLandscape2();
	}

	@Override
	public void resetLandscape() {
		// TODO
		System.out.println("reset landscape from tutoriallandscapeexchance called");
	}

	@Override
	public Landscape getLandscape(final long timestamp) {
		// TODO different landscapes for different times
		return TutorialLandscapeCreator.createTutorialLandscape();
	}
}
