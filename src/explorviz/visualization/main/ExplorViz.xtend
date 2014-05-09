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
import explorviz.visualization.login.LoginService
import explorviz.visualization.login.LoginServiceAsync
import elemental.client.Browser
import com.google.gwt.user.client.Window
import explorviz.visualization.engine.navigation.Navigation

class ExplorViz implements EntryPoint, PageControl {
    
    static Element view
    static Element spinner
    
    static RootPanel configuration_ribbon
    static RootPanel reset_landscape_ribbon
    static RootPanel codeviewer_ribbon
    static RootPanel explorviz_ribbon
    
    public static String currentUserName
    
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
		
		val LoginServiceAsync loginService = GWT::create(typeof(LoginService))
		val endpoint = loginService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "loginservice"
		loginService.getCurrentUsername(new UsernameCallBack())
		
		configuration_ribbon = RootPanel::get("configuration_ribbon")
		reset_landscape_ribbon = RootPanel::get("reset_landscape")
		codeviewer_ribbon = RootPanel::get("codeviewer_ribbon")
		explorviz_ribbon = RootPanel::get("explorviz_ribbon")
		
		createConfigurationRibbonLink()
		createCodeViewerRibbonLink()
		createExplorVizRibbonLink()
		
		JSHelpers::registerResizeHandler()
        
		callFirstPage()
	}
	
	def static resizeHandler() {
		WebGLStart::disable
		Navigation::deregisterWebGLKeys
		
	    view.setInnerHTML("")
	    
	    Navigation::registerWebGLKeys()
	    
		WebGLStart::initWebGL()
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
		
		configuration_ribbon.sinkEvents(Event::ONCLICK)
		configuration_ribbon.addHandler([
			fadeInSpinner()
			setExplorVizInvisible()
			
        	explorviz_ribbon.element.parentElement.className = ""
        	codeviewer_ribbon.element.parentElement.className = ""
        	configuration_ribbon.element.parentElement.className = "active"
        	
			configurationService.getPage(callback)
		], ClickEvent::getType())
		
        reset_landscape_ribbon.sinkEvents(Event::ONCLICK)
        reset_landscape_ribbon.addHandler([
            // TODO
            landscapeExchangeService.resetLandscape(new DummyCallBack());
        ], ClickEvent::getType())
	}
	
	def private createCodeViewerRibbonLink() {
        val CodeViewerMenuServiceAsync codeViewerService = GWT::create(typeof(CodeViewerMenuService))
        
        val endpoint = codeViewerService as ServiceDefTarget
        val moduleRelativeURL = GWT::getModuleBaseURL() + "codeviewermenu"
        endpoint.serviceEntryPoint = moduleRelativeURL
        
        codeviewer_ribbon.sinkEvents(Event::ONCLICK)
        codeviewer_ribbon.addHandler([
            fadeInSpinner()
            setExplorVizInvisible()
            
        	explorviz_ribbon.element.parentElement.className = ""
        	codeviewer_ribbon.element.parentElement.className = "active"
        	configuration_ribbon.element.parentElement.className = ""
        	
            codeViewerService.getPage(callback)
        ], ClickEvent::getType())
    }
	
    def private createExplorVizRibbonLink() {
        val ExplorVizMenuServiceAsync explorvizService = GWT::create(typeof(ExplorVizMenuService))
        
        val endpoint = explorvizService as ServiceDefTarget
        val moduleRelativeURL = GWT::getModuleBaseURL() + "explorvizmenu"
        endpoint.serviceEntryPoint = moduleRelativeURL
        
        explorviz_ribbon.sinkEvents(Event::ONCLICK)
        explorviz_ribbon.addHandler([
            fadeInSpinner()
            setExplorVizInvisible()
            
       		explorviz_ribbon.element.parentElement.className = "active"
        	codeviewer_ribbon.element.parentElement.className = ""
        	configuration_ribbon.element.parentElement.className = ""
            
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

class LogoutCallBack implements AsyncCallback<Void> {
	override onFailure(Throwable caught) {
	}
	
	override onSuccess(Void result) {
		Window.Location.reload()
	}
}

class UsernameCallBack implements AsyncCallback<String> {
	override onFailure(Throwable caught) {
	}
	
	override onSuccess(String result) {
		ExplorViz.currentUserName = result
		if (ExplorViz.currentUserName != null && ExplorViz.currentUserName != "") {
			Browser::getDocument().getElementById("username").innerHTML = "Signed in as <b>" + ExplorViz.currentUserName + "</b> "
			
			val logoutA = Browser::getDocument().createAnchorElement
			logoutA.innerHTML = "(logout)"
			logoutA.className = "navbar-link"
			logoutA.id = "logout"
			
			val LoginServiceAsync loginService = GWT::create(typeof(LoginService))
			val endpoint = loginService as ServiceDefTarget
			endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "loginservice"
	        logoutA.addEventListener("click", [
	        	loginService.logout(new LogoutCallBack)
	        ], false)
	        
	        Browser::getDocument().getElementById("username").appendChild(logoutA)
        }
	}
}