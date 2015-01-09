package explorviz.visualization.landscapeinformation

class EventViewer {
    
    def static init() {
    }
    
    def static openDialog() {
    	EventViewerJS::openDialog
    	EventViewerJS::setEventText('<b>2010-20-10 10:00:</b> New Node "xxx" detected')
    }
}
