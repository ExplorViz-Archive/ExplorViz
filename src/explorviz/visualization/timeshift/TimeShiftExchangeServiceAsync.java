package explorviz.visualization.timeshift;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TimeShiftExchangeServiceAsync {
	void getAvailableLandscapes(AsyncCallback<Map<Long, Long>> callback);
}
