package explorviz.server.landscapeexchange;

import java.io.FileNotFoundException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.experiment.LandscapeReplayer;
import explorviz.server.main.Configuration;
import explorviz.server.repository.LandscapeRepositoryModel;
import explorviz.server.repository.RepositoryStarter;
import explorviz.shared.model.Landscape;
import explorviz.visualization.landscapeexchange.LandscapeExchangeService;

public class LandscapeExchangeServiceImpl extends RemoteServiceServlet implements
LandscapeExchangeService {

	private static final long serialVersionUID = 4310863128987822861L;
	private static LandscapeRepositoryModel model;

	static {
		startRepository();
	}

	public static LandscapeRepositoryModel getModel() {
		return model;
	}

	@Override
	public Landscape getCurrentLandscape() {
		if (Configuration.experiment) {
			final LandscapeReplayer replayer = LandscapeReplayer.getReplayerForCurrentUser();

			return replayer.getCurrentLandscape();
		} else {
			// return model.getLastPeriodLandscape();
			return LandscapeDummyCreator.createDummyLandscape();
		}
	}

	@Override
	public Landscape getLandscape(final long timestamp) {
		try {
			if (Configuration.experiment) {
				final LandscapeReplayer replayer = LandscapeReplayer.getReplayerForCurrentUser();

				return replayer.getLandscape(timestamp);
			} else {
				return model.getLandscape(timestamp);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void resetLandscape() {
		model.reset();
	}

	private static void startRepository() {
		model = new LandscapeRepositoryModel();
		final RepositoryStarter repositoryController = new RepositoryStarter();
		new Thread(new Runnable() {
			@Override
			public void run() {
				repositoryController.start(model);
			}
		}).start();
	}
}
