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

class ManageUsersAndRolesPage implements IPage {

	override render(PageControl pageControl) {
		Navigation::deregisterWebGLKeys()
		JSHelpers::hideAllButtonsAndDialogs()

		pageControl.setView(
			'''<div style="width:300px; margin:0 auto"><form style="display: inline-block; text-align: center;" class='form' role='form' id='addUserForm'>
					<div class='form-group'>
					<label for='username'>Username:</label> <input type='text'></input>
					</div></form></br>
						<button id="addUser" type="button" class="btn btn-default btn-sm">
		<span class="glyphicon glyphicon-floppy-disk"></span> Add User</button></div>'''.
				toString())

		ManageUsersAndRolesPageJS::init()

		Experiment::tutorial = false
		ExperimentTools::toolsModeActive = false
	}

	static def void addUserForm(String userForm) {
		var String[] userFormList = userForm.split("&")
		var String username = userFormList.get(0).substring("username=".length);	
		val ConfigurationServiceAsync configService = GWT::create(typeof(ConfigurationService))
		val endpoint = configService as ServiceDefTarget
		endpoint.serviceEntryPoint = GWT::getModuleBaseURL() + "configurationservice"
		configService.createUser(username, new VoidCallback());
	}
}
