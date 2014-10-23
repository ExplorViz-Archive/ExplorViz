package explorviz.plugin.capacitymanagement

import explorviz.visualization.main.PluginManagerClientSide
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.NodeCommand

class CapManClientSide  {

	new() {
		PluginManagerClientSide::addNodePopupSeperator
		PluginManagerClientSide::addNodePopupEntry("Terminate", new TerminateNodeCommand())
		PluginManagerClientSide::addNodePopupEntry("Restart", new RestartNodeCommand())
		PluginManagerClientSide::addNodePopupSeperator
		PluginManagerClientSide::addNodePopupEntry("Start new instance of same type", new StartNewInstanceNodeCommand())
		
		PluginManagerClientSide::addApplicationPopupSeperator
		PluginManagerClientSide::addApplicationPopupEntry("Stop", new StopApplicationCommand())
		PluginManagerClientSide::addApplicationPopupEntry("Restart", new RestartApplicationCommand())
		PluginManagerClientSide::addApplicationPopupSeperator
		PluginManagerClientSide::addApplicationPopupEntry("Migrate", new MigrateApplicationCommand())
		PluginManagerClientSide::addApplicationPopupEntry("Replicate", new ReplicateApplicationCommand())
	}
}

class TerminateNodeCommand extends NodeCommand {
	override execute() {
	}
}

class RestartNodeCommand extends NodeCommand {
	override execute() {
	}
}

class StartNewInstanceNodeCommand extends NodeCommand {
	override execute() {
	}
}

class StopApplicationCommand extends ApplicationCommand {
	override execute() {
	}
}

class RestartApplicationCommand extends ApplicationCommand {
	override execute() {
	}
}

class MigrateApplicationCommand extends ApplicationCommand {
	override execute() {
	}
}

class ReplicateApplicationCommand extends ApplicationCommand {
	override execute() {
	}
}
