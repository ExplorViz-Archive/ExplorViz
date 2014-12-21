package explorviz.plugin.rootcausedetection

import explorviz.plugin.interfaces.IRootCauseDetector
import explorviz.server.main.PluginManagerServerSide
import explorviz.shared.model.Landscape
import explorviz.plugin.rootcausedetection.model.RanCorrLandscape
import explorviz.shared.model.Component

class RanCorr implements IRootCauseDetector {

	new() {
		PluginManagerServerSide::registerAsRootCauseDetector(this)
	}

	def void addComponentsAndClasses(Component component, RanCorrLandscape ranCorrLandscape) {
		ranCorrLandscape.addPackage(component)

		for (clazz : component.clazzes) {

			// add classes of this component
			ranCorrLandscape.addClass(clazz)
		}

		for (subcomponent : component.children) {

			// add subcomponents to the RanCorr landscape
			addComponentsAndClasses(component, ranCorrLandscape)
		}
	}

	override doRootCauseDetection(Landscape landscape) {
		val ranCorrLandscape = new RanCorrLandscape()

		for (system : landscape.systems) {
			for (nodeGroup : system.nodeGroups) {
				for (node : nodeGroup.nodes) {
					for (application : node.applications) {

						// add all applications
						ranCorrLandscape.addApplication(application)

						for (operation : application.communications) {

							// add all operations in the current application
							ranCorrLandscape.addOperation(operation)
						}

						for (component : application.components) {

							// add all components and classes in the current application
							addComponentsAndClasses(component, ranCorrLandscape)
						}
					}
				}
			}
		}

		ranCorrLandscape.calculateRootCauseRatings(RanCorrConfiguration.ranCorrAlgorithm)
		ranCorrLandscape.persistRootCauseRatings(RanCorrConfiguration.ranCorrPersistAlgorithm)

	// A few examples for later...
	//   See if Anomaly Scores a available:
	//     if (application.isGenericDataPresent(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE))
	//   Get anomaly score:
	//     var anomalyScores = application.getGenericData(IPluginKeys::TIMESTAMP_TO_ANOMALY_SCORE) as TreeMapLongDoubleIValue
	//   Write RGB output:
	//     application.putGenericStringData(IPluginKeys::ROOTCAUSE_RGB_INDICATOR, "255,0,0")
	}
}
