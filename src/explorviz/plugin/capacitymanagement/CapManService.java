package explorviz.plugin.capacitymanagement;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.model.Landscape;

@RemoteServiceRelativePath("capman")
public interface CapManService extends RemoteService {
	void sendExecutionPlan(Landscape landscape);
}
