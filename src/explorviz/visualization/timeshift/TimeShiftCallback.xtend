package explorviz.visualization.timeshift

import com.google.gwt.user.client.rpc.AsyncCallback
import java.util.Map

class TimeShiftCallback<T> implements AsyncCallback<T> {
	
	override onFailure(Throwable caught) {
	}
	
	override onSuccess(T result) {
		val resultMap = result as Map<Long, Long>
		TimeShiftJS.updateTimeshiftChart(resultMap)
	}
}