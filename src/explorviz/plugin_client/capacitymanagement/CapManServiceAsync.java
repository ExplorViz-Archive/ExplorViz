package explorviz.plugin_client.capacitymanagement;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.model.Landscape;

public interface CapManServiceAsync {
	void sendExecutionPlan(Landscape landscape, AsyncCallback<Void> callback);
}
