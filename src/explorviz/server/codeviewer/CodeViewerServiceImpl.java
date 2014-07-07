package explorviz.server.codeviewer;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.visualization.codeviewer.CodeViewerService;

public class CodeViewerServiceImpl extends RemoteServiceServlet implements CodeViewerService {

	private static final long serialVersionUID = -8158302993483043223L;

	@Override
	public String getCode(final String project, final String file) {
		final String sourceFolder = getServletContext().getRealPath("/source/");

		return CodeViewerGetCode.getCode(sourceFolder, file);
	}

	@Override
	public String getCodeStructure(final String project) {
		final String sourceFolder = getServletContext().getRealPath("/source/" + project + "/");

		return CodeViewerListCodeStructure.getCodeTree(sourceFolder);
	}

}
