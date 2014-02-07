package explorviz.visualization.layout

import explorviz.visualization.model.LandscapeClientSide

import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.layout.application.ApplicationLayoutInterface
import explorviz.visualization.layout.landscape.LandscapeKielerInterface

class LayoutService {

	def static LandscapeClientSide layoutLandscape(LandscapeClientSide landscape)
			throws LayoutException {
		LandscapeKielerInterface::applyLayout(landscape)
	}
	
	def static ApplicationClientSide layoutApplication(ApplicationClientSide application)
			throws LayoutException {
		ApplicationLayoutInterface::applyLayout(application)
	}
}