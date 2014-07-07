package explorviz.visualization.view.menu;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("explorvizmenu")
public interface ExplorVizMenuService extends RemoteService {
	public String getPage();
}