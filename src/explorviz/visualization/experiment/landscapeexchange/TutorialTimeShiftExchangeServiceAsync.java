package explorviz.visualization.experiment.landscapeexchange;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.visualization.timeshift.TimeShiftExchangeServiceAsync;

public interface TutorialTimeShiftExchangeServiceAsync extends TimeShiftExchangeServiceAsync {
	void getAvailableLandscapes(AsyncCallback<Map<Long, Long>> callback);
}
