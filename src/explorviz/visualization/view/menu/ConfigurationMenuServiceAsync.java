package explorviz.visualization.view.menu;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationMenuServiceAsync {
    void getPage(AsyncCallback<String> callback);
}
