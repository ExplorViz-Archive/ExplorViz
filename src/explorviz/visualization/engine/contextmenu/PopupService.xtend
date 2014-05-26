package explorviz.visualization.engine.contextmenu

import explorviz.visualization.model.NodeClientSide
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.ClazzClientSide

import explorviz.visualization.engine.contextmenu.popupmenus.NodePopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.ApplicationPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.ComponentPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.ClazzPopupMenu

class PopupService {
	static val nodePopupMenu = new NodePopupMenu()
	static val applicationPopupMenu = new ApplicationPopupMenu()
	static val componentPopupMenu = new ComponentPopupMenu()
	static val clazzPopupMenu = new ClazzPopupMenu()

	def static hidePopupMenus() {
		applicationPopupMenu.hide()
		nodePopupMenu.hide()
		componentPopupMenu.hide()
		clazzPopupMenu.hide()
	}

	def static showNodePopupMenu(int x, int y, NodeClientSide node) {
		nodePopupMenu.show(x, y, node.ipAddress + " (node)")
	}
	
	def static showApplicationPopupMenu(int x, int y, ApplicationClientSide app) {
		applicationPopupMenu.setCurrentApplication(app)
		applicationPopupMenu.show(x, y, app.name  + " (application)")
	}
	
	def static showComponentPopupMenu(int x, int y, ComponentClientSide compo) {
		componentPopupMenu.show(x, y, compo.name)
	}
	
	def static showClazzPopupMenu(int x, int y, ClazzClientSide clazz) {
		clazzPopupMenu.show(x, y, clazz.name  + " (class)")
	}

}
