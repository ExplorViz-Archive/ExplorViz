package explorviz.visualization.engine.contextmenu

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.Node
import explorviz.visualization.engine.contextmenu.popupmenus.ApplicationPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.ClazzPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.ComponentPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.NodePopupMenu
import com.google.gwt.user.client.Command
import explorviz.plugin_client.main.PluginManagementClientSide

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

	def static showNodePopupMenu(int x, int y, Node node) {
		PluginManagementClientSide::popupMenuOpenedOn(node)
		nodePopupMenu.setCurrentNode(node)
		nodePopupMenu.show(x, y, node.ipAddress + " (node)")
	}
	
	def static showApplicationPopupMenu(int x, int y, Application app) {
		PluginManagementClientSide::popupMenuOpenedOn(app)
		applicationPopupMenu.setCurrentApplication(app)
		applicationPopupMenu.show(x, y, app.name  + " (application)")
	}
	
	def static showComponentPopupMenu(int x, int y, Component compo) {
		componentPopupMenu.setCurrentComponent(compo)
		componentPopupMenu.show(x, y, compo.name)
	}
	
	def static showClazzPopupMenu(int x, int y, Clazz clazz) {
		clazzPopupMenu.setCurrentClazz(clazz)
		clazzPopupMenu.show(x, y, clazz.name  + " (class)")
	}

	def static void addNodePopupEntry(String label, Command command) {
		nodePopupMenu.addNewEntry(label, command)
	}
	
	def static void addNodePopupSeperator() {
		nodePopupMenu.addSeperator
	}
	
	def static void clearNodePopup() {
		nodePopupMenu.clear
	}
	
	def static void setNodePopupEntryChecked(String label, boolean checked) {
		nodePopupMenu.setEntryChecked(label, checked)
	}
	
	def static void addApplicationPopupEntry(String label, Command command) {
		applicationPopupMenu.addNewEntry(label, command)
	}
	
	def static void addApplicationPopupSeperator() {
		applicationPopupMenu.addSeperator
	}
	
	def static void clearApplicationPopup() {
		applicationPopupMenu.clear
	}
	
	def static void setApplicationPopupEntryChecked(String label, boolean checked) {
		applicationPopupMenu.setEntryChecked(label, checked)
	}
	
	def static void addComponentPopupEntry(String label, Command command) {
		componentPopupMenu.addNewEntry(label, command)
	}
	
	def static void addComponentPopupSeperator() {
		componentPopupMenu.addSeperator
	}
	
	def static void clearComponentPopup() {
		componentPopupMenu.clear
	}
	
	def static void addClazzPopupEntry(String label, Command command) {
		clazzPopupMenu.addNewEntry(label, command)
	}
	
	def static void addClazzPopupSeperator() {
		clazzPopupMenu.addSeperator
	}
	
	def static void clearClazzPopup() {
		clazzPopupMenu.clear
	}
}
