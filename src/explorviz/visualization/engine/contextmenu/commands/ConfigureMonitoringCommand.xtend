package explorviz.visualization.engine.contextmenu.commands

import explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.experiment.Experiment

class ConfigureMonitoringCommand extends ApplicationCommand {
      override execute() {
      	if(!Experiment::tutorial){
      		PopupService::hidePopupMenus()
        	AdaptiveMonitoring::openDialog(currentApp)
      	}
      }
}