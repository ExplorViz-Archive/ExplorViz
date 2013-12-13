package explorviz.visualization.codeviewer;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("codeviewer")
public interface CodeViewerService extends RemoteService {
    public String getCode(String project, String file);
    
    public String getCodeStructure(String project);
}
