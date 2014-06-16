package explorviz.visualization.experiment.landscapeexchange

import com.google.gwt.user.client.Timer
import explorviz.shared.model.Landscape
import explorviz.visualization.landscapeexchange.LandscapeConverter
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.engine.Logging
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync

class TutorialLandscapeExchangeTimer extends Timer {
    val TutorialLandscapeExchangeServiceAsync landscapeExchangeService
    
    new (LandscapeExchangeServiceAsync landscapeExchangeService) {
        this.landscapeExchangeService = landscapeExchangeService as TutorialLandscapeExchangeServiceAsync
    }
    
    override run() {
    	if(!Experiment::loadOtherLandscape){
        	landscapeExchangeService.getCurrentLandscape(new LandscapeConverter<Landscape>)
        	Logging.log("load normal landscape")
        }else{
        	landscapeExchangeService.getCurrentLandscape2(new LandscapeConverter<Landscape>)
        	Logging.log("load other landscape")
        }
    }
}