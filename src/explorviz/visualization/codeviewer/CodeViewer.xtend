package explorviz.visualization.codeviewer

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.interaction.Usertracking

class CodeViewer {
    private static var CodeViewerServiceAsync codeViewerService
    protected static var String currentProject
    
    def static init() {
        codeViewerService = createAsyncService()
    }
    
    def static openDialog(String project, String filepath, String filename) {
    	currentProject = project
    	
        codeViewerService.getCodeStructure(project, new CodeViewerRenderCodeStructure(filepath, filename))
    }

    def static getCode(String filepath, String filename) {
        Usertracking::trackCodeviewerCode(currentProject, filepath, filename)
        
        var file = filepath + "/" + filename
        if (!file.toLowerCase.startsWith(currentProject.toLowerCase)) {
        	file = currentProject + "/" + file
        }
        codeViewerService.getCode(currentProject, file, new CodeViewerRenderSource(filename))
    }
    
    def static private createAsyncService() {
        val CodeViewerServiceAsync codeViewerService = GWT::create(typeof(CodeViewerService))
        val endpoint = codeViewerService as ServiceDefTarget
        val moduleRelativeURL = GWT::getModuleBaseURL() + "codeviewer"
        endpoint.serviceEntryPoint = moduleRelativeURL
        
        codeViewerService
    }
}
