package explorviz.visualization.view.menu;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("codeviewermenu")
public interface CodeViewerMenuService extends RemoteService {
    public String getPage();
}
