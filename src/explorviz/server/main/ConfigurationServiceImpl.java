package explorviz.server.main;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.database.DBConnection;
import explorviz.visualization.experiment.services.ConfigurationService;

/**
 * @author Santje Finke
 *
 */
public class ConfigurationServiceImpl extends RemoteServiceServlet implements ConfigurationService {

	private static final long serialVersionUID = 2272248579894988743L;

	@Override
	public void saveConfiguration(final String language, final boolean experiment,
			final boolean skip) {
		Configuration.selectedLanguage = language;
		Configuration.experiment = experiment;
		Configuration.skipQuestion = skip;
		if (experiment) {
			DBConnection.createUsersForExperimentIfNotExist(300);
		}
	}

	@Override
	public void saveConfig(final String language, final boolean experiment, final boolean skip,
			final String filename) {
		Configuration.selectedLanguage = language;
		Configuration.experiment = experiment;
		Configuration.skipQuestion = skip;
		Configuration.experimentFilename = filename;
	}
}
