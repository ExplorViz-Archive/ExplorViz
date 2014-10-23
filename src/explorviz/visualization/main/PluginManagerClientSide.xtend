package explorviz.visualization.main

import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.NodeCommand

class PluginManagerClientSide {
	def static void addConfigurationOption(String name, String description) {
		
	}
	
	def static void addNodePopupEntry(String label, NodeCommand command) {
		PopupService::addNodePopupEntry(label, command)
	}
	
	def static void addNodePopupSeperator() {
		PopupService::addNodePopupSeperator
	}
	
	def static void clearNodePopup() {
		PopupService::clearNodePopup
	}
	
	def static void addApplicationPopupEntry(String label, ApplicationCommand command) {
		PopupService::addApplicationPopupEntry(label, command)
	}
	
	def static void addApplicationPopupSeperator() {
		PopupService::addApplicationPopupSeperator
	}
	
	def static void clearApplicationPopup() {
		PopupService::clearApplicationPopup
	}
}