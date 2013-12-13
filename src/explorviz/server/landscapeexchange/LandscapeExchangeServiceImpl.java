package explorviz.server.landscapeexchange;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

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

	@Override
	public Landscape getLandscape() {
		return model.getCurrentLandscape();
		// return LandscapeDummyCreator.createDummyLandscape();
		// return ReadStaticsFromSourceFolder.readInFolder("source/Neo4J");
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
