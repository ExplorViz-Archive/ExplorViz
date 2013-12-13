package explorviz.visualization.view

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.view.CodeViewerPage
import explorviz.visualization.view.ConfigurationPage
import explorviz.visualization.main.PageControl
import explorviz.visualization.view.ErrorPage

class PageCaller<T> implements AsyncCallback<T> {
	PageControl pageControl

	new (PageControl pageControlParam) {
		pageControl = pageControlParam
	}

	override onFailure(Throwable caught) {
		new ErrorPage().renderWithMessage(pageControl, caught.getMessage())
		pageControl.fadeOutSpinner()
	}

	override onSuccess(T result) {
		val returnedValue = result as String
		switch (returnedValue) {
			case 'explorviz' : showExplorViz
			case 'codeviewer' : showCodeViewer
			case 'configuration' : showConfiguration
			default: pageControl.setView(returnedValue)
		}
		pageControl.fadeOutSpinner()
	}

	def private showExplorViz() {
		new ExplorVizPage().render(pageControl)
	}

	def private showCodeViewer() {
		new CodeViewerPage().render(pageControl)
	}
	
	def private showConfiguration() {
		new ConfigurationPage().render(pageControl)
	}
}