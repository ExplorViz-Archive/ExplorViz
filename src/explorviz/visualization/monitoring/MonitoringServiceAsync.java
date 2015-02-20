package explorviz.visualization.monitoring;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MonitoringServiceAsync {
	void sendRecordBundle(String recordBundle, AsyncCallback<Void> callback);
}
