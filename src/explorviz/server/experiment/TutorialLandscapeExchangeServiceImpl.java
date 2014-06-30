package explorviz.server.experiment;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.repository.LandscapeRepositoryModel;
import explorviz.server.repository.RepositoryStarter;
import explorviz.shared.model.Landscape;
import explorviz.visualization.experiment.landscapeexchange.TutorialLandscapeExchangeService;

public class TutorialLandscapeExchangeServiceImpl extends RemoteServiceServlet implements
		TutorialLandscapeExchangeService {

	private static final long serialVersionUID = 1L;
	private static LandscapeRepositoryModel model;

	static {
		startRepository();
	}

	public static LandscapeRepositoryModel getModel() {
		return model;
	}

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
		return TutorialLandscapeCreator.createTutorialLandscape();
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
