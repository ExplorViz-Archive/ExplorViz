package explorviz.server.experiment;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.shared.model.Landscape;
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeService;

/**
 * @author Santje Finke
 * 
 */
public class TutorialLandscapeExchangeServiceImpl extends RemoteServiceServlet implements
		TutorialLandscapeExchangeService {
	private static final long serialVersionUID = -2306440473112747163L;

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
		// empty
	}

	@Override
	public Landscape getLandscape(final long timestamp) {
		return TutorialLandscapeCreator.createTutorialLandscape();
	}
}
