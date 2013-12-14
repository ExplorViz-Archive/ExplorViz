package explorviz.visualization.timeshift;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("timeshiftexchange")
public interface TimeShiftExchangeService extends RemoteService {
	Map<Long, Long> getAvailableLandscapes();
}
