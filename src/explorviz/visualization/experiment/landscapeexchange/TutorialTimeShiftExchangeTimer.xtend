package explorviz.visualization.experiment.landscapeexchange

import com.google.gwt.user.client.Timer
import java.util.Map
import explorviz.visualization.experiment.landscapeexchange.TutorialTimeShiftExchangeServiceAsync
import explorviz.visualization.timeshift.TimeShiftCallback
import explorviz.visualization.timeshift.TimeShiftExchangeServiceAsync

class TutorialTimeShiftExchangeTimer extends Timer {
    val TutorialTimeShiftExchangeServiceAsync timeshiftExchangeService
    
    new (TimeShiftExchangeServiceAsync timeshiftExchangeService) {
        this.timeshiftExchangeService = timeshiftExchangeService as TutorialTimeShiftExchangeServiceAsync
    }
    
    override run() {
        timeshiftExchangeService.getAvailableLandscapes(new TimeShiftCallback<Map<Long, Long>>())
    }
}