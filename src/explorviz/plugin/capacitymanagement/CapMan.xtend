package explorviz.plugin.capacitymanagement

import explorviz.plugin.attributes.IPluginKeys
import explorviz.plugin.attributes.TreeMapLongDoubleIValue
import explorviz.plugin.interfaces.ICapacityManager
import explorviz.server.main.Configuration
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node

class CapMan implements ICapacityManager {

	new() {
		PluginManagerServerSide::registerAsCapacityManager(this)
	}

	override doCapacityManagement(Landscape landscape) {
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					if (node.lastSeenTimestamp >= landscape.timestamp - Configuration::outputIntervalSeconds * 1000) {
						newCPUUtilizationReceived(node, node.cpuUtilization, node.lastSeenTimestamp)
					}
				}
			}
		}

		// TODO calculate if new node/application should be started or terminated
		
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_NEW_PLAN_ID, "Dummy1")
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_WARNING_TEXT,
//			"The software landscape violates its requirements for response times.")
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_COUNTERMEASURE_TEXT,
//			"It is suggested to start a new node of type 'm1.small' with the application 'Neo4J' on it.")
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_CONSEQUENCE_TEXT,
//			"After the change, the response time is improved and the operating costs increase by 5 Euro per hour.")

		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					node.putGenericData(IPluginKeys::CAPMAN_STATE, CapManStates::NONE)
					for (application : node.applications) {
						application.putGenericData(IPluginKeys::CAPMAN_STATE, CapManStates::NONE)
					}
				}
			}
		}
	}

	def void newCPUUtilizationReceived(Node node, double utilization, long timestamp) {
		var cpuUtilHistory = node.getGenericData(IPluginKeys::CAPMAN_CPU_UTIL_HISTORY) as TreeMapLongDoubleIValue
		if (cpuUtilHistory == null) {
			cpuUtilHistory = new TreeMapLongDoubleIValue()
		}

		// TODO delete old entries
		cpuUtilHistory.put(timestamp, utilization)

		node.putGenericData(IPluginKeys::CAPMAN_CPU_UTIL_HISTORY, cpuUtilHistory)
	}

	override receivedFinalCapacityAdaptationPlan(Landscape landscape) {
		println("Received capman plan at: " + landscape.timestamp)
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						if (application.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) {

							val state = application.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
							if (state == CapManStates::RESTART) {
								// TODO conduct restart...
							}
						}
					}
				}
			}
		}
	}

}
