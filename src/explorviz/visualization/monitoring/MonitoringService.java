package explorviz.visualization.monitoring;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("monitoring")
public interface MonitoringService extends RemoteService {
	public void sendRecordBundle(String recordBundle);
}
