package explorviz.plugin_client.capacitymanagement;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.model.Landscape;

@RemoteServiceRelativePath("capman")
public interface CapManService extends RemoteService {
	/**
	 * Sends execution plan and landscape that should be changed
	 *
	 * @param landscape
	 *            software landscape
	 */
	void sendExecutionPlan(Landscape landscape);

	void cancelButton(Landscape landscape);
}
