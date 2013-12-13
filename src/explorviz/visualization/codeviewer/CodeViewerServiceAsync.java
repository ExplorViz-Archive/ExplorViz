package explorviz.visualization.codeviewer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CodeViewerServiceAsync {
    
    void getCode(String project, String file, AsyncCallback<String> callback);
    
    void getCodeStructure(String project, AsyncCallback<String> callback);
    
}
