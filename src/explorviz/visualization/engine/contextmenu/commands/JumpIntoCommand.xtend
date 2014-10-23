package explorviz.visualization.engine.contextmenu.commands

import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.main.SceneDrawer
import explorviz.visualization.interaction.Usertracking
import explorviz.visualization.experiment.Experiment

class JumpIntoCommand extends ApplicationCommand {
      override execute() {
      	if(!Experiment::tutorial){
      		PopupService::hidePopupMenus()
       		Usertracking::trackApplicationDoubleClick(currentApp);
        	SceneDrawer::createObjectsFromApplication(currentApp, false)
      	}
      }
}