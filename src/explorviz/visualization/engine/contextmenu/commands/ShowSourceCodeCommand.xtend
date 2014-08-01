package explorviz.visualization.engine.contextmenu.commands

import com.google.gwt.user.client.Command
import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.shared.model.Clazz
import explorviz.visualization.codeviewer.CodeViewer
import explorviz.visualization.experiment.Experiment
import explorviz.visualization.engine.Logging

class ShowSourceCodeCommand implements Command {
	var Clazz currentClazz

	def setCurrentClazz(Clazz clazz) {
		currentClazz = clazz
	}

	override execute() {
		if (!Experiment::tutorial || Experiment.getStep.codeview) {
			if (Experiment::tutorial && Experiment.getStep.codeview) {
				Experiment.incStep();
			}
			PopupService::hidePopupMenus()

			var filePath = currentClazz.fullQualifiedName
			filePath = filePath.substring(0, filePath.lastIndexOf(".")).replaceAll("\\.", "/")

			CodeViewer::openDialog(currentClazz.parent.belongingApplication.name, filePath, currentClazz.name + ".java");
		} else {
			Logging.log("Debugausgabe: you can't do this now")
		}
	}
}
