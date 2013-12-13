package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.Timer
import explorviz.shared.model.Landscape
import explorviz.visualization.landscapeexchange.LandscapeConverter

class LandscapeExchangeTimer extends Timer {
    val LandscapeExchangeServiceAsync landscapeExchangeService
    
    new (LandscapeExchangeServiceAsync landscapeExchangeService) {
        this.landscapeExchangeService = landscapeExchangeService
    }
    
    override run() {
        landscapeExchangeService.getLandscape(new LandscapeConverter<Landscape>)
    }
}