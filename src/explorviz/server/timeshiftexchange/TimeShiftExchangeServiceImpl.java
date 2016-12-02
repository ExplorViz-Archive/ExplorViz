package explorviz.server.timeshiftexchange;

import java.io.IOException;
import java.util.*;

import org.json.JSONException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.experiment.LandscapeReplayer;
import explorviz.server.landscapeexchange.LandscapeExchangeServiceImpl;
import explorviz.server.login.LoginServiceImpl;
import explorviz.server.main.Configuration;
import explorviz.server.util.JSONServiceImpl;
import explorviz.visualization.timeshift.TimeShiftExchangeService;

public class TimeShiftExchangeServiceImpl extends RemoteServiceServlet
		implements TimeShiftExchangeService {
	private static final long serialVersionUID = -3278027233811592148L;
	private long startTime = System.currentTimeMillis();

	private final JSONServiceImpl jsonService = new JSONServiceImpl();

	@Override
	public Map<Long, Long> getAvailableLandscapes() {

		final String currentUsername = LoginServiceImpl.getCurrentUsernameStatic();

		boolean isUserInExp = false;
		try {
			isUserInExp = jsonService.isUserInCurrentExperiment(currentUsername);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}

		if (Configuration.experiment && isUserInExp) {
			final LandscapeReplayer replayer = LandscapeReplayer.getReplayerForCurrentUser();
			return replayer.getAvailableLandscapesForTimeshift();
		} else {
			// return createDummy();
			return LandscapeExchangeServiceImpl.getModel().getAvailableLandscapes();
		}
	}

	// Generates dummy values with continuous date
	protected Map<Long, Long> createDummy() {
		final Map<Long, Long> result = new TreeMap<Long, Long>();

		result.put(startTime, (long) new Random().nextInt(300000));
		startTime += (20 * 1000);

		return result;
	}

}
