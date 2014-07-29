package explorviz.visualization.experiment.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationServiceAsync {

	void saveConfiguration(String language, boolean experiment, boolean skip,
			AsyncCallback<Void> callback);

}
