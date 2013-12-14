package explorviz.visualization.timeshift

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import com.google.gwt.user.client.Timer

class TimeShiftExchangeManager {
    val static DATA_EXCHANGE_INTERVALL_MILLIS = 10000
    
    def static init() {
    	TimeShiftJS.init()
    	
        val timeshiftExchangeService = createAsyncService()
        
        val Timer timer = new TimeShiftExchangeTimer(timeshiftExchangeService)
        timer.run
        timer.scheduleRepeating(DATA_EXCHANGE_INTERVALL_MILLIS)
    }
    
    def static private createAsyncService() {
        val TimeShiftExchangeServiceAsync timeshiftExchangeService = GWT::create(typeof(TimeShiftExchangeService))
        val endpoint = timeshiftExchangeService as ServiceDefTarget
        val moduleRelativeURL = GWT::getModuleBaseURL() + "timeshiftexchange"
        endpoint.serviceEntryPoint = moduleRelativeURL
        
        timeshiftExchangeService
    }
}