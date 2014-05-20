package explorviz.visualization.experiment

import explorviz.visualization.view.IPage
import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.engine.main.WebGLStart

class QuestionPage implements IPage{
	
	override render(PageControl pageControl) {
		var htmlResult = '''<div id="test">Hier kommt der Fragebogen</div>'''.toString()
		
		pageControl.setView(htmlResult)
		
		Navigation::registerWebGLKeys()
		WebGLStart::initWebGL()
	}
	
}