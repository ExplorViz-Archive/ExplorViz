package explorviz.visualization.engine.contextmenu.popupmenus

import explorviz.shared.model.Application
import explorviz.visualization.engine.contextmenu.PopupMenu
import explorviz.visualization.engine.contextmenu.commands.ConfigureMonitoringCommand
import explorviz.visualization.engine.contextmenu.commands.JumpIntoCommand

class ApplicationPopupMenu extends PopupMenu {
	var Application currentApp
	
	var JumpIntoCommand jumpInto
	var ConfigureMonitoringCommand configureMonitoring
	
	new() {
		super()
		jumpInto = new JumpIntoCommand()
		addNewEntry("Jump into", jumpInto)
		addSeperator()
//		addNewEntry("Stop", new DummyCommand())
//		addNewEntry("Restart", new DummyCommand())
//		addSeperator()
//		addNewEntry("Migrate", new DummyCommand())
//		addNewEntry("Replicate", new DummyCommand())
//		addSeperator()
		configureMonitoring = new ConfigureMonitoringCommand()
		addNewEntry("Configure monitoring", configureMonitoring)
	}

	override show(int x, int y, String name) {
		super.show(x, y, name)
	}
	
	def void setCurrentApplication(Application app) {
		currentApp = app
		
		jumpInto.setCurrentApp(currentApp)
		configureMonitoring.setCurrentApp(currentApp)
	}
	
}
