package explorviz.visualization.experiment.landscapeexchange;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.visualization.timeshift.TimeShiftExchangeService;

@RemoteServiceRelativePath("tutorialtimeshiftexchange")
public interface TutorialTimeShiftExchangeService extends TimeShiftExchangeService {
	Map<Long, Long> getAvailableLandscapes();
}
