package explorviz.visualization.view.menu;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ExplorVizMenuServiceImpl extends RemoteServiceServlet implements ExplorVizMenuService {

	private static final long serialVersionUID = -1013985260361101696L;

	@Override
	public String getPage() {
		return "explorviz";
	}
}
