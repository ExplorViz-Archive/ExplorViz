package explorviz.plugin_server.capacitymanagement

import explorviz.plugin_client.attributes.IPluginKeys
import explorviz.plugin_server.interfaces.ICapacityManager

import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import java.util.Map
import explorviz.plugin_server.capacitymanagement.scaling_strategies.IScalingStrategy
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration
import explorviz.plugin_client.capacitymanagement.execution.ExecutionOrganizer
import explorviz.plugin_client.capacitymanagement.CapManClientSide
import explorviz.plugin_client.capacitymanagement.CapManStates
import explorviz.plugin_client.capacitymanagement.CapManExecutionStates
import explorviz.shared.model.Application
import java.util.ArrayList
import java.util.List
import explorviz.plugin_client.capacitymanagement.configuration.LoadBalancersReader
import explorviz.plugin_client.capacitymanagement.execution.ExecutionAction
import explorviz.plugin_client.capacitymanagement.execution.ApplicationTerminateAction
import explorviz.plugin_client.capacitymanagement.execution.ApplicationRestartAction
import explorviz.plugin_client.capacitymanagement.execution.NodeReplicateAction
import explorviz.plugin_client.capacitymanagement.execution.NodeTerminateAction
import explorviz.plugin_client.capacitymanagement.execution.NodeRestartAction

class CapMan implements ICapacityManager {
		private static final Logger LOG = LoggerFactory.getLogger(typeof(CapMan));
		private final IScalingStrategy strategy;

	private final CapManConfiguration configuration;
	private final ExecutionOrganizer organizer;
	
	new(String test) {
		val strategyClazz = Class
				.forName("explorviz.plugin_server.capacitymanagement.scaling_strategies."
						+ "ScalingStrategyPerformance");
		strategy = ( strategyClazz.getConstructor(typeof(CapManConfiguration))).newInstance(configuration) as IScalingStrategy;
		configuration = new CapManConfiguration();
		organizer = new ExecutionOrganizer(configuration);
	}

	new() {
		val settingsFile = "./META-INF/explorviz.capacity_manager.default.properties";
		LoadBalancersReader.readInLoadBalancers(settingsFile);
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
		//TODO neuer Aufruf
		strategy = ( strategyClazz.getConstructor(typeof(CapManConfiguration))).newInstance(configuration) as IScalingStrategy;
		//strategy = ( strategyClazz.getConstructor()).newInstance(configuration) as IScalingStrategy;
	}

	override doCapacityManagement(Landscape landscape) {
		var double maxRootCauseRating = initializeAndGetHighestRCR(landscape)
		var List<Application> applicationsToBeAnalysed = getApplicationsToBeAnalysed(landscape, maxRootCauseRating)
		var Map<Application, Integer> planMapApplication = strategy.analyzeApplications(landscape, applicationsToBeAnalysed);
		createApplicationExecutionPlan(landscape, planMapApplication)
	}
	
	def double initializeAndGetHighestRCR(Landscape landscape) {
		//Save the largest Root Cause Rating
		var double maxRootCauseRating = 0
		
		//Initialize CapManStatus.
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					node.putGenericData(IPluginKeys::CAPMAN_STATE, CapManStates::NONE)
					for (application : node.applications) {
						if (application.isGenericDataPresent(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)) {
							var rating = application.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)
							//comparing rating with abs() because it can be positive or negative
							if(Math.abs(rating) > Math.abs(maxRootCauseRating)) {
								//Update maximum root cause rating.
								maxRootCauseRating = rating	
							}
						}
						
						application.putGenericData(IPluginKeys::CAPMAN_STATE, CapManStates::NONE)
						
						// TODO update the current progress of restarting action
						application.putGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE, CapManExecutionStates::NONE)
					}
				}
			}
		}
		
		
		return maxRootCauseRating
	}
	
	//Collect all the Applications that are down to 10% below the maximum rating.
	def List<Application> getApplicationsToBeAnalysed(Landscape landscape, double rootCauseRating) {
		var List<Application> applicationGroup = new ArrayList<Application>()
		
		
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						if (application.isGenericDataPresent(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)) {
							if(Math.abs(application.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)) 
								>= Math.abs(rootCauseRating) - 0.1) {
								applicationGroup.add(application)
							}
						}
					}
				}
			}
		}
		return applicationGroup
	} 
	
	
	//Execution Plan for Applications
	//if an application should be replicated we need to replicate the whole node. 
	//That's because the node is the system thats overloaded
	def void createApplicationExecutionPlan(Landscape landscape, Map<Application, Integer> planMapApplication) {
		var String warningText = ""
		var String counterMeasureText = ""
		var String consequenceText = ""
		//TODO maybe insert a if isgenericdatapresent-thingy
		var String oldPlanId = landscape.getGenericStringData(IPluginKeys::CAPMAN_NEW_PLAN_ID)
		var String newPlanId = ""
		var now = landscape.timestamp
		//set new plan id -- but only after X seconds from last plan ID
		if (landscape.isGenericDataPresent(IPluginKeys::CAPMAN_TIMESTAMP_LAST_PLAN)) {
			newPlanId = computePlanId(configuration.waitTimeForNewPlan, landscape, now, Integer.parseInt(oldPlanId))
		} else {
			newPlanId = "0";
		}		
		//if we have a new id, create new plan
		if(!oldPlanId.equalsIgnoreCase(newPlanId)) {
			landscape.putGenericStringData(IPluginKeys::CAPMAN_NEW_PLAN_ID, newPlanId)
			for (Map.Entry<Application, Integer> mapEntries : planMapApplication.entrySet()) {
				if (mapEntries.getValue() == 0) {
					//terminate application
					CapManClientSide::setElementShouldBeTerminated(mapEntries.key, true)
	
					warningText += "Application: " + mapEntries.key.name + "of Node: " + mapEntries.key.parent.displayName
						+ "is error-prone, because the application is underloaded and their exists at least on other instance of this application." 
					 //+ "Also the CPU-Utilization is below the set cpu-bound of " + configuration.cpuBoundForApplications * 100 +"%."
					counterMeasureText += "It is suggested to terminate Application " + mapEntries.key.name + "."
					consequenceText += "After the change, the operating costs decrease by 5 Euro per hour."
					
					//if application is the last application of the node, terminate the node also
					if (mapEntries.key.parent.applications.size <= 1) {
						CapManClientSide::setElementShouldBeTerminated(mapEntries.key.parent, true)
						warningText += "Node: " + mapEntries.key.parent.displayName +" will be empty after terminating the application "+ mapEntries.key.name
						counterMeasureText += "It is suggested to terminate the node "+ mapEntries.key.parent.displayName
					}
				} else if (mapEntries.getValue() == 1) {
					//replicate node
					CapManClientSide::setElementShouldBeReplicated(mapEntries.key.parent, true)
					warningText += "Application: " + mapEntries.key.name + "of Node: " + mapEntries.key.parent.displayName
					 + "is error-prone, because of the node being overloaded." 
					 //+ "Also the CPU-Utilization is above the set cpu-bound of " + configuration.cpuBoundForApplications * 100 +"%."
					 counterMeasureText += "It is suggested to replicate the node " + mapEntries.key.parent.displayName + "."
					 consequenceText += "After the change, the response time is improved and the operating costs increase by 5 Euro per hour."
				} /*else {
					CapManClientSide::setElementShouldBeRestarted(mapEntries.getKey(), true)
					warningText += "Application: " + mapEntries.getKey().id + "of Node: " + mapEntries.getKey().parent.getDisplayName()
					 + "has warnings, because of the anomalyscore being high. " 
					 counterMeasureText += "It is suggested to restart Application " + mapEntries.getKey().id + "."
					 consequenceText += "After the change, hopefully there is nothing to do here."
					
				}*/
				landscape.putGenericStringData(IPluginKeys::CAPMAN_WARNING_TEXT, warningText)
				landscape.putGenericStringData(IPluginKeys::CAPMAN_COUNTERMEASURE_TEXT, counterMeasureText)
				landscape.putGenericStringData(IPluginKeys::CAPMAN_CONSEQUENCE_TEXT, consequenceText)
			}
		}
	}

	def String computePlanId(int waitTimeForNewPlan, Landscape landscape, long now, Integer planId) {
		var int newPlanId = planId
		//if time from last plan exceeds current-time - wait time, create new ID
		//waitTime is multiplicated times 1000 because calendar.time is in milliseconds
		if (landscape.getGenericLongData(IPluginKeys::CAPMAN_TIMESTAMP_LAST_PLAN) < (now - (1000 * waitTimeForNewPlan))) {
			if (landscape.isGenericDataPresent(IPluginKeys::CAPMAN_NEW_PLAN_ID)) {
				newPlanId += 1
			}
			//since we will create a new plan, save the time for this plan
			landscape.putGenericLongData(IPluginKeys::CAPMAN_TIMESTAMP_LAST_PLAN, now)
		} 
		return newPlanId.toString()
	}
	//ExecutionPlan setting CapManStates in Nodes.
	//Display UserDialog.
	//TODO Reconsider texts of warnings, countermeasure etc. for nodes. Also when is this stuff called?
	/*def void createNodeExecutionPlan(Landscape landscape, Map<Node, Boolean> planMapNode) {
		var String warningText = ""
		var String counterMeasureText = ""
		var String consequenceText = ""
		
		for (Map.Entry<Node, Boolean> mapEntries : planMapNode.entrySet()) {
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
		
	}*/
	

	//convert CapMan-plan to action list
	override receivedFinalCapacityAdaptationPlan(Landscape landscape) {
		var ArrayList<ExecutionAction> actionList = new ArrayList<ExecutionAction>()
		println("Received capman plan at: " + landscape.timestamp)
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {
						if (application.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) {
							// dont modify the landscape here - only modify in doCapacityManagement
							val state = application.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
							if (state == CapManStates::TERMINATE) {
								actionList.add(new ApplicationTerminateAction(application));
							} else if (state == CapManStates::RESTART) {
								actionList.add(new ApplicationRestartAction(application));
							}
							//TODO migration missing, replicate option for user?
						}
					}
					if (node.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) {
						// dont modify the landscape here - only modify in doCapacityManagement
						val state = node.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
						if (state == CapManStates::REPLICATE) {
							actionList.add(new NodeReplicateAction(node));
						} else if (state == CapManStates::TERMINATE) {
							actionList.add(new NodeTerminateAction(node));
						} else if (state == CapManStates::RESTART) {
							actionList.add(new NodeRestartAction(node));
						}
					}
				}
			}
		}
		organizer.executeActionList(actionList);
	}
	
}
