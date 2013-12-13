package explorviz.server.codeviewer;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.visualization.codeviewer.CodeViewerService;

public class CodeViewerServiceImpl extends RemoteServiceServlet implements CodeViewerService {

	private static final long serialVersionUID = -8158302993483043223L;

	@Override
	public String getCode(final String project, final String file) {
		return CodeViewerGetCode.getCode(file, getServletContext().getRealPath("/"));
	}

	@Override
	public String getCodeStructure(final String project) {
		return CodeViewerListCodeStructure.getCodeTreeExample();
	}

}
