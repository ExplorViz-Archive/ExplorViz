package explorviz.visualization.main

import com.google.gwt.core.client.EntryPoint
import com.google.gwt.core.client.GWT
import com.google.gwt.dom.client.Element
import com.google.gwt.dom.client.Style
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.Event
import com.google.gwt.user.client.Window
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.rpc.ServiceDefTarget
import com.google.gwt.user.client.ui.RootPanel
import elemental.client.Browser
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.pageservices.TutorialMenuService
import explorviz.visualization.experiment.pageservices.TutorialMenuServiceAsync
import explorviz.visualization.interaction.Usertracking
import explorviz.visualization.landscapeexchange.LandscapeExchangeService
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync
import explorviz.visualization.login.LoginService
import explorviz.visualization.login.LoginServiceAsync
import explorviz.visualization.view.PageCaller
import explorviz.visualization.view.js.CenterElementJS
import explorviz.visualization.view.menu.ConfigurationMenuService
import explorviz.visualization.view.menu.ConfigurationMenuServiceAsync
import explorviz.visualization.view.menu.ExplorVizMenuService
import explorviz.visualization.view.menu.ExplorVizMenuServiceAsync
import java.util.logging.Level
import java.util.logging.Logger

class ExplorViz implements EntryPoint, PageControl {

	static Element view
	static Element spinner

	static RootPanel explorviz_ribbon
	static RootPanel tutorial_ribbon
	static RootPanel configuration_ribbon
	static RootPanel reset_landscape_ribbon

	public static String currentUserName

	AsyncCallback<String> callback

	val logger = Logger::getLogger("ExplorVizMainLogger")
	
	var static ExplorViz instance

	@Override
	override onModuleLoad() {
		GWT::setUncaughtExceptionHandler(
			[
				val message = if (it.cause != null) it.cause.message else it.message
				val stackTrace = if (it.cause != null)
						createStackStringFromThrowable(it.cause)
					else
						createStackStringFromThrowable(it)
				logger.log(Level::SEVERE, "Uncaught Error occured: " + message + " " + stackTrace)
			])

		view = RootPanel::get("view").element
		spinner = DOM::getElementById("spinner")
		
		instance = this

		val LoginServiceAsync loginService = GWT::create(typeof(LoginService))
		val endpoint = loginService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "loginservice"
		loginService.getCurrentUsername(new UsernameCallBack())

		explorviz_ribbon = RootPanel::get("explorviz_ribbon")
		tutorial_ribbon = RootPanel::get("tutorial_ribbon")
		configuration_ribbon = RootPanel::get("configuration_ribbon")
		reset_landscape_ribbon = RootPanel::get("reset_landscape")

		createExplorVizRibbonLink()
		createTutorialRibbonLink()
		createConfigurationRibbonLink()

		JSHelpers::registerResizeHandler()

		callFirstPage()
	}

	def static disableWebGL() {
		WebGLStart::disable
	}

	def static resizeHandler() {
		if (WebGLStart::explorVizVisible) {
			JSHelpers::hideAllButtonsAndDialogs
			disableWebGL()
			
			view.setInnerHTML("")
	
			Navigation::registerWebGLKeys()
			WebGLStart::initWebGL()
		}
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
	
	def public static toMainPage(){
		instance.tabSwitch(true, false, false)
		instance.callFirstPage()
		
	}

	def private callFirstPage() {
		callback = new PageCaller<String>(this)
		callback.onSuccess("explorviz")
	}

	def private createExplorVizRibbonLink() {
		val ExplorVizMenuServiceAsync explorvizService = GWT::create(typeof(ExplorVizMenuService))

		val endpoint = explorvizService as ServiceDefTarget
		val moduleRelativeURL = GWT::getModuleBaseURL() + "explorvizmenu"
		endpoint.serviceEntryPoint = moduleRelativeURL

		explorviz_ribbon.sinkEvents(Event::ONCLICK)
		explorviz_ribbon.addHandler(
			[
				tabSwitch(true, false, false)
				explorvizService.getPage(callback)
			], ClickEvent::getType())
	}
	
	private def tabSwitch(boolean explorviz, boolean tutorial, boolean configuration) {
		JSHelpers::hideAllButtonsAndDialogs
		disableWebGL()
		setView("")
		fadeInSpinner()
		
		if (explorviz)
			Usertracking::trackClickedExplorVizTab()
		if (tutorial)
			Usertracking::trackClickedTutorialTab()
		if (configuration)
			Usertracking::trackClickedConfigurationTab()
		
		explorviz_ribbon.element.parentElement.className = if (explorviz) "active" else ""
		tutorial_ribbon.element.parentElement.className = if (tutorial) "active" else ""
		configuration_ribbon.element.parentElement.className = if (configuration) "active" else ""
	}
	
	def private createTutorialRibbonLink() {
		val TutorialMenuServiceAsync tutorialService = GWT::create(typeof(TutorialMenuService))

		val endpoint = tutorialService as ServiceDefTarget
		val moduleRelativeURL = GWT::getModuleBaseURL() + "tutorialmenu"
		endpoint.serviceEntryPoint = moduleRelativeURL

		tutorial_ribbon.sinkEvents(Event::ONCLICK)
		tutorial_ribbon.addHandler(
			[
				tabSwitch(false, true, false)
				tutorialService.getPage(callback)
			], ClickEvent::getType())
	}
	
	def private createConfigurationRibbonLink() {
		val ConfigurationMenuServiceAsync configurationService = GWT::create(typeof(ConfigurationMenuService))
		val endpoint = configurationService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "configurationmenu"

		val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
		val endpointLandscape = landscapeExchangeService as ServiceDefTarget
		endpointLandscape.serviceEntryPoint = GWT::getModuleBaseURL() + "landscapeexchange"

		configuration_ribbon.sinkEvents(Event::ONCLICK)
		configuration_ribbon.addHandler(
			[
				tabSwitch(false, false, true)
				configurationService.getPage(callback)
			], ClickEvent::getType())

		reset_landscape_ribbon.sinkEvents(Event::ONCLICK)
		reset_landscape_ribbon.addHandler(
			[
				landscapeExchangeService.resetLandscape(new DummyCallBack());
			], ClickEvent::getType())
	}

	public override fadeInSpinner() {
		CenterElementJS::centerSpinner()
		spinner.style.display = Style.Display::BLOCK
	}

	public override fadeOutSpinner() {
		spinner.style.display = Style.Display::NONE
	}

	override setView(String result) {
		view.setInnerHTML(result)
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
			Browser::getDocument().getElementById("username").innerHTML = "Signed in as <b>" + ExplorViz.currentUserName +
				"</b> "

			val logoutA = Browser::getDocument().createAnchorElement
			logoutA.innerHTML = "(logout)"
			logoutA.className = "navbar-link"
			logoutA.id = "logout"

			val LoginServiceAsync loginService = GWT::create(typeof(LoginService))
			val endpoint = loginService as ServiceDefTarget
			endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "loginservice"
			logoutA.addEventListener("click",
				[
					loginService.logout(new LogoutCallBack)
				], false)

			Browser::getDocument().getElementById("username").appendChild(logoutA)
		}
	}
}
