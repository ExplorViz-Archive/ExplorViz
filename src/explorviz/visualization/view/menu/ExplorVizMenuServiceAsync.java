package explorviz.visualization.view.menu;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ExplorVizMenuServiceAsync {
    
    void getPage(AsyncCallback<String> callback);
    
}
