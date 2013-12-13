package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService

class DummyCommand implements Command {
      override execute() {
        PopupService::hidePopupMenus()
      }
}