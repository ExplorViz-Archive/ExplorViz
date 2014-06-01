package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.interaction.Usertracking

class JumpIntoCommand implements Command {
	  var ApplicationClientSide currentApp
	
	  def setCurrentApp(ApplicationClientSide app) {
	  	currentApp = app
	  }
	
      override execute() {
        PopupService::hidePopupMenus()
        Usertracking::trackApplicationDoubleClick(currentApp);
        SceneDrawer::createObjectsFromApplication(currentApp, true)
      }
}