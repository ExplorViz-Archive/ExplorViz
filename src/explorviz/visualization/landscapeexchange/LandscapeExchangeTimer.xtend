package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.Timer
import explorviz.shared.model.Landscape

class LandscapeExchangeTimer extends Timer {
    val LandscapeExchangeServiceAsync landscapeExchangeService
    
    new (LandscapeExchangeServiceAsync landscapeExchangeService) {
        this.landscapeExchangeService = landscapeExchangeService
    }
    
    override run() {
        landscapeExchangeService.getCurrentLandscape(new LandscapeExchangeCallback<Landscape>)
    }
}