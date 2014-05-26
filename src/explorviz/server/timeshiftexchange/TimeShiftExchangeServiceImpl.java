package explorviz.server.timeshiftexchange;

import java.util.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.landscapeexchange.LandscapeExchangeServiceImpl;
import explorviz.visualization.timeshift.TimeShiftExchangeService;

public class TimeShiftExchangeServiceImpl extends RemoteServiceServlet implements
		TimeShiftExchangeService {
	private static final long serialVersionUID = -3278027233811592148L;

	@Override
	public Map<Long, Long> getAvailableLandscapes() {
		// return createDummy();
		return LandscapeExchangeServiceImpl.getModel().getAvailableLandscapes();
	}

	protected Map<Long, Long> createDummy() {
		final Map<Long, Long> result = new HashMap<Long, Long>();

		result.put(System.currentTimeMillis(), (long) new Random().nextInt(300000));

		for (int i = 1; i < 40; i++) {
			result.put(System.currentTimeMillis() + (i * 20 * 1000),
					(long) new Random().nextInt(300000));
		}

		return result;
	}
}
