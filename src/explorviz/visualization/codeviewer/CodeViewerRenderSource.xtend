package explorviz.visualization.codeviewer

import com.google.gwt.user.client.rpc.AsyncCallback

class CodeViewerRenderSource<T> implements AsyncCallback<T> {
    var String filename
    
    new(String filenameParam) {
        filename = filenameParam
    }
    
	override onFailure(Throwable caught) {
	    // TODO
//		new ErrorPage().renderWithMessage(pageControl, caught.getMessage())
	}

	override onSuccess(T result) {
		val source = result as String
		
		CodeMirrorJS::startCodeMirror(source, filename)
	}
}