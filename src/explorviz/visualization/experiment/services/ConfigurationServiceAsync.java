package explorviz.visualization.experiment.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationServiceAsync {

	void saveConfiguration(final String language, final boolean experiment, final boolean skip,
			AsyncCallback<Void> callback);

}
