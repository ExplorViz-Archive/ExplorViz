package explorviz.plugin_server.capacitymanagement

import explorviz.plugin_client.attributes.IPluginKeys
import explorviz.plugin_server.interfaces.ICapacityManager

import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import java.util.Map
import explorviz.plugin_server.capacitymanagement.scaling_strategies.IScalingStrategy
import explorviz.plugin_client.capacitymanagement.configuration.CapManConfiguration
import explorviz.plugin_server.capacitymanagement.execution.ExecutionOrganizer
import explorviz.plugin_client.capacitymanagement.CapManClientSide
import explorviz.plugin_client.capacitymanagement.CapManStates
import explorviz.plugin_client.capacitymanagement.CapManExecutionStates
import explorviz.shared.model.Application
import java.util.ArrayList
import java.util.List
import explorviz.plugin_server.capacitymanagement.configuration.LoadBalancersReader
import explorviz.plugin_server.capacitymanagement.execution.ExecutionAction
import explorviz.plugin_server.capacitymanagement.execution.ApplicationTerminateAction
import explorviz.plugin_server.capacitymanagement.execution.ApplicationRestartAction
import explorviz.plugin_server.capacitymanagement.execution.NodeReplicateAction
import explorviz.plugin_server.capacitymanagement.execution.NodeTerminateAction
import explorviz.plugin_server.capacitymanagement.execution.NodeRestartAction
import explorviz.plugin_server.capacitymanagement.loadbalancer.LoadBalancersFacade
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import explorviz.plugin_server.capacitymanagement.execution.ApplicationMigrateAction
import explorviz.shared.model.Node
import explorviz.plugin_server.capacitymanagement.configuration.InitialSetupReader
import explorviz.plugin_server.capacitymanagement.loadbalancer.ScalingGroupRepository

class CapMan implements ICapacityManager {
	private static final Logger LOG = LoggerFactory.getLogger(typeof(CapMan));
	private IScalingStrategy strategy;
	
	public static boolean planCanceled = false;
	

	private CapManConfiguration configuration;
	private ExecutionOrganizer organizer;
	private boolean initialized = false;
	private ScalingGroupRepository scalingGroupRepo = InitialSetupReader.getScalingGroupRepository();
	
	private boolean initializedGenericData =false;
	
	new() {
	
			configuration = new CapManConfiguration();
			
		organizer = new ExecutionOrganizer(configuration, scalingGroupRepo);
         
       try {
       	val loadbalancerSetupFile = "explorviz.capacity_manager.loadbalancers.properties";
		LoadBalancersReader.readInLoadBalancers(CapManConfiguration.getResourceFolder + loadbalancerSetupFile);
		LoadBalancersFacade::reset();
		
        LOG.info("Capacity Manager started");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            System.exit(1);
        }
		PluginManagerServerSide::registerAsCapacityManager(this);
		val strategyClazz = Class
				.forName("explorviz.plugin_server.capacitymanagement.scaling_strategies."
						+ configuration.getScalingStrategy());
		// Loads strategy to analyze nodes that is determined in the configuration file.
		strategy = ( strategyClazz.getConstructor()).newInstance() as IScalingStrategy;
	}
/**
 * Run CapacityManagement if it's called.
 * @param landscape
 * 			Landscape to work on.
 */
	override doCapacityManagement(Landscape landscape) {
		println(planCanceled)
		if(planCanceled) {
			planCanceled = false;
		    println("wir waren hier drin")
			landscape.putGenericBooleanData(IPluginKeys.ANOMALY_PRESENT, false)
			landscape.putGenericBooleanData(IPluginKeys.CAPMAN_PLAN_IN_PROGRESS, false)
		}
		
		if(!initializedGenericData && landscape.systems.size != 0){
			initializedGenericData = true
			landscape.putGenericBooleanData(IPluginKeys.CAPMAN_PLAN_IN_PROGRESS, false)
			landscape.putGenericDoubleData(IPluginKeys.CAPMAN_NEW_PLAN_ID, 0.0)
		}
		if(!initialized){
			initialized = true;
			LOG.info("Initial setup of the landscape: Nodes and applications will be started.");
			val initialSetupFile = "explorviz.capacity_manager.initial_setup.properties";
 
		val nodesToStart = InitialSetupReader.readInitialSetup(CapManConfiguration.getResourceFolder + initialSetupFile);
		
		organizer.executeActionList(nodesToStart);
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_WARNING_TEXT, "Test Warning");
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_COUNTERMEASURE_TEXT, "Test CounterMeasure");
//		landscape.putGenericStringData(IPluginKeys::CAPMAN_CONSEQUENCE_TEXT, "Test Consequence");
		}
		if (landscape.isGenericDataPresent(IPluginKeys.ANOMALY_PRESENT)) {
			if (landscape.getGenericBooleanData(IPluginKeys.ANOMALY_PRESENT)) {
				var double maxRootCauseRating = initializeAndGetHighestRCR(landscape)
				var List<Application> applicationsToBeAnalysed = getApplicationsToBeAnalysed(landscape, maxRootCauseRating)
				var Map<Application, Integer> planMapApplication = strategy.analyzeApplications(landscape, applicationsToBeAnalysed, scalingGroupRepo);
				createApplicationExecutionPlan(landscape, planMapApplication)
			}
		}

		
	}
/**
 *  Find the highest RootCauseRating and return it
 *  to be able to filter the applications to be analyzed.
 * @param landscape
 * 			Landscape to work on.
 */
	def double initializeAndGetHighestRCR(Landscape landscape) {
		//Save the largest Root Cause Rating.
		var double maxRootCauseRating = 0
		
		//Initialize CapManStatus.
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					node.putGenericData(IPluginKeys::CAPMAN_STATE, CapManStates::NONE)
					for (application : node.applications) {
						if (application.isGenericDataPresent(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)) {
							var rating = application.getGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY)
							//Comparing rating with abs() because it can be positive or negative.
							if(Math.abs(rating) > Math.abs(maxRootCauseRating)) {
								//Update maximum root cause rating.
								maxRootCauseRating = rating	
							}
						}
						
						application.putGenericData(IPluginKeys::CAPMAN_STATE, CapManStates::NONE)
						application.putGenericData(IPluginKeys::CAPMAN_EXECUTION_STATE, CapManExecutionStates::NONE)
					}
				}
			}
		}
		
		return maxRootCauseRating
	}
	
	/**
	 * Collect all the applications that are down to 10% below the maximum rating.
	 * @param landscape
	 * 			Landscape to work on.
	 * @param rootCauseRating 
	 * 			RootCauseRating for application given by RootCauseDetection.
	 */
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
	
	/**
	 *  Execution Plan for applications. If an application
	 *  should be replicated we need to replicate the whole node.
	 *  That's because the node is the system that is overloaded.
	 * @param landscape 
	 * 			Landscape to work on
	 * @param planMapApplication 
	 * 			List of analyzed applications.
	 */
	def void createApplicationExecutionPlan(Landscape landscape, Map<Application, Integer> planMapApplication) {
		//initialize variables
		var String warningText = ""
		var String counterMeasureText = ""
		var String consequenceText = ""
		var double oldPlanId = landscape.getGenericDoubleData(IPluginKeys::CAPMAN_NEW_PLAN_ID)
		var double newPlanId = oldPlanId;
		
		//Set new plan id -- but only after X seconds from last plan ID.
	println("ja sind wird")
		if (!landscape.getGenericBooleanData(IPluginKeys.CAPMAN_PLAN_IN_PROGRESS)) { 
			newPlanId += 1

		}
		
		//If we have a new id, create new plan. If no plan was created before, let it pass with null
		if(oldPlanId != newPlanId) {
			landscape.putGenericDoubleData(IPluginKeys::CAPMAN_NEW_PLAN_ID, newPlanId)
			for (Map.Entry<Application, Integer> mapEntries : planMapApplication.entrySet()) {
				if (mapEntries.getValue() == 0) {
					//Terminate application.
					CapManClientSide::setElementShouldBeTerminated(mapEntries.key, true)
	
					warningText += "Application: " + mapEntries.key.name + "of Node: " + mapEntries.key.parent.displayName
						+ "is error-prone, because the application is underloaded and there exists at least one other instance of this application." 
					counterMeasureText += "It is suggested to terminate Application " + mapEntries.key.name + "."
					consequenceText += "After the change, the operating costs decrease by 5 Euro per hour."
					
					//If application is the last application of the node, also terminate the node. 
					if (mapEntries.key.parent.applications.size <= 1) {
						CapManClientSide::setElementShouldBeTerminated(mapEntries.key.parent, true)
						warningText += "Node: " + mapEntries.key.parent.displayName +" will be empty after terminating the application "+ mapEntries.key.name
						counterMeasureText += "It is suggested to terminate the node "+ mapEntries.key.parent.displayName
					}
				} else if (mapEntries.getValue() == 1) {
					//Replicate node.
					CapManClientSide::setElementShouldBeReplicated(mapEntries.key.parent, true)
					warningText += "Application: " + mapEntries.key.name + "of Node: " + mapEntries.key.parent.displayName
					 + "is error-prone, because of the node being overloaded." 
					 counterMeasureText += "It is suggested to replicate the node " + mapEntries.key.parent.displayName + "."
					 consequenceText += "After the change, the response time is improved and the operating costs increase by 5 Euro per hour."
				} 
				
				landscape.putGenericStringData(IPluginKeys::CAPMAN_WARNING_TEXT, warningText)
				landscape.putGenericStringData(IPluginKeys::CAPMAN_COUNTERMEASURE_TEXT, counterMeasureText)
				landscape.putGenericStringData(IPluginKeys::CAPMAN_CONSEQUENCE_TEXT, consequenceText)
				
				
				landscape.putGenericBooleanData(IPluginKeys.CAPMAN_PLAN_IN_PROGRESS, true)
			}
		}
	}


	
	/**
	 * Convert CapMan-Plan to action list.
	 * @param landscape 
	 * 			Landscape to work on.
	 */
	override receivedFinalCapacityAdaptationPlan(Landscape landscape) {
		var ArrayList<ExecutionAction> actionList = new ArrayList<ExecutionAction>()
		var loginfo = new StringBuffer();
		println("Received capman plan at: " + landscape.hash)
		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					node.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.RESTARTING);
					for (application : node.applications) {
						if (application.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) {
							try{
							// Dont modify the landscape here - only modify in doCapacityManagement.
							val state = application.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
							if (state == CapManStates::TERMINATE) {
								actionList.add(new ApplicationTerminateAction(application));
								node.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.TERMINATING);
								loginfo.append("Terminate Application " + application.name + " \n");
							} else if (state == CapManStates::RESTART) {
								actionList.add(new ApplicationRestartAction(application));
								node.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.RESTARTING);
								loginfo.append("Restart Application " + application.name + " \n");
							//TODO Migration missing, replicate option for user?
							//Inserted for Migration
							} else if (state == CapManStates::MIGRATE){
								//pick random node for migration
								var Node destinationNode 
								var firstNodeFromNodeGroup = application.parent.parent.nodes.get(0)
								if(firstNodeFromNodeGroup.equals(application.parent)) {
									//src = target
									loginfo.append("Source node is target node")
								}
								destinationNode = firstNodeFromNodeGroup
								
								actionList.add(new ApplicationMigrateAction(application, destinationNode));
								loginfo.append("Migrate Application " + application.name + "to " + destinationNode.ipAddress + " \n");
							}
							}catch(MappingException me){
								me.printStackTrace();
								LOG.error("Error while building ActionList: "+ me.getMessage());
							}
						}
					}
					if (node.isGenericDataPresent(IPluginKeys::CAPMAN_STATE)) {
						val state = node.getGenericData(IPluginKeys::CAPMAN_STATE) as CapManStates
						if (state == CapManStates::REPLICATE) {
							actionList.add(new NodeReplicateAction(node));
							loginfo.append("Replicate node " + node.ipAddress + " \n");
							
						} else if (state == CapManStates::TERMINATE) {
							actionList.add(new NodeTerminateAction(node));
							node.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.TERMINATING);
							loginfo.append("Terminate node " + node.ipAddress + " \n");
						} else if (state == CapManStates::RESTART) {
							actionList.add(new NodeRestartAction(node));
							node.putGenericData(IPluginKeys.CAPMAN_EXECUTION_STATE,
									CapManExecutionStates.RESTARTING);
							loginfo.append("Restart node  " + node.ipAddress + " \n");
						}
					}
				}
			}
		}
		LOG.info("Execution Plan: \n " + loginfo.toString )
		organizer.executeActionList(actionList);
	}
	
	override cancelButton(Landscape landscape) {
		planCanceled = true;
	}
	
}
