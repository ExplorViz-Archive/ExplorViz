package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.shared.model.Application
import explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring
import explorviz.visualization.engine.contextmenu.PopupService

class ConfigureMonitoringCommand implements Command {
	  var Application currentApp
	
	  def setCurrentApp(Application app) {
	  	currentApp = app
	  }
	
      override execute() {
        PopupService::hidePopupMenus()
        AdaptiveMonitoring::openDialog(currentApp)
      }
}