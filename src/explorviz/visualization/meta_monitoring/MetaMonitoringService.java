package explorviz.visualization.meta_monitoring;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("metamonitoring")
public interface MetaMonitoringService extends RemoteService {
	public void sendRecordBundle(String recordBundle);
}
