package explorviz.server.timeshiftexchange;

import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.landscapeexchange.LandscapeExchangeServiceImpl;
import explorviz.visualization.timeshift.TimeShiftExchangeService;

public class TimeShiftExchangeServiceImpl extends RemoteServiceServlet implements
		TimeShiftExchangeService {
	private static final long serialVersionUID = -3278027233811592148L;

	@Override
	public Map<Long, Long> getAvailableLandscapes() {
		return LandscapeExchangeServiceImpl.getModel().getAvailableLandscapes();
	}
}
