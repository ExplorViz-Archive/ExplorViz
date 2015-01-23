package explorviz.plugin_server.capacitymanagement

import explorviz.plugin_client.attributes.IPluginKeys
import explorviz.plugin_client.attributes.TreeMapLongDoubleIValue
import explorviz.plugin_server.interfaces.ICapacityManager

import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.shared.model.Node
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

class CapMan implements ICapacityManager {
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
		
		var List<Application> applicationsToBeAnalysed = getApplicationsToBeAnalysed(landscape, maxRootCauseRating)
		var Map<Application, Integer> planMapApplication = strategy.analyzeApplications(applicationsToBeAnalysed);
		
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
					}
				}
			}
		}
		return applicationGroup
	} 
	
	
	//Execution Plan for Applications
	def void createApplicationExecutionPlan(Landscape landscape, Map<Application, Integer> planMapApplication) {
		var String warningText = ""
		var String counterMeasureText = ""
		var String consequenceText = ""
		
		for (Map.Entry<Application, Integer> mapEntries : planMapApplication.entrySet()) {
			if (mapEntries.getValue() == 0) {
				CapManClientSide::setElementShouldBeTerminated(mapEntries.getKey(), true)
				warningText += "Application: " + mapEntries.getKey().id + "of Node: " + mapEntries.getKey().parent.getDisplayName()
				 + "is error-prone, because of the anomalyscore being too damn high. " 
				 + "Also the CPU-Utilization is below the set cpu-bound of " + configuration.cpuBoundForApplications * 100 +"%."
				 counterMeasureText += "It is suggested to terminate Application " + mapEntries.getKey().id + "."
				 consequenceText += "After the change, the operating costs decrease by 5 Euro per hour."
			} else if (mapEntries.getValue() == 1) {
				CapManClientSide::setElementShouldStartNewInstance(mapEntries.getKey(), true)
				warningText += "Application: " + mapEntries.getKey().id + "of Node: " + mapEntries.getKey().parent.getDisplayName()
				 + "is error-prone, because of the anomalyscore being too damn high. " 
				 + "Also the CPU-Utilization is above the set cpu-bound of " + configuration.cpuBoundForApplications * 100 +"%."
				 counterMeasureText += "It is suggested to start a new application of " + mapEntries.getKey().id + "."
				 consequenceText += "After the change, the response time is improved and the operating costs increase by 5 Euro per hour."
			} else {
				CapManClientSide::setElementShouldBeRestarted(mapEntries.getKey(), true)
				warningText += "Application: " + mapEntries.getKey().id + "of Node: " + mapEntries.getKey().parent.getDisplayName()
				 + "has warnings, because of the anomalyscore being high. " 
				 counterMeasureText += "It is suggested to restart Application " + mapEntries.getKey().id + "."
				 consequenceText += "After the change, hopefully there is nothing to do here."
				
			}
			landscape.putGenericStringData(IPluginKeys::CAPMAN_WARNING_TEXT, warningText)
			landscape.putGenericStringData(IPluginKeys::CAPMAN_COUNTERMEASURE_TEXT, counterMeasureText)
			landscape.putGenericStringData(IPluginKeys::CAPMAN_CONSEQUENCE_TEXT, consequenceText)
			
		}
	}
	//ExecutionPlan setting CapManStates in Nodes.
	//Display UserDialog.
	//TODO Reconsider texts of warnings, countermeasure etc. for nodes. Also when is this stuff called?
	def void createNodeExecutionPlan(Landscape landscape, Map<Node, Boolean> planMapNode) {
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
	
}
