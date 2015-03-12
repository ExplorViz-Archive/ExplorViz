package explorviz.server.modelingexchange;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.repository.RepositoryStorage;
import explorviz.shared.model.Landscape;
import explorviz.visualization.modelingexchange.ModelingExchangeService;

public class ModelingExchangeServiceImpl extends RemoteServiceServlet implements
ModelingExchangeService {

	private static final long serialVersionUID = -3224588733269050280L;

	@Override
	public Landscape getCurrentLandscape() {
		return RepositoryStorage.readTargetArchitecture();
	}

	@Override
	public Landscape getLandscape(final long timestamp) {
		return null;
	}

	@Override
	public void resetLandscape() {
		RepositoryStorage.saveTargetArchitecture(new Landscape());
	}

	@Override
	public void saveLandscape(final Landscape landscape) {
		RepositoryStorage.saveTargetArchitecture(landscape);
	}
}