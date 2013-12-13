package explorviz.visualization.codeviewer

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.interaction.Usertracking

class CodeViewer {
    static var CodeViewerServiceAsync codeViewerService
    
    def static init() {
        codeViewerService = createAsyncService()
        codeViewerService.getCodeStructure("explorviz", new CodeViewerRenderCodeStructure())
    }

    def static getCode(String project, String filepath, String filename) {
        Usertracking::trackCodeviewerCode(project, filepath, filename)
        codeViewerService.getCode(project, filepath + "/" + filename, new CodeViewerRenderSource(filename))
    }
    
    def static private createAsyncService() {
        val CodeViewerServiceAsync codeViewerService = GWT::create(typeof(CodeViewerService))
        val endpoint = codeViewerService as ServiceDefTarget
        val moduleRelativeURL = GWT::getModuleBaseURL() + "codeviewer"
        endpoint.serviceEntryPoint = moduleRelativeURL
        
        codeViewerService
    }
}
