package explorviz.plugin.capacitymanagement

import explorviz.plugin.attributes.IPluginKeys
import explorviz.plugin.attributes.TreeMapLongDoubleIValue
import explorviz.plugin.interfaces.ICapacityManager

import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.plugin.capacitymanagement.cpu_utilization.IAverageCPUUtilizationReceiver
import java.util.Map
import explorviz.plugin.capacitymanagement.scaling_strategies.IScalingStrategy
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import explorviz.plugin.capacitymanagement.scaling_strategies.IScalingControl
import explorviz.plugin.capacitymanagement.configuration.CapManConfiguration
import explorviz.server.main.Configuration
import explorviz.plugin.capacitymanagement.execution.ExecutionOrganizer


class CapMan implements ICapacityManager, IAverageCPUUtilizationReceiver {
		private static final Logger LOG = LoggerFactory.getLogger(typeof(CapMan));
		private final IScalingStrategy strategy;

private final CapManConfiguration configuration;
private final ExecutionOrganizer organizer;


	new() {
		val settingsFile = "./META-INF/explorviz.capacity_manager.default.properties";
		configuration = new CapManConfiguration(settingsFile);
		organizer = new ExecutionOrganizer(configuration);
        try {           
       
		//LoadBalancersReader::readInLoadBalancers(settingsFile);
		//LoadBalancersFacade::reset();

        LOG.info("Capacity Manager started");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            System.exit(1);
        }
		PluginManagerServerSide::registerAsCapacityManager(this);
		val strategyClazz = Class
				.forName("explorviz.plugin.capacitymanagement.scaling_strategies."
						+ configuration.getScalingStrategy());
		// loads strategy to analyze nodes that is determined in the
		// configuration file
		strategy = ( strategyClazz.getConstructor(typeof(IScalingControl),
				typeof(CapManConfiguration))).newInstance(organizer, configuration) as IScalingStrategy;
	}

	override doCapacityManagement(Landscape landscape) {
		//TODO: distributor und reader war in CapacityManagementStarter initialisiert worden
		// wo initialisieren wir ihn nun? brauchen wir doch wieder einen Starter?
	/*	val distributor = new CPUUtilizationDistributor(
		configuration.getAverageCpuUtilizationTimeWindowInMillisecond(),landscape, this);		
		val reader = new CPUUtilizationTCPReader(configuration.getCpuUtilizationReaderListenerPort(),
				distributor);
		reader.start();
		*/
		
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
						
						// TODO update the current progress of restarting action
						application.putGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE, CapManExecutionStates::NONE)
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
								// TODO add to action list
								// dont modify the landscape here - only modify in doCapacityManagement
							}
						}
					}
				}
			}
		}
	}
	
	
		override newCPUUtilizationAverage(Map<Node, Double> averageCPUUtilizations) {
			if (!averageCPUUtilizations.isEmpty()) {
			strategy.analyze(averageCPUUtilizations);
		}
	}


												
												
	
}
