package explorviz.visualization.layout

import explorviz.visualization.model.LandscapeClientSide

import explorviz.visualization.layout.exceptions.LayoutException
import explorviz.visualization.model.ApplicationClientSide

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