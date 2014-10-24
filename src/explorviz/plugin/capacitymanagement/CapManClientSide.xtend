package explorviz.plugin.capacitymanagement

import explorviz.plugin.interfaces.IPluginClientSide
import explorviz.plugin.main.Perspective
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.NodeCommand
import explorviz.visualization.main.PluginManagerClientSide
import explorviz.shared.model.Node
import explorviz.shared.model.Application

class CapManClientSide implements IPluginClientSide {
	public static String TERMINATE_STRING = "Terminate"
	public static String RESTART_STRING = "Restart"
	public static String START_NEW_NODE_STRING = "Start new instance of same type"
	public static String STOP_STRING = "Stop"
	public static String MIGRATE_STRING = "Migrate"
	public static String REPLICATE_STRING = "Replicate"
	
	override switchedToPerspective(Perspective perspective) {
		if (perspective == Perspective::PLANNING) {
			PluginManagerClientSide::addNodePopupSeperator
			PluginManagerClientSide::addNodePopupEntry(TERMINATE_STRING, new TerminateNodeCommand())
			PluginManagerClientSide::addNodePopupEntry(RESTART_STRING, new RestartNodeCommand())
			PluginManagerClientSide::addNodePopupSeperator
			PluginManagerClientSide::addNodePopupEntry(START_NEW_NODE_STRING,
				new StartNewInstanceNodeCommand())

			PluginManagerClientSide::addApplicationPopupSeperator
			PluginManagerClientSide::addApplicationPopupEntry(STOP_STRING, new StopApplicationCommand())
			PluginManagerClientSide::addApplicationPopupEntry(RESTART_STRING, new RestartApplicationCommand())
			PluginManagerClientSide::addApplicationPopupSeperator
			PluginManagerClientSide::addApplicationPopupEntry(MIGRATE_STRING, new MigrateApplicationCommand())
			PluginManagerClientSide::addApplicationPopupEntry(REPLICATE_STRING, new ReplicateApplicationCommand())

		}
	}
	
	override popupMenuOpenedOn(Node node) {
		PluginManagerClientSide::setNodePopupEntryChecked(CapManClientSide::TERMINATE_STRING, true)
		PluginManagerClientSide::setNodePopupEntryChecked(CapManClientSide::RESTART_STRING, true)
	}
	
	override popupMenuOpenedOn(Application app) {
		PluginManagerClientSide::setApplicationPopupEntryChecked(CapManClientSide::STOP_STRING, true)
		PluginManagerClientSide::setApplicationPopupEntryChecked(CapManClientSide::RESTART_STRING, true)
	}

}

class TerminateNodeCommand extends NodeCommand {
	override execute() {
		super.execute()
	}
}

class RestartNodeCommand extends NodeCommand {
	override execute() {
		super.execute()
	}
}

class StartNewInstanceNodeCommand extends NodeCommand {
	override execute() {
		super.execute()
	}
}

class StopApplicationCommand extends ApplicationCommand {
	override execute() {
		super.execute()
	}
}

class RestartApplicationCommand extends ApplicationCommand {
	override execute() {
		super.execute()
	}
}

class MigrateApplicationCommand extends ApplicationCommand {
	override execute() {
		super.execute()
	}
}

class ReplicateApplicationCommand extends ApplicationCommand {
	override execute() {
		super.execute()
	}
}
