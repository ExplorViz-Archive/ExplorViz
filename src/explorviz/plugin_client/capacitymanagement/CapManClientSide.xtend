package explorviz.plugin_client.capacitymanagement

import explorviz.plugin_client.attributes.IPluginKeys
import explorviz.plugin_client.interfaces.IPluginClientSide
import explorviz.plugin_client.main.Perspective
import explorviz.shared.model.Application
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.shared.model.helper.GenericModelElement
import explorviz.visualization.engine.contextmenu.commands.ApplicationCommand
import explorviz.visualization.engine.contextmenu.commands.NodeCommand
import explorviz.visualization.main.PluginManagerClientSide
import explorviz.visualization.main.ExplorViz
import explorviz.plugin_client.capacitymanagement.CapManStates

class CapManClientSide implements IPluginClientSide {
	public static String TERMINATE_STRING = "Terminate"
	public static String RESTART_STRING = "Restart"
	public static String STOP_STRING = "Stop"
	public static String MIGRATE_STRING = "Migrate"
	public static String REPLICATE_STRING = "Replicate"
	
	public String oldPlanId = ""

	override switchedToPerspective(Perspective perspective) {
		if (perspective == Perspective::PLANNING) {
			PluginManagerClientSide::addNodePopupEntry(TERMINATE_STRING, new TerminateNodeCommand())
			PluginManagerClientSide::addNodePopupEntry(RESTART_STRING, new RestartNodeCommand())
			PluginManagerClientSide::addNodePopupSeperator
			PluginManagerClientSide::addNodePopupEntry(REPLICATE_STRING, new ReplicateNodeCommand())
			
			PluginManagerClientSide::addApplicationPopupSeperator
			PluginManagerClientSide::addApplicationPopupEntry(STOP_STRING, new StopApplicationCommand())
			PluginManagerClientSide::addApplicationPopupEntry(RESTART_STRING, new RestartApplicationCommand())
			PluginManagerClientSide::addApplicationPopupSeperator
			PluginManagerClientSide::addApplicationPopupEntry(MIGRATE_STRING, new MigrateApplicationCommand())			
		}
	}

	override popupMenuOpenedOn(Node node) {
		PluginManagerClientSide::setNodePopupEntryChecked(CapManClientSide::TERMINATE_STRING,
			elementShouldBeTerminated(node))
		PluginManagerClientSide::setNodePopupEntryChecked(CapManClientSide::RESTART_STRING,
			elementShouldBeRestarted(node))
		PluginManagerClientSide::setNodePopupEntryChecked(CapManClientSide::REPLICATE_STRING,
			elementShouldBeReplicated(node))
	}
	
	override popupMenuOpenedOn(Application app) {
		PluginManagerClientSide::setApplicationPopupEntryChecked(CapManClientSide::STOP_STRING,
			elementShouldBeTerminated(app))
		PluginManagerClientSide::setApplicationPopupEntryChecked(CapManClientSide::RESTART_STRING,
			elementShouldBeRestarted(app))
		//TODO implement migration
		//Inserted for migration
		PluginManagerClientSide::setApplicationPopupEntryChecked(CapManClientSide::MIGRATE_STRING,
			elementShouldBeMigrated(app))
	}
	
	//Get and set CapManStates.
	def static boolean elementShouldBeTerminated(GenericModelElement element) {
		if (!element.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) return false

		val state = element.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
		state == CapManStates::TERMINATE
	}

	def static void setElementShouldBeTerminated(GenericModelElement element, boolean value) {
		if (value) {
			setCapManState(element, CapManStates::TERMINATE)
		} else {
			setCapManState(element, CapManStates::NONE)
		}
	}
	
	def static boolean elementShouldBeRestarted(GenericModelElement element) {
		if (!element.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) return false

		val state = element.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
		state == CapManStates::RESTART
	}

	def static void setElementShouldBeRestarted(GenericModelElement element, boolean value) {
		if (value) {
			setCapManState(element, CapManStates::RESTART)
		} else {
			setCapManState(element, CapManStates::NONE)
		}
	}
	def static boolean elementShouldBeReplicated(GenericModelElement element) {
		if (!element.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) return false

		val state = element.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
		state == CapManStates::REPLICATE
	}

	def static void setElementShouldBeReplicated(GenericModelElement element, boolean value) {
		if (value) {
			setCapManState(element, CapManStates::REPLICATE)
		} else {
			setCapManState(element, CapManStates::NONE)
		}
	}
	
	//Inserted for migration
	def static boolean elementShouldBeMigrated(GenericModelElement element) {
		if (!element.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) return false

		val state = element.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
		state == CapManStates::MIGRATE
	}
	def static void setElementShouldBeMigrated(GenericModelElement element, boolean value) {
		if (value) {
			setCapManState(element, CapManStates::MIGRATE)
		} else {
			setCapManState(element, CapManStates::NONE)
		}
	}

	def static setCapManState(GenericModelElement element, CapManStates state) {
		element.putGenericData(IPluginKeys::CAPMAN_STATE, state)
	}

/**
	 * @author jgi, dtj If a new plan is available, show it.
	 * @param landscape 
	 * 			New landscape to be received.
	 */
	override newLandscapeReceived(Landscape landscape) {
		val suggestionAvailable = landscape.isGenericDataPresent(IPluginKeys::CAPMAN_NEW_PLAN_ID)
		if (suggestionAvailable) {
			// only show once
			val newPlanId = landscape.getGenericStringData(IPluginKeys::CAPMAN_NEW_PLAN_ID)
			if (newPlanId.equalsIgnoreCase(oldPlanId)) {
				return;
			}
			
			oldPlanId = newPlanId
			
			var warningText = landscape.getGenericStringData(IPluginKeys::CAPMAN_WARNING_TEXT)
			var counterMeasureText = landscape.getGenericStringData(IPluginKeys::CAPMAN_COUNTERMEASURE_TEXT)
			var consequenceText = landscape.getGenericStringData(IPluginKeys::CAPMAN_CONSEQUENCE_TEXT)
			if (warningText != null && consequenceText != null && counterMeasureText != null) {
			CapManClientSideJS::openPlanExecutionQuestionDialog(
				warningText,
				counterMeasureText,
				consequenceText)
				}
		}
	}
	
//Available GUI options.
	def static void conductOkAction() {
		ExplorViz::instance.executeCapManPlanning
	}

	def static void conductManualRefinementAction() {
		ExplorViz::instance.switchToPlanningPerspective
	}

	def static void conductCancelAction() {
		// empty
	}
}

//Possible commands to be executed.
class TerminateNodeCommand extends NodeCommand {
	override execute() {
		CapManClientSide::setElementShouldBeTerminated(currentNode,
			!CapManClientSide::elementShouldBeTerminated(currentNode))
		super.execute()
	}
}

class RestartNodeCommand extends NodeCommand {
	override execute() {
		CapManClientSide::setElementShouldBeRestarted(currentNode,
			!CapManClientSide::elementShouldBeRestarted(currentNode))
		super.execute()
	}
}

class ReplicateNodeCommand extends NodeCommand {
	override execute() {
		CapManClientSide::setElementShouldBeReplicated(currentNode,
			!CapManClientSide::elementShouldBeReplicated(currentNode))
		super.execute()
	}
}

class StopApplicationCommand extends ApplicationCommand {
	override execute() {
		CapManClientSide::setElementShouldBeTerminated(currentApp,
			!CapManClientSide::elementShouldBeTerminated(currentApp))
		super.execute()
	}
}

class RestartApplicationCommand extends ApplicationCommand {
	override execute() {
		CapManClientSide::setElementShouldBeRestarted(currentApp,
			!CapManClientSide::elementShouldBeRestarted(currentApp))
		super.execute()
	}
}

//Inserted for Migration
class MigrateApplicationCommand extends ApplicationCommand {
	override execute() {
		//TODO implement method
		CapManClientSide::setElementShouldBeMigrated(currentApp,
			!CapManClientSide::elementShouldBeMigrated(currentApp))
		super.execute()
	}
}
