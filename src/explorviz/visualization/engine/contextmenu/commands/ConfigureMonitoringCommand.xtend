package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring
import explorviz.visualization.model.ApplicationClientSide

class ConfigureMonitoringCommand implements Command {
	  var ApplicationClientSide currentApp
	
	  def setCurrentApp(ApplicationClientSide app) {
	  	currentApp = app
	  }
	
      override execute() {
        PopupService::hidePopupMenus()
        AdaptiveMonitoring::openDialog(currentApp)
      }
}