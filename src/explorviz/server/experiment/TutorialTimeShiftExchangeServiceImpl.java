package explorviz.server.experiment;

import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.main.Configuration;
import explorviz.visualization.experiment.landscapeexchange.TutorialTimeShiftExchangeService;

public class TutorialTimeShiftExchangeServiceImpl extends RemoteServiceServlet implements
		TutorialTimeShiftExchangeService {
	private static final long serialVersionUID = -3278027233811592148L;

	@Override
	public Map<Long, Long> getAvailableLandscapes() {
		// return
		// TutorialLandscapeExchangeServiceImpl.getModel().getAvailableLandscapes();
		return createTimeshiftGraph();
	}

	protected Map<Long, Long> createTimeshiftGraph() {

		final Map<Long, Long> result = new TreeMap<Long, Long>();

		// result.put(System.currentTimeMillis(), (long) 5400);
		result.put(Configuration.tutorialStart, (long) 5400);
		long time = Configuration.tutorialStart + (1 * 20 * 1000);
		for (int i = 1; time < System.currentTimeMillis(); i++) {
			time = Configuration.tutorialStart + (i * 20 * 1000);
			if (!Configuration.secondLandscape || (time < Configuration.secondLandscapeTime)) {
				result.put(Configuration.tutorialStart + (i * 20 * 1000), (long) 5400);
			} else {
				result.put(Configuration.tutorialStart + (i * 20 * 1000), (long) 6000);
			}
		}

		return result;
	}
}
