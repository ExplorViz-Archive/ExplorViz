package explorviz.plugin.capacitymanagement

import explorviz.plugin.interfaces.IPluginClientSide
import explorviz.plugin.main.Perspective
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.NodeCommand
import explorviz.visualization.main.PluginManagerClientSide
import explorviz.shared.model.Node
import explorviz.shared.model.Application
import explorviz.shared.model.Landscape

class CapManClientSide implements IPluginClientSide {
	public static String TERMINATE_STRING = "Terminate"
	public static String RESTART_STRING = "Restart"
	public static String START_NEW_NODE_STRING = "Start new instance of same type"
	public static String STOP_STRING = "Stop"
	public static String MIGRATE_STRING = "Migrate"
	public static String REPLICATE_STRING = "Replicate"

	override switchedToPerspective(Perspective perspective) {
		if (perspective == Perspective::PLANNING) {
			openPlanExecutionQuestionDialog()
			PluginManagerClientSide::addNodePopupSeperator
			PluginManagerClientSide::addNodePopupEntry(TERMINATE_STRING, new TerminateNodeCommand())
			PluginManagerClientSide::addNodePopupEntry(RESTART_STRING, new RestartNodeCommand())
			PluginManagerClientSide::addNodePopupSeperator
			PluginManagerClientSide::addNodePopupEntry(START_NEW_NODE_STRING, new StartNewInstanceNodeCommand())

			PluginManagerClientSide::addApplicationPopupSeperator
			PluginManagerClientSide::addApplicationPopupEntry(STOP_STRING, new StopApplicationCommand())
			PluginManagerClientSide::addApplicationPopupEntry(RESTART_STRING, new RestartApplicationCommand())
			PluginManagerClientSide::addApplicationPopupSeperator
			PluginManagerClientSide::addApplicationPopupEntry(MIGRATE_STRING, new MigrateApplicationCommand())
			PluginManagerClientSide::addApplicationPopupEntry(REPLICATE_STRING, new ReplicateApplicationCommand())
		}
	}

	override popupMenuOpenedOn(Node node) {
		PluginManagerClientSide::setNodePopupEntryChecked(CapManClientSide::TERMINATE_STRING,
			nodeShouldBeTerminated(node))
		PluginManagerClientSide::setNodePopupEntryChecked(CapManClientSide::RESTART_STRING, nodeShouldBeRestarted(node))
	}

	def static boolean nodeShouldBeTerminated(Node node) {
		false
	}

	def static void setNodeShouldBeTerminated(Node node, boolean value) {
		// TODO
	}

	def static boolean nodeShouldBeRestarted(Node node) {
		false
	}

	def static void setNodeShouldBeRestarted(Node node, boolean value) {
		// TODO
	}

	override popupMenuOpenedOn(Application app) {
		PluginManagerClientSide::setApplicationPopupEntryChecked(CapManClientSide::STOP_STRING,
			applicationShouldBeStopped(app))
		PluginManagerClientSide::setApplicationPopupEntryChecked(CapManClientSide::RESTART_STRING,
			applicationShouldBeRestarted(app))
	}

	def static boolean applicationShouldBeStopped(Application app) {
		false
	}

	def static void setApplicationShouldBeStopped(Application app, boolean value) {
		// TODO
	}

	def static boolean applicationShouldBeRestarted(Application app) {
		false

	}

	def static void setApplicationShouldBeRestarted(Application app, boolean value) {
		// TODO
	}

	def static void openPlanExecutionQuestionDialog() {
		CapManClientSideJS::openPlanExecutionQuestionDialog(
			"The software landscape violates its requirements for response times.",
			"It is suggested to start a new node of type 'm1.small' with the application 'Neo4J' on it.",
			"After the change, the response time is improved and the operating costs increase by 5 Euro per hour.")
	}
	
	override newLandscapeReceived(Landscape landscape) {
		// TODO ?
	}
	
}

class TerminateNodeCommand extends NodeCommand {
	override execute() {
		CapManClientSide::setNodeShouldBeTerminated(currentNode, !CapManClientSide::nodeShouldBeTerminated(currentNode))
		CapManClientSide::setNodeShouldBeRestarted(currentNode, false)
		super.execute()
	}
}

class RestartNodeCommand extends NodeCommand {
	override execute() {
		CapManClientSide::setNodeShouldBeRestarted(currentNode, !CapManClientSide::nodeShouldBeRestarted(currentNode))
		CapManClientSide::setNodeShouldBeTerminated(currentNode, false)
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
		CapManClientSide::setApplicationShouldBeStopped(currentApp,
			!CapManClientSide::applicationShouldBeStopped(currentApp))
		CapManClientSide::setApplicationShouldBeRestarted(currentApp, false)
		super.execute()
	}
}

class RestartApplicationCommand extends ApplicationCommand {
	override execute() {
		CapManClientSide::setApplicationShouldBeRestarted(currentApp,
			!CapManClientSide::applicationShouldBeRestarted(currentApp))
		CapManClientSide::setApplicationShouldBeStopped(currentApp, false)
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
