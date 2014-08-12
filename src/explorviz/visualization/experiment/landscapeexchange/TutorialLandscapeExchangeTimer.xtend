package explorviz.visualization.experiment.landscapeexchange

import com.google.gwt.user.client.Timer
import explorviz.shared.model.Landscape
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync
import explorviz.visualization.landscapeexchange.LandscapeExchangeCallback

class TutorialLandscapeExchangeTimer extends Timer {
    val TutorialLandscapeExchangeServiceAsync landscapeExchangeService
    public static boolean loadedFirstLandscape = false
    
    new (LandscapeExchangeServiceAsync landscapeExchangeService) {
        this.landscapeExchangeService = landscapeExchangeService as TutorialLandscapeExchangeServiceAsync
    }
    
    override run() {
    	if(!Experiment::loadOtherLandscape){
    		if(!loadedFirstLandscape){
        		landscapeExchangeService.getCurrentLandscape(new LandscapeExchangeCallback<Landscape>(false))
        	}
        }else{
        	landscapeExchangeService.getCurrentLandscape2(new LandscapeExchangeCallback<Landscape>(false))
        }
    }
}