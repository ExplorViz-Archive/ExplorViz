package explorviz.visualization.engine.contextmenu.popupmenus

import com.google.gwt.user.client.Command
import explorviz.shared.model.Application
import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.ConfigureMonitoringCommand
import explorviz.visualization.engine.contextmenu.commands.JumpIntoCommand
import java.util.ArrayList
import java.util.List

class ApplicationPopupMenu extends PopupMenu {
	var List<ApplicationCommand> menuEntries
	
	override init() {
		menuEntries = new ArrayList<ApplicationCommand>()
		super.init()
		addNewEntry("Jump into", new JumpIntoCommand())
		addSeperator()
		addNewEntry("Configure monitoring", new ConfigureMonitoringCommand())
	}
	
	override addNewEntry(String label, Command command) {
		if (command instanceof ApplicationCommand) {
			menuEntries.add(command as ApplicationCommand)
			super.addNewEntry(label, command)
		}
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentApplication(Application app) {
		for (entry : menuEntries) {
			entry.currentApp = app
		}
	}
	
}
