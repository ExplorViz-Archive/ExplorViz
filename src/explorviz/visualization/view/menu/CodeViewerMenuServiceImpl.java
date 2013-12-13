package explorviz.visualization.view.menu;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CodeViewerMenuServiceImpl extends RemoteServiceServlet implements
	CodeViewerMenuService {
    
    private static final long serialVersionUID = -1013985260361101696L;
    
    @Override
    public String getPage() {
	return "codeviewer";
    }
}
