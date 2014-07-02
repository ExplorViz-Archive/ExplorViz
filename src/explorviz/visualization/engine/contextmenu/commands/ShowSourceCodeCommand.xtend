package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.shared.model.Clazz
import explorviz.visualization.codeviewer.CodeViewer

class ShowSourceCodeCommand implements Command {
	  var Clazz currentClazz
	
	  def setCurrentClazz(Clazz clazz) {
	  	currentClazz = clazz
	  }
	
      override execute() {
        PopupService::hidePopupMenus()
        
        CodeViewer::openDialog(currentClazz.parent.belongingApplication.name);
        
        var filePath = currentClazz.fullQualifiedName
        filePath = filePath.substring(0, filePath.lastIndexOf(".")).replaceAll("\\.", "/")
        
        CodeViewer::getCode(filePath, currentClazz.name + ".java");
      }
}