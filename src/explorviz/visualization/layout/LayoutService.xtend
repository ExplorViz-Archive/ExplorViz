package explorviz.visualization.layout

import explorviz.shared.model.Application
import explorviz.shared.model.Landscape
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.layout.landscape.LandscapeKielerInterface

class LayoutService {

	def static Landscape layoutLandscape(Landscape landscape)
			throws LayoutException {
		LandscapeKielerInterface::applyLayout(landscape)
	}
	
	def static Application layoutApplication(Application application)
			throws LayoutException {
		ApplicationLayoutInterface::applyLayout(application)
	}
}