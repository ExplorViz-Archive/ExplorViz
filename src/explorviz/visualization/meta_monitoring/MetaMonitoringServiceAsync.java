package explorviz.visualization.meta_monitoring;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MetaMonitoringServiceAsync {
	void sendRecordBundle(String recordBundle, AsyncCallback<Void> callback);
}
