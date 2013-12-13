package explorviz.visualization.view.menu;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CodeViewerMenuServiceAsync {
    
    void getPage(AsyncCallback<String> callback);
    
}
