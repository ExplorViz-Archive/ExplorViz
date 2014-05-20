package explorviz.visualization.experiment.landscapeexchange

import com.google.gwt.user.client.Timer
import explorviz.shared.model.Landscape
import explorviz.visualization.landscapeexchange.LandscapeConverter

class TutorialLandscapeExchangeTimer extends Timer {
    val TutorialLandscapeExchangeServiceAsync landscapeExchangeService
    
    new (TutorialLandscapeExchangeServiceAsync landscapeExchangeService) {
        this.landscapeExchangeService = landscapeExchangeService
    }
    
    override run() {
        landscapeExchangeService.getCurrentLandscape(new LandscapeConverter<Landscape>)
    }
}