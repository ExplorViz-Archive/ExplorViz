package explorviz.visualization.landscapeexchange

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import com.google.gwt.user.client.Timer

class LandscapeExchangeManager {
    val static DATA_EXCHANGE_INTERVALL_MILLIS = 5000
    
    def static init() {
        val landscapeExchangeService = createAsyncService()
        
        LandscapeConverter::reset()
        
        val Timer timer = new LandscapeExchangeTimer(landscapeExchangeService)
        timer.run
        timer.scheduleRepeating(DATA_EXCHANGE_INTERVALL_MILLIS)
    }
    
    def static private createAsyncService() {
        val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
        val endpoint = landscapeExchangeService as ServiceDefTarget
        val moduleRelativeURL = GWT::getModuleBaseURL() + "landscapeexchange"
        endpoint.serviceEntryPoint = moduleRelativeURL
        
        landscapeExchangeService
    }
}