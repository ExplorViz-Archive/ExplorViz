package explorviz.server.main;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.database.DBConnection;
import explorviz.visualization.experiment.services.ConfigurationService;

public class ConfigurationServiceImpl extends RemoteServiceServlet implements ConfigurationService {

	private static final long serialVersionUID = 2272248579894988743L;

	@Override
	public void saveConfiguration(final String language, final boolean experiment) {
		Configuration.selectedLanguage = language;
		Configuration.experiment = experiment;

		if (experiment) {
			DBConnection.createUsersForExperimentIfNotExist(300);
		}
	}
}
