package explorviz.visualization.experiment.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("configurationservice")
public interface ConfigurationService extends RemoteService {

	void saveConfiguration(String language, boolean experiment, boolean skip);

}
