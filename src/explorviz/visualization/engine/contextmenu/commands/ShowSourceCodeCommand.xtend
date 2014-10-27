package explorviz.visualization.engine.contextmenu.commands

import explorviz.visualization.engine.contextmenu.PopupService
import explorviz.visualization.codeviewer.CodeViewer
import explorviz.visualization.experiment.Experiment

class ShowSourceCodeCommand extends ClazzCommand {
	override execute() {
		if (!Experiment::tutorial || Experiment.getStep.codeview) {
			if (Experiment::tutorial && Experiment.getStep.codeview) {
				Experiment.incStep();
			}
			PopupService::hidePopupMenus()

			var filePath = currentClazz.fullQualifiedName
			filePath = filePath.substring(0, filePath.lastIndexOf(".")).replaceAll("\\.", "/")

			CodeViewer::openDialog(currentClazz.parent.belongingApplication.name, filePath, currentClazz.name + ".java");
		}
	}

}
