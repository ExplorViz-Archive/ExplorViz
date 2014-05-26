package explorviz.visualization.adaptivemonitoring;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.adaptivemonitoring.AdaptiveMonitoringPattern;

@RemoteServiceRelativePath("adaptivemonitoring")
public interface AdaptiveMonitoringService extends RemoteService {
	public List<AdaptiveMonitoringPattern> getAdaptiveMonitoringPatterns();

	public boolean addPattern(AdaptiveMonitoringPattern pattern);

	public boolean removePattern(AdaptiveMonitoringPattern pattern);
}
