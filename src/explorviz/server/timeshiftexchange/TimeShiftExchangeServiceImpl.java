package explorviz.server.timeshiftexchange;

import java.util.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.experiment.LandscapeReplayer;
import explorviz.server.main.Configuration;
import explorviz.visualization.timeshift.TimeShiftExchangeService;

public class TimeShiftExchangeServiceImpl extends RemoteServiceServlet
		implements TimeShiftExchangeService {
	private static final long serialVersionUID = -3278027233811592148L;

	@Override
	public Map<Long, Long> getAvailableLandscapes() {
		if (Configuration.experiment) {
			final LandscapeReplayer replayer = LandscapeReplayer.getReplayerForCurrentUser();
			return replayer.getAvailableLandscapesForTimeshift();
		} else {
			return createDummy();
			// return
			// LandscapeExchangeServiceImpl.getModel().getAvailableLandscapes();
		}
	}

	protected Map<Long, Long> createDummy() {
		final Map<Long, Long> result = new TreeMap<Long, Long>();

		result.put(System.currentTimeMillis(), (long) new Random().nextInt(300000));

		for (int i = 1; i < 100; i++) {
			result.put(System.currentTimeMillis() + (i * 20 * 1000),
					(long) new Random().nextInt(300000));
		}

		return result;
	}
}
