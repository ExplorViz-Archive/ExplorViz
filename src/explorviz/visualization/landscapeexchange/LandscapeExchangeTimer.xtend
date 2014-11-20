package explorviz.visualization.landscapeexchange

import com.google.gwt.user.client.Timer
import explorviz.shared.model.Landscape

class LandscapeExchangeTimer extends Timer {
    val LandscapeExchangeServiceAsync landscapeExchangeService
    // TODO HACK
    public static var boolean alreadyExchanged = false
    
    new (LandscapeExchangeServiceAsync landscapeExchangeService) {
        this.landscapeExchangeService = landscapeExchangeService
    }
    
    override run() {
    	if (!alreadyExchanged)
        	landscapeExchangeService.getCurrentLandscape(new LandscapeExchangeCallback<Landscape>(false))
    }
}
