package explorviz.visualization.main

import com.google.gwt.core.client.EntryPoint
import com.google.gwt.core.client.GWT
import com.google.gwt.dom.client.Element
import com.google.gwt.dom.client.Style
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.rpc.ServiceDefTarget
import com.google.gwt.user.client.ui.RootPanel
import explorviz.visualization.codeviewer.CodeMirrorJS
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.view.PageCaller
import explorviz.visualization.view.js.CenterElementJS
import explorviz.visualization.view.menu.CodeViewerMenuService
import explorviz.visualization.view.menu.CodeViewerMenuServiceAsync
import explorviz.visualization.view.menu.ConfigurationMenuService
import explorviz.visualization.view.menu.ConfigurationMenuServiceAsync
import explorviz.visualization.view.menu.ExplorVizMenuService
import explorviz.visualization.view.menu.ExplorVizMenuServiceAsync
import java.util.logging.Level
import java.util.logging.Logger
import explorviz.visualization.landscapeexchange.LandscapeExchangeService
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync

class ExplorViz implements EntryPoint, PageControl {
    
    Element view
    Element spinner
    
    AsyncCallback<String> callback
    
    val logger = Logger::getLogger("ExplorVizMainLogger")
    
    @Override
    override onModuleLoad() {
        GWT::setUncaughtExceptionHandler([
        	val message = if (it.cause != null) it.cause.message else it.message
        	val stackTrace = if (it.cause != null) createStackStringFromThrowable(it.cause) else createStackStringFromThrowable(it)
        	
            logger.log(Level::SEVERE, "Uncaught Error occured: " + message + " " + stackTrace)
        ])
        
		view = RootPanel::get("view").element
		spinner = DOM::getElementById("spinner")
		
		createConfigurationRibbonLink()
		createCodeViewerRibbonLink()
		createExplorVizRibbonLink()
        
        
        
		callFirstPage()
	}
	
    def static createStackStringFromThrowable(Throwable t) {
        var stack = ""
        var i = 0
        while (i < t.stackTrace.length) {
            stack = stack + "\n\t" + (t.stackTrace.get(i))
            i = i + 1
        }
        stack
    }
	
	def private callFirstPage() {
		callback = new PageCaller<String>(this)
		callback.onSuccess("explorviz")
	}
	
	def private createConfigurationRibbonLink() {
		val ConfigurationMenuServiceAsync configurationService = GWT::create(typeof(ConfigurationMenuService))
		val endpoint = configurationService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "configurationmenu"
		
        val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
        val endpointLandscape = landscapeExchangeService as ServiceDefTarget
        endpointLandscape.serviceEntryPoint = GWT::getModuleBaseURL() + "landscapeexchange"
		
		val configuration_ribbon = RootPanel::get("configuration_ribbon")
		configuration_ribbon.sinkEvents(Event::ONCLICK)
		configuration_ribbon.addHandler([
			fadeInSpinner()
			setExplorVizInvisible
			configurationService.getPage(callback)
		], ClickEvent::getType())
		
        val reset_landscape = RootPanel::get("reset_landscape")
        reset_landscape.sinkEvents(Event::ONCLICK)
        reset_landscape.addHandler([
            // TODO
            landscapeExchangeService.resetLandscape(new DummyCallBack());
        ], ClickEvent::getType())
	}
	
	def private createCodeViewerRibbonLink() {
        val CodeViewerMenuServiceAsync codeViewerService = GWT::create(typeof(CodeViewerMenuService))
        
        val endpoint = codeViewerService as ServiceDefTarget
        val moduleRelativeURL = GWT::getModuleBaseURL() + "codeviewermenu"
        endpoint.serviceEntryPoint = moduleRelativeURL
        
        val configuration_ribbon = RootPanel::get("codeviewer_ribbon")
        configuration_ribbon.sinkEvents(Event::ONCLICK)
        configuration_ribbon.addHandler([
            fadeInSpinner()
            setExplorVizInvisible
            codeViewerService.getPage(callback)
        ], ClickEvent::getType())
        
        val undo_codeviewer = RootPanel::get("undo_codeviewer")
        undo_codeviewer.sinkEvents(Event::ONCLICK)
        undo_codeviewer.addHandler([
            CodeMirrorJS::undoCodeMirror
            CodeMirrorJS::updateHistoryDisplayCodeMirror
        ], ClickEvent::getType())
        
        val redo_codeviewer = RootPanel::get("redo_codeviewer")
        redo_codeviewer.sinkEvents(Event::ONCLICK)
        redo_codeviewer.addHandler([
            CodeMirrorJS::redoCodeMirror
            CodeMirrorJS::updateHistoryDisplayCodeMirror
        ], ClickEvent::getType())
    }
	
    def private createExplorVizRibbonLink() {
        val ExplorVizMenuServiceAsync explorvizService = GWT::create(typeof(ExplorVizMenuService))
        
        val endpoint = explorvizService as ServiceDefTarget
        val moduleRelativeURL = GWT::getModuleBaseURL() + "explorvizmenu"
        endpoint.serviceEntryPoint = moduleRelativeURL
        
        val explorviz_ribbon = RootPanel::get("explorviz_ribbon")
        explorviz_ribbon.sinkEvents(Event::ONCLICK)
        explorviz_ribbon.addHandler([
            fadeInSpinner()
            setExplorVizInvisible()
            explorvizService.getPage(callback)
        ], ClickEvent::getType())
    }
    
    override fadeInSpinner() {
    	setView("")
    	CenterElementJS::centerSpinner()
		spinner.style.display = Style.Display::BLOCK
    }
    
    override fadeOutSpinner() {
		spinner.style.display = Style.Display::NONE
    }
    
    override setView(String result) {
    	view.setInnerHTML(result)
    }
    
    def private setExplorVizInvisible() {
    	WebGLStart::disable()
    }
}

class DummyCallBack implements AsyncCallback<Void> {
	override onFailure(Throwable caught) {
	}
	
	override onSuccess(Void result) {
	}
}