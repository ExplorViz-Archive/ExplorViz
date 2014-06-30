package explorviz.visualization.timeshift

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.Timer
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.landscapeexchange.TutorialTimeShiftExchangeTimer
import explorviz.visualization.experiment.landscapeexchange.TutorialTimeShiftExchangeService

class TimeShiftExchangeManager {
    val static DATA_EXCHANGE_INTERVALL_MILLIS = 10000
    static var Timer timer
    
    def static init() {
    	TimeShiftJS.init()
    	
        val timeshiftExchangeService = createAsyncService()
        
        if(timer!=null){
			timer.cancel()
		}
		if(Experiment::tutorial){
			timer = new TutorialTimeShiftExchangeTimer(timeshiftExchangeService)
		}else{
        	timer = new TimeShiftExchangeTimer(timeshiftExchangeService)
       	}
        timer.run
        timer.scheduleRepeating(DATA_EXCHANGE_INTERVALL_MILLIS)
    }
    
    def static cancel() {
    	timer.cancel
    }
    
    def static private createAsyncService() {
    	if(Experiment::tutorial){
    		val TimeShiftExchangeServiceAsync timeshiftExchangeService = GWT::create(typeof(TutorialTimeShiftExchangeService))
	        val endpoint = timeshiftExchangeService as ServiceDefTarget
	        val moduleRelativeURL = GWT::getModuleBaseURL() + "tutorialtimeshiftexchange"
	        endpoint.serviceEntryPoint = moduleRelativeURL
	        
	        return timeshiftExchangeService
    	}else{
	        val TimeShiftExchangeServiceAsync timeshiftExchangeService = GWT::create(typeof(TimeShiftExchangeService))
	        val endpoint = timeshiftExchangeService as ServiceDefTarget
	        val moduleRelativeURL = GWT::getModuleBaseURL() + "timeshiftexchange"
	        endpoint.serviceEntryPoint = moduleRelativeURL
	        
	        return timeshiftExchangeService
        }
    }
}