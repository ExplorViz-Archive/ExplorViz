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
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.experiment.Questionnaire
import explorviz.visualization.experiment.TutorialJS
import explorviz.visualization.experiment.pageservices.TutorialMenuService
import explorviz.visualization.experiment.pageservices.TutorialMenuServiceAsync
import explorviz.visualization.interaction.Usertracking
import explorviz.visualization.landscapeexchange.LandscapeExchangeService
import explorviz.visualization.landscapeexchange.LandscapeExchangeServiceAsync
import explorviz.visualization.login.LoginService
import explorviz.visualization.login.LoginServiceAsync
import explorviz.visualization.services.AuthorizationService
import explorviz.visualization.view.PageCaller
import java.util.logging.Level
import java.util.logging.Logger

import static explorviz.visualization.main.ExplorViz.*

class ExplorViz implements EntryPoint, PageControl {

	static Element view
	static Element spinner

	static RootPanel explorviz_ribbon
	static RootPanel tutorial_ribbon
	protected static RootPanel exp_tools_ribbon
	protected static RootPanel configuration_ribbon
	protected static RootPanel manage_users_and_roles_ribbon
	protected static RootPanel question_ribbon
	protected static RootPanel reset_landscape_ribbon
	protected static RootPanel download_answers_ribbon
	protected static RootPanel modeling_ribbon

	public static User currentUser

	PageCaller callback

	val logger = Logger::getLogger("ExplorVizMainLogger")

	var static ExplorViz instance
	public var boolean controlGroupActive

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

		instance = this

		spinner = DOM::getElementById("spinner")

		controlGroupActive = RootPanel::get("extravisQuestionnaire") != null

		view = RootPanel::get("view").element

		explorviz_ribbon = RootPanel::get("explorviz_ribbon")
		tutorial_ribbon = RootPanel::get("tutorial_ribbon")

		createExplorVizRibbonLink()
		createTutorialRibbonLink()

		JSHelpers::registerResizeHandler()

	// callFirstPage()
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
			if (Experiment::experiment && !Experiment::tutorial) {
				Questionnaire::startQuestions()
			} else if (Experiment::tutorial) {
				TutorialJS::showTutorialDialog()
			}
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
		instance.tabSwitch(true, false, false, false, false, false, false)
		WebGLStart::setModeling(false)
		instance.callback.showExplorViz
	}

	def protected void callFirstPage() {
		callback = new PageCaller(this)
		if (currentUser != null && currentUser.firstLogin) {
			tabSwitch(false, true, false, false, false, false, false)
			WebGLStart::setModeling(false)
			callback.showTutorial
		} else {
			WebGLStart::setModeling(false)
			callback.showExplorViz
		}
	}

	def private void createExplorVizRibbonLink() {
		explorviz_ribbon.sinkEvents(Event::ONCLICK)
		explorviz_ribbon.addHandler(
			[
			tabSwitch(true, false, false, false, false, false, false)
			WebGLStart::setModeling(false)
			callback.showExplorViz
		], ClickEvent::getType())
	}

	private def void tabSwitch(boolean explorviz, boolean tutorial, boolean configuration, boolean questions,
		boolean manage_users, boolean modeling, boolean exp_tools) {
		JSHelpers::hideAllButtonsAndDialogs
		disableWebGL()
		setView("")

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
			manage_users_and_roles_ribbon.element.parentElement.className = if (manage_users) "active" else ""
			question_ribbon.element.parentElement.className = if (questions) "active" else ""
			modeling_ribbon.element.parentElement.className = if (modeling) "active" else ""
			exp_tools_ribbon.element.parentElement.className = if (exp_tools) "active" else ""
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
			tabSwitch(false, true, false, false, false, false, false)
			WebGLStart::setModeling(false)
			callback.showTutorial()
		], ClickEvent::getType())
	}

	protected def void createConfigurationRibbonLink() {
		val LandscapeExchangeServiceAsync landscapeExchangeService = GWT::create(typeof(LandscapeExchangeService))
		val endpointLandscape = landscapeExchangeService as ServiceDefTarget
		endpointLandscape.serviceEntryPoint = GWT::getModuleBaseURL() + "landscapeexchange"

		configuration_ribbon.sinkEvents(Event::ONCLICK)
		configuration_ribbon.addHandler(
			[
			tabSwitch(false, false, true, false, false, false, false)
			callback.showConfiguration()
		], ClickEvent::getType())

		manage_users_and_roles_ribbon.sinkEvents(Event::ONCLICK)
		manage_users_and_roles_ribbon.addHandler(
			[
			tabSwitch(false, false, false, false, true, false, false)
			callback.showManageUsersAndRoles
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
			tabSwitch(false, false, false, true, false, false, false)
			callback.showEditQuestions
		], ClickEvent::getType())

		modeling_ribbon.sinkEvents(Event::ONCLICK)
		modeling_ribbon.addHandler(
			[
			tabSwitch(false, false, false, false, false, true, false)
			WebGLStart::setModeling(true)
			callback.showExplorViz
		], ClickEvent::getType())
	}

	protected def void createExpToolsRibbonLink() {
		exp_tools_ribbon.sinkEvents(Event::ONCLICK)
		exp_tools_ribbon.addHandler(
			[
			tabSwitch(false, false, false, false, false, false, true)
			WebGLStart::setModeling(false)
			callback.showExpTools
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

	static def isControlGroupActive() {
		return instance.controlGroupActive
	}

	def static getPageCaller() {
		return instance.callback
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
			JSHelpers::showElementById("exp_tools_ribbon")
			ExplorViz.reset_landscape_ribbon = RootPanel::get("reset_landscape")
			ExplorViz.download_answers_ribbon = RootPanel::get("download_answers")
			ExplorViz.configuration_ribbon = RootPanel::get("configuration_ribbon")
			ExplorViz.manage_users_and_roles_ribbon = RootPanel::get("manage_users_and_roles_ribbon")
			ExplorViz.question_ribbon = RootPanel::get("question_ribbon")
			ExplorViz.modeling_ribbon = RootPanel::get("modeling_ribbon")
			ExplorViz.exp_tools_ribbon = RootPanel::get("exp_tools_ribbon")

			pageinstance.createConfigurationRibbonLink()
			pageinstance.createExpToolsRibbonLink()
		}

		val currentUsername = result.username

		if (currentUsername != null && currentUsername != "") {
			Browser::getDocument().getElementById("username").innerHTML = "Signed in as <b>" + currentUsername + "</b> "

			val logoutA = Browser::getDocument().createAnchorElement
			logoutA.innerHTML = "(logout)"
			logoutA.className = "navbar-link"
			logoutA.style.marginRight = "10px"
			logoutA.id = "logout"

			val LoginServiceAsync loginService = GWT::create(typeof(LoginService))
			val endpoint = loginService as ServiceDefTarget
			endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "loginservice"
			logoutA.addEventListener("click", [
				loginService.logout(new LogoutCallBack)
			], false)

			Browser::getDocument().getElementById("username").appendChild(logoutA)

			pageinstance.callFirstPage()
		}
	}
}
