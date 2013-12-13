package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import com.google.gwt.user.client.Window
import explorviz.visualization.engine.contextmenu.PopupService

class ShowSourceCodeCommand implements Command {
      override execute() {
        PopupService::hidePopupMenus()
        Window::alert("Show me the source!");
      }
}