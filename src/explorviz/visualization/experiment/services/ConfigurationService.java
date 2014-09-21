package explorviz.visualization.experiment.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Santje Finke
 * 
 */
@RemoteServiceRelativePath("configurationservice")
public interface ConfigurationService extends RemoteService {

	/**
	 * Saves the given configuration on the server.
	 * 
	 * @param language
	 *            The language to set
	 * @param experiment
	 *            Turns experiment mode on and off
	 * @param skip
	 *            Turns the skip button on and off
	 */
	void saveConfiguration(String language, boolean experiment, boolean skip);

}
