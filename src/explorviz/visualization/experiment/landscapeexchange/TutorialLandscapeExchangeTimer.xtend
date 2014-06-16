package explorviz.visualization.experiment.landscapeexchange

import com.google.gwt.user.client.Timer
import explorviz.shared.model.Landscape
import explorviz.visualization.landscapeexchange.LandscapeConverter
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.engine.Logging

class TutorialLandscapeExchangeTimer extends Timer {
    val TutorialLandscapeExchangeServiceAsync landscapeExchangeService
    
    new (TutorialLandscapeExchangeServiceAsync landscapeExchangeService) {
        this.landscapeExchangeService = landscapeExchangeService
    }
    
    override run() {
    	if(!Experiment::loadOtherLandscape){
        	landscapeExchangeService.getCurrentLandscape(new LandscapeConverter<Landscape>)
        }else{
        	landscapeExchangeService.getCurrentLandscape2(new LandscapeConverter<Landscape>)
        	Logging.log("load other landscape")
        }
    }
}