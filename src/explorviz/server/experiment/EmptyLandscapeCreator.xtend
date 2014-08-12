package explorviz.server.experiment

import explorviz.shared.model.Landscape
import explorviz.server.repository.LandscapePreparer

class EmptyLandscapeCreator {
	var static int applicationId = 0

	def static createTutorialLandscape() {
		applicationId = 0

		val landscape = new Landscape()
		landscape.hash = 9 //java.lang.System.currentTimeMillis
		
		LandscapePreparer.prepareLandscape(landscape)
	}

}
