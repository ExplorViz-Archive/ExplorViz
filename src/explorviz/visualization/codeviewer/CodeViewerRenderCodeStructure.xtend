package explorviz.visualization.codeviewer

import com.google.gwt.user.client.rpc.AsyncCallback

class CodeViewerRenderCodeStructure<T> implements AsyncCallback<T> {
	override onFailure(Throwable caught) {
	    // TODO
//		new ErrorPage().renderWithMessage(pageControl, caught.getMessage())
	}

	override onSuccess(T result) {
	    val codeStructure = result as String
	    
        CodeMirrorJS::fillCodeTree(codeStructure)
	}
}