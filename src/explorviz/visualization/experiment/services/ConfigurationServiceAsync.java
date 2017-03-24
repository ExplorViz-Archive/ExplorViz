package explorviz.visualization.experiment.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Santje Finke
 *
 */
public interface ConfigurationServiceAsync {

	void saveConfiguration(final String language, final boolean experiment, final boolean skip,
			AsyncCallback<Void> callback);

	void saveConfig(String language, boolean experiment, boolean skip, String filename,
			AsyncCallback<Void> callback);

	void createUsersForICSAStudy(AsyncCallback<Void> callback);

	void getUsers(AsyncCallback<String[]> callback);
}
