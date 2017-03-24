package explorviz.visualization.view

import explorviz.visualization.engine.navigation.Navigation
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.main.JSHelpers
import explorviz.visualization.main.PageControl
import explorviz.visualization.experiment.tools.ExperimentTools
import explorviz.visualization.experiment.services.ConfigurationServiceAsync
import com.google.gwt.user.client.rpc.ServiceDefTarget
import explorviz.visualization.experiment.services.ConfigurationService
import com.google.gwt.core.client.GWT
import explorviz.visualization.experiment.callbacks.VoidCallback
import com.google.gwt.user.client.rpc.AsyncCallback

class ManageUsersAndRolesPage implements IPage {

	override render(PageControl pageControl) {
		Navigation::deregisterWebGLKeys()
		JSHelpers::hideAllButtonsAndDialogs()
		getUsers()

		pageControl.setView('''
					<div style="width:300px; margin:0 auto">
						<form style="display: inline-block; text-align: center;" class='form' role='form' id='addUserForm'>
							<div class='form-group' style='display:none'>
								<label for='username'>Username:</label>
								<input type='text' disabled></input>
							</div>
						</form>
						<button id="addUser" type="button" class="btn btn-default btn-sm">
						<span class="glyphicon glyphicon-floppy-disk"></span> Add 10 new ICSA study users</button>
					</div>
					<br/>
					<br/>
					<div id="users" class="container" style="width:300px;height:80%;overflow:auto;">
					</div>									
					'''.toString())

		ManageUsersAndRolesPageJS::init()

		Experiment::tutorial = false
		ExperimentTools::toolsModeActive = false
	}

	static def void addUserForm(String userForm) {
		val ConfigurationServiceAsync configService = GWT::create(typeof(ConfigurationService))
		val endpoint = configService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "configurationservice"
		configService.createUsersForICSAStudy(new VoidCallback());
	}

	def getUsers() {
		val ConfigurationServiceAsync configService = GWT::create(typeof(ConfigurationService))
		val endpoint = configService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "configurationservice"
		configService.getUsers(new UsersCallback());
	}
}

class UsersCallback implements AsyncCallback<String[]> {

	override onFailure(Throwable caught) {
	}

	override onSuccess(String[] result) {
		ManageUsersAndRolesPageJS::fillUsers(result)
	}
}
