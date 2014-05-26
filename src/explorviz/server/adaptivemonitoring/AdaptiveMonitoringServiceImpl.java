package explorviz.server.adaptivemonitoring;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.shared.adaptivemonitoring.AdaptiveMonitoringPattern;
import explorviz.visualization.adaptivemonitoring.AdaptiveMonitoringService;

public class AdaptiveMonitoringServiceImpl extends RemoteServiceServlet implements
		AdaptiveMonitoringService {
	private static final long serialVersionUID = -6834991857485620181L;

	private final List<AdaptiveMonitoringPattern> patterns = new ArrayList<AdaptiveMonitoringPattern>();

	@Override
	public List<AdaptiveMonitoringPattern> getAdaptiveMonitoringPatterns() {
		return patterns;
	}

	@Override
	public boolean addPattern(final AdaptiveMonitoringPattern patternToAdd) {
		// TODO validate before addition
		patterns.add(patternToAdd);

		return true;
	}

	@Override
	public boolean removePattern(final AdaptiveMonitoringPattern patternToRemove) {
		AdaptiveMonitoringPattern patternFound = null;

		for (final AdaptiveMonitoringPattern pattern : patterns) {
			if (pattern.getPattern().equals(patternToRemove.getPattern())) {
				patternFound = pattern;
			}
		}

		if (patternFound != null) {
			return patterns.remove(patternFound);
		}

		return false;
	}
}
