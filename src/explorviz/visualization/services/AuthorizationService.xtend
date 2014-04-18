package explorviz.visualization.services

import explorviz.visualization.main.ExplorViz

class AuthorizationService {
    def static getCurrentUsername() {
        ExplorViz::currentUserName
    }
    
    def static getCurrentUser() {
    }
}