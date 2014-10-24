package explorviz.visualization.main

import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.NodeCommand
import explorviz.visualization.engine.contextmenu.commands.ComponentCommand
import explorviz.visualization.engine.contextmenu.commands.ClazzCommand

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
	
	def static void setNodePopupEntryChecked(String label, boolean checked) {
		PopupService::setNodePopupEntryChecked(label, checked)
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
	
	def static void setApplicationPopupEntryChecked(String label, boolean checked) {
		PopupService::setApplicationPopupEntryChecked(label, checked)
	}

	def static void addComponentPopupEntry(String label, ComponentCommand command) {
		PopupService::addComponentPopupEntry(label, command)
	}

	def static void addComponentPopupSeperator() {
		PopupService::addComponentPopupSeperator
	}

	def static void clearComponentPopup() {
		PopupService::clearComponentPopup
	}

	def static void addClazzPopupEntry(String label, ClazzCommand command) {
		PopupService::addClazzPopupEntry(label, command)
	}

	def static void addClazzPopupSeperator() {
		PopupService::addClazzPopupSeperator
	}

	def static void clearClazzPopup() {
		PopupService::clearClazzPopup
	}
}
