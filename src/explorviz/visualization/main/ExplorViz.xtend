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
import explorviz.shared.auth.User
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.pageservices.TutorialMenuService
import explorviz.visualization.experiment.pageservices.TutorialMenuServiceAsync
import explorviz.visualization.interaction.Usertracking
import explorviz.visualization.landscapeexchange.LandscapeExchangeService
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync
import explorviz.visualization.login.LoginService
import explorviz.visualization.login.LoginServiceAsync
import explorviz.visualization.services.AuthorizationService
import explorviz.visualization.view.PageCaller
import explorviz.visualization.view.menu.ConfigurationMenuService
import explorviz.visualization.view.menu.ConfigurationMenuServiceAsync
import explorviz.visualization.view.menu.ExplorVizMenuService
import explorviz.visualization.view.menu.ExplorVizMenuServiceAsync
import java.util.logging.Level
import java.util.logging.Logger
import explorviz.visualization.experiment.pageservices.EditQuestionsMenuService
import explorviz.visualization.experiment.pageservices.EditQuestionsMenuServiceAsync

class ExplorViz implements EntryPoint, PageControl {

	static Element view
	static Element spinner

	static RootPanel explorviz_ribbon
	static RootPanel tutorial_ribbon
	protected static RootPanel configuration_ribbon
	protected static RootPanel question_ribbon
	protected static RootPanel reset_landscape_ribbon
	protected static RootPanel download_answers_ribbon

	public static User currentUser

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
		requestCurrentUser()

		view = RootPanel::get("view").element
		spinner = DOM::getElementById("spinner")

		instance = this


		explorviz_ribbon = RootPanel::get("explorviz_ribbon")
		tutorial_ribbon = RootPanel::get("tutorial_ribbon")

		createExplorVizRibbonLink()
		createTutorialRibbonLink()

		JSHelpers::registerResizeHandler()

//		callFirstPage()
	}
	
	def void requestCurrentUser() {
		val LoginServiceAsync loginService = GWT::create(typeof(LoginService))
		val endpoint = loginService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "loginservice"
		loginService.getCurrentUser(new UserCallBack(this))
	}

	def static void disableWebGL() {
		WebGLStart::disable
	}

	def static void resizeHandler() {
		if (WebGLStart::explorVizVisible) {
			JSHelpers::hideAllButtonsAndDialogs
			disableWebGL()

			view.setInnerHTML("")

			WebGLStart::initWebGL()
			Navigation::registerWebGLKeys()
		}
	}

	def static String createStackStringFromThrowable(Throwable t) {
		var stack = ""
		var i = 0
		while (i < t.stackTrace.length) {
			stack = stack + "\n\t" + (t.stackTrace.get(i))
			i = i + 1
		}
		stack
	}

	def public static void toMainPage() {
		instance.tabSwitch(true, false, false, false)
		instance.callFirstPage()

	}

	def protected void callFirstPage() {
		callback = new PageCaller<String>(this)
		if(currentUser != null && currentUser.firstLogin){
			tabSwitch(false, true, false, false)
			callback.onSuccess("tutorial")
		}else{
			callback.onSuccess("explorviz")
		}
	}

	def private void createExplorVizRibbonLink() {
		val ExplorVizMenuServiceAsync explorvizService = GWT::create(typeof(ExplorVizMenuService))

		val endpoint = explorvizService as ServiceDefTarget
		val moduleRelativeURL = GWT::getModuleBaseURL() + "explorvizmenu"
		endpoint.serviceEntryPoint = moduleRelativeURL

		explorviz_ribbon.sinkEvents(Event::ONCLICK)
		explorviz_ribbon.addHandler(
			[
				tabSwitch(true, false, false, false)
				explorvizService.getPage(callback)
			], ClickEvent::getType())
	}

	private def void tabSwitch(boolean explorviz, boolean tutorial, boolean configuration, boolean questions) {
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
		if (AuthorizationService::currentUserHasRole("admin")) {
			configuration_ribbon.element.parentElement.className = if (configuration) "active" else ""
			question_ribbon.element.parentElement.className = if (questions) "active" else ""
		}
	}

	def private void createTutorialRibbonLink() {
		val TutorialMenuServiceAsync tutorialService = GWT::create(typeof(TutorialMenuService))

		val endpoint = tutorialService as ServiceDefTarget
		val moduleRelativeURL = GWT::getModuleBaseURL() + "tutorialmenu"
		endpoint.serviceEntryPoint = moduleRelativeURL

		tutorial_ribbon.sinkEvents(Event::ONCLICK)
		tutorial_ribbon.addHandler(
			[
				tabSwitch(false, true, false, false)
				tutorialService.getPage(callback)
			], ClickEvent::getType())
	}

	protected def void createConfigurationRibbonLink() {
		val ConfigurationMenuServiceAsync configurationService = GWT::create(typeof(ConfigurationMenuService))
		val endpoint = configurationService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "configurationmenu"

		val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
		val endpointLandscape = landscapeExchangeService as ServiceDefTarget
		endpointLandscape.serviceEntryPoint = GWT::getModuleBaseURL() + "landscapeexchange"
		
		val EditQuestionsMenuServiceAsync editQuestionsService = GWT::create(typeof(EditQuestionsMenuService))
		val endpointQuestions = editQuestionsService as ServiceDefTarget
		endpointQuestions.serviceEntryPoint = GWT::getModuleBaseURL() + "editquestionsmenu"

		configuration_ribbon.sinkEvents(Event::ONCLICK)
		configuration_ribbon.addHandler(
			[
				tabSwitch(false, false, true, false)
				configurationService.getPage(callback)
			], ClickEvent::getType())

		reset_landscape_ribbon.sinkEvents(Event::ONCLICK)
		reset_landscape_ribbon.addHandler(
			[
				landscapeExchangeService.resetLandscape(new DummyCallBack());
			], ClickEvent::getType())
			
		download_answers_ribbon.sinkEvents(Event::ONCLICK)
		download_answers_ribbon.addHandler(
			[
				Questionnaire::downloadAnswers()
			], ClickEvent::getType())
			
		question_ribbon.sinkEvents(Event::ONCLICK)	
		question_ribbon.addHandler(
			[
				tabSwitch(false, false, false, true)
				editQuestionsService.getPage(callback)
			], ClickEvent::getType())
	}

	public override fadeInSpinner() {
		JSHelpers::centerSpinner()
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

class UserCallBack implements AsyncCallback<User> {
	ExplorViz pageinstance
	
	new(ExplorViz pageinstance) {
		this.pageinstance = pageinstance;
	}
	
	override onFailure(Throwable caught) {
	}

	override onSuccess(User result) {
		ExplorViz.currentUser = result
		
		if (AuthorizationService::currentUserHasRole("admin")) {
			JSHelpers::showElementById("administration_ribbon")
			ExplorViz.reset_landscape_ribbon = RootPanel::get("reset_landscape")
			ExplorViz.download_answers_ribbon = RootPanel::get("download_answers")
			ExplorViz.configuration_ribbon = RootPanel::get("configuration_ribbon")
			ExplorViz.question_ribbon = RootPanel::get("question_ribbon")

			pageinstance.createConfigurationRibbonLink()
		}
		
		val currentUsername = result.username
		if (currentUsername != null && currentUsername != "") {
			Browser::getDocument().getElementById("username").innerHTML = "Signed in as <b>" + currentUsername +
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
			
			pageinstance.callFirstPage()
		}
	}
}
