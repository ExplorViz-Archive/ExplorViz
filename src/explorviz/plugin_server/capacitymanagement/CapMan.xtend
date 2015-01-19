package explorviz.plugin_server.capacitymanagement

import explorviz.plugin_client.attributes.IPluginKeys
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue
import explorviz.plugin_server.interfaces.ICapacityManager

import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
import explorviz.plugin_server.capacitymanagement.cpu_utilization.IAverageCPUUtilizationReceiver
import java.util.Map
import explorviz.plugin_server.capacitymanagement.scaling_strategies.IScalingStrategy
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration
import explorviz.plugin_client.capacitymanagement.execution.ExecutionOrganizer
import java.util.Set
import explorviz.plugin_client.capacitymanagement.CapManClientSide
import explorviz.plugin_client.capacitymanagement.CapManStates
import explorviz.plugin_client.capacitymanagement.CapManExecutionStates
import java.lang.reflect.Array
import explorviz.shared.model.Application
import java.util.ArrayList
import java.util.List

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
				.forName("explorviz.plugin_server.capacitymanagement.scaling_strategies."
						+ configuration.getScalingStrategy());
		// loads strategy to analyze nodes that is determined in the
		// configuration file
		strategy = ( strategyClazz.getConstructor(typeof(CapManConfiguration))).newInstance(configuration) as IScalingStrategy;
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
		//Save the largest Root Cause Rating
		var double maxRootCauseRating = 0
		//Initialize CapManStatus.
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					node.putGenericData(IPluginKeys::CAPMAN_STATE, CapManStates::NONE)
					for (application : node.applications) {
						if (application.isGenericDataPresent(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)) {
							if(application.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY) > maxRootCauseRating){
								//Update maximum root cause rating.
								maxRootCauseRating = application.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)
							}
						}
						
						application.putGenericData(IPluginKeys::CAPMAN_STATE, CapManStates::NONE)
						
						// TODO update the current progress of restarting action
						application.putGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE, CapManExecutionStates::NONE)
					}
				}
			}
		}
		
			
		//Get RootCauseMarkings.
//		for (system : landscape.systems) {
//			for (nodeGroup : system.nodeGroups) {
//				for (node : nodeGroup.nodes) {
//					for (application : node.applications) {
//						//TODO Read RootCauseMarkings
//					}
//				}
//			}
//		}
		
		// TODO calculate if new node/application should be started or terminated
	
	//TODO createExecutionPlan
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_NEW_PLAN_ID, "Execution Plan")
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_WARNING_TEXT,
//			"The software landscape violates its requirements for response times.")
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_COUNTERMEASURE_TEXT,
//			"It is suggested to start a new node of type 'm1.small' with the application 'Neo4J' on it.")
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_CONSEQUENCE_TEXT,
//			"After the change, the response time is improved and the operating costs increase by 5 Euro per hour.")
	
	}
	//Collect all the Applications that are down to 10% below the maximum rating.
	def List<Application> getApplicationsToBeAnalysed(Landscape landscape, double rootCauseRating) {
		var List<Application> applicationGroup = new ArrayList<Application>()
		
		
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						if (application.isGenericDataPresent(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)) {
							if(application.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY) >= (rootCauseRating - 0.1) ){
								applicationGroup.add(application)
							}
						}
						
						//TODO Read RootCauseMarkings
					}
				}
			}
		}
		return applicationGroup
	} 
	
	
	
	
	//ExecutionPlan setting CapManStates in Nodes.
	//Display UserDialog.
	def void createExecutionPlan(Landscape landscape, Map<Node, Boolean> planMap) {
		var String warningText = ""
		var String counterMeasureText = ""
		var String consequenceText = ""
		
		for (Map.Entry<Node, Boolean> mapEntries : planMap.entrySet()) {
			if (mapEntries.getValue()) {
				CapManClientSide::setElementShouldStartNewInstance(mapEntries.getKey(), true)
				warningText += "Node: " + mapEntries.getKey().getDisplayName() + "has a threshold above "
				 + configuration.scalingHighCpuThreshold * 100 + "%!\n"
				 counterMeasureText += "It is suggested to start a new node for " + mapEntries.getKey().getDisplayName() + "."
				 consequenceText += "After the change, the response time is improved and the operating costs increase by 5 Euro per hour."
			} else {
				CapManClientSide::setElementShouldBeTerminated(mapEntries.getKey(), true)
				warningText += "Node: " + mapEntries.getKey().getDisplayName() + "has a threshold below "
				 + configuration.scalingLowCpuThreshold * 100 + "%!\n"
				 counterMeasureText += "It is suggested to terminate Node " + mapEntries.getKey().getDisplayName() + "."
				 consequenceText += "After the change, the operating costs decrease by 5 Euro per hour."
			}
			landscape.putGenericStringData(IPluginKeys::CAPMAN_WARNING_TEXT, warningText)
			landscape.putGenericStringData(IPluginKeys::CAPMAN_COUNTERMEASURE_TEXT, counterMeasureText)
			landscape.putGenericStringData(IPluginKeys::CAPMAN_CONSEQUENCE_TEXT, consequenceText)
			
		}
		
	}
	
	def void newCPUUtilizationReceived(Node node, double utilization, long timestamp) {
		var cpuUtilHistory = node.getGenericData(IPluginKeys::CAPMAN_CPU_UTIL_HISTORY) as TreeMapLongDoubleIValue
		if (cpuUtilHistory == null) {
			cpuUtilHistory = new TreeMapLongDoubleIValue()
		}
		//If the number of entries is higher then the number allowed by history limit,
		//delete all older entries up to that point.
		var historyEntriesToDelete = cpuUtilHistory.size() - configuration.cpuUtilizationHistoryLimit
		if (historyEntriesToDelete > configuration.cpuUtilizationHistoryLimit) {
			var Set<Long> history = cpuUtilHistory.keySet()
			var long oldestTimestamp = timestamp
			for (var int counter = 0; counter < historyEntriesToDelete; counter++) {
				for (long i : history) {
					if (i < oldestTimestamp) {
						oldestTimestamp = i
					}
				}
			}	
			cpuUtilHistory.remove(oldestTimestamp)
		}
			
		

		// TODO delete old entries. DONE?
		//Delete so many entries permanently?
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
