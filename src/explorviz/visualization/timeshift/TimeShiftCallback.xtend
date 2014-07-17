package explorviz.visualization.timeshift

import com.google.gwt.user.client.rpc.AsyncCallback
import java.util.Map
import explorviz.visualization.main.ErrorDialog

class TimeShiftCallback<T> implements AsyncCallback<T> {
	
	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
	}
	
	override onSuccess(T result) {
		val resultMap = result as Map<Long, Long>
		TimeShiftJS.updateTimeshiftChart(resultMap)
	}
}