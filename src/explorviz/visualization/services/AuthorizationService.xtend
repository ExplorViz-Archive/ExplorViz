package explorviz.visualization.services

import explorviz.visualization.main.ExplorViz
import explorviz.shared.auth.User

class AuthorizationService {
	def static String getCurrentUsername() {
		if (ExplorViz::currentUser != null)
			ExplorViz::currentUser.username
		else
			""
	}

	def static User getCurrentUser() {
		ExplorViz::currentUser
	}
	
	def static boolean currentUserHasRole(String rolename) {
		if (ExplorViz::currentUser != null) {
			for (role : ExplorViz::currentUser.roles) {
				if (role.rolename != null && role.rolename.equalsIgnoreCase(rolename)) {
					return true
				}
			}
		}
		
		false
	}
}
