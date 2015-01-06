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

		ranCorrLandscape.calculateRootCauseRatings(RanCorrConfiguration.ranCorrAlgorithm,
			RanCorrConfiguration.ranCorrAggregationAlgorithm)
		ranCorrLandscape.persistRootCauseRatings(RanCorrConfiguration.ranCorrPersistAlgorithm)
	}
}
