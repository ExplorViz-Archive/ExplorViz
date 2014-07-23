package explorviz.visualization.view

import com.google.gwt.user.client.rpc.AsyncCallback
import explorviz.visualization.view.ConfigurationPage
import explorviz.visualization.main.PageControl
import explorviz.visualization.experiment.TutorialPage
import explorviz.visualization.main.ErrorDialog
import explorviz.visualization.experiment.EditQuestionsPage

class PageCaller<T> implements AsyncCallback<T> {
	PageControl pageControl

	new (PageControl pageControlParam) {
		pageControl = pageControlParam
	}

	override onFailure(Throwable caught) {
		ErrorDialog::showError(caught)
		pageControl.fadeOutSpinner()
	}

	override onSuccess(T result) {
		val returnedValue = result as String
		switch (returnedValue) {
			case 'explorviz' : showExplorViz
			case 'configuration' : showConfiguration
			case 'tutorial': showTutorial
			case 'editquestions': showEditQuestions
			default: pageControl.setView(returnedValue)
		}
		pageControl.fadeOutSpinner()
	}

	def private showExplorViz() {
		new ExplorVizPage().render(pageControl)
	}
	
	def private showConfiguration() {
		new ConfigurationPage().render(pageControl)
	}
	
	def private showTutorial() {
		new TutorialPage().render(pageControl)
	}
	
	def private showEditQuestions(){
		new EditQuestionsPage().render(pageControl)
	}
}