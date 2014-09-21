package explorviz.server.experiment

import explorviz.shared.model.Landscape
import explorviz.server.repository.LandscapePreparer

/**
 * @author Santje Finke
 * 
 */
class EmptyLandscapeCreator {
	var static int applicationId = 0

	/**
	 * Provides an empty landscape that can be used to reduce lag when the previously shown
	 * landscape isn't needed anymore.
	 */
	def static createEmptyLandscape() {
		applicationId = 0

		val landscape = new Landscape()
		landscape.hash = java.lang.System.currentTimeMillis
		
		LandscapePreparer.prepareLandscape(landscape)
	}

}
