package explorviz.visualization.timeshift

import com.google.gwt.user.client.Timer
import java.util.Map

class TimeShiftExchangeTimer extends Timer {
    val TimeShiftExchangeServiceAsync timeshiftExchangeService
    
    new (TimeShiftExchangeServiceAsync timeshiftExchangeService) {
        this.timeshiftExchangeService = timeshiftExchangeService
    }
    
    override run() {
        timeshiftExchangeService.getAvailableLandscapes(new TimeShiftCallback<Map<Long, Long>>())
    }
}