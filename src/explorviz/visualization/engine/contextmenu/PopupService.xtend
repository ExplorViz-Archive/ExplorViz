package explorviz.visualization.engine.contextmenu

import explorviz.shared.model.Application
import explorviz.shared.model.Clazz
import explorviz.shared.model.Component
import explorviz.shared.model.Node
import explorviz.shared.model.System
import explorviz.shared.model.helper.CommunicationTileAccumulator
import explorviz.visualization.engine.contextmenu.popupmenus.ApplicationPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.ClazzPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.ComponentPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.NodePopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.modeling.ModelingApplicationPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.modeling.ModelingCommunicationPopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.modeling.ModelingNodePopupMenu
import explorviz.visualization.engine.contextmenu.popupmenus.modeling.ModelingSystemPopupMenu

class PopupService {
	static val nodePopupMenu = new NodePopupMenu()
	static val applicationPopupMenu = new ApplicationPopupMenu()
	static val componentPopupMenu = new ComponentPopupMenu()
	static val clazzPopupMenu = new ClazzPopupMenu()

	static val modelingSystemPopupMenu = new ModelingSystemPopupMenu()
	static val modelingNodePopupMenu = new ModelingNodePopupMenu()
	static val modelingApplicationPopupMenu = new ModelingApplicationPopupMenu()
	static val modelingCommunicationPopupMenu = new ModelingCommunicationPopupMenu()

	def static hidePopupMenus() {
		applicationPopupMenu.hide()
		nodePopupMenu.hide()
		componentPopupMenu.hide()
		clazzPopupMenu.hide()

		modelingSystemPopupMenu.hide()
		modelingNodePopupMenu.hide()
		modelingApplicationPopupMenu.hide()
		modelingCommunicationPopupMenu.hide()
	}

	def static showNodePopupMenu(int x, int y, Node node) {
		nodePopupMenu.show(x, y, node.ipAddress + " (node)")
	}

	def static showApplicationPopupMenu(int x, int y, Application app) {
		applicationPopupMenu.setCurrentApplication(app)
		applicationPopupMenu.show(x, y, app.name + " (application)")
	}

	def static showComponentPopupMenu(int x, int y, Component compo) {
		//		componentPopupMenu.show(x, y, compo.name)
	}

	def static showClazzPopupMenu(int x, int y, Clazz clazz) {
		clazzPopupMenu.setCurrentClazz(clazz)
		clazzPopupMenu.show(x, y, clazz.name + " (class)")
	}

	def static showModelingSystemPopupMenu(int x, int y, System system) {
		modelingSystemPopupMenu.currentSystem = system
		modelingSystemPopupMenu.show(x, y, system.name + " (system)")
	}

	def static showModelingNodePopupMenu(int x, int y, Node node) {
		modelingNodePopupMenu.currentNode = node
		modelingNodePopupMenu.show(x, y, node.name + " (node)")
	}

	def static showModelingApplicationPopupMenu(int x, int y, Application app) {
		modelingApplicationPopupMenu.currentApplication = app
		modelingApplicationPopupMenu.show(x, y, app.name + " (application)")
	}

	def static showModelingCommunicationPopupMenu(int x, int y, CommunicationTileAccumulator commu) {
		modelingCommunicationPopupMenu.currentCommunication = commu
		modelingCommunicationPopupMenu.show(x, y, "(communication)")
	}

}
