package explorviz.visualization.view

import explorviz.visualization.main.PageControl
import explorviz.visualization.codeviewer.CodeViewer
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.adaptivemonitoring.AdaptiveMonitoring

class CodeViewerPage implements IPage {
	override render(PageControl pageControl) {
        Navigation::deregisterWebGLKeys()
	    
	    val htmlResult = '''<div id="codetreeview-wrapper"><div id="codetreeview"></div></div>
                <div id="codeview-wrapper"><h1 id="codeview-filename"></h1><div id="codeview" style="height:100%"></div></div>'''.toString()
	    
		pageControl.setView(htmlResult)
		CodeViewer::init()
		AdaptiveMonitoring::init()
	}
}