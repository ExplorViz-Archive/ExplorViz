package explorviz.visualization.interaction;

import explorviz.shared.model.*;
import explorviz.shared.model.System;
import explorviz.shared.model.helper.CommunicationAppAccumulator;
import explorviz.shared.usertracking.records.*;
import explorviz.shared.usertracking.records.application.*;
import explorviz.shared.usertracking.records.codeviewer.CodeviewerOpenFileRecord;
import explorviz.shared.usertracking.records.codeviewer.CodeviewerRecord;
import explorviz.shared.usertracking.records.landscape.*;
import explorviz.visualization.engine.usertracking.UsertrackingService;
import explorviz.visualization.experiment.Experiment;

/**
 * @author Maria Kosche
 *
 */
public class Usertracking {
	public static void trackApplicationDoubleClick(final Application app) {
		if (Experiment.experiment) {
			final ApplicationRecord record = new ApplicationOpenSystemLevelRecord(app);
			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackApplicationRightClick(final Application app) {
		if (Experiment.experiment) {
			final ApplicationRecord record = new ApplicationOpenPopupMenuRecord(app);
			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackClazzRightClick(final Clazz clazz) {
		if (Experiment.experiment) {
			final ClazzRecord record = new ClazzOpenPopupMenuRecord(clazz);
			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackCodeviewerCode(final String project, final String filepath,
			final String filename) {
		if (Experiment.experiment) {
			final CodeviewerRecord record = new CodeviewerOpenFileRecord(project, filepath,
					filename);
			UsertrackingService.putUsertrackingRecord(record);
		}

	}

	public static void trackCommunicationClick(final CommunicationAppAccumulator comclazz) {
		if (Experiment.experiment) {
			final CommunicationClazzRecord record = new CommunicationClazzClickRecord();
			UsertrackingService.putUsertrackingRecord(record);
		}

	}

	public static void trackComponentClick(final Component component) {
		if (Experiment.experiment) {
			final ComponentRecord record = (component.isHighlighted()) ? new ComponentHighlightRecord(
					component) : new ComponentUnhighlightRecord(component);

					UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackComponentDoubleClick(final Component compo) {
		if (Experiment.experiment) {
			final ComponentRecord record = (compo.isOpened()) ? new ComponentCloseRecord(compo)
			: new ComponentOpenRecord(compo);

			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackDraw3DNodeUnhighlightAll() {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new Draw3DNodeUnhighlightAllRecord());
		}
	}

	public static void trackComponentOpenAll() {
		if (Experiment.experiment) {
			final ComponentOpenAllRecord record = new ComponentOpenAllRecord();

			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackComponentMouseHover(final Component component) {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new ComponentInformationRecord(component));
		}
	}

	public static void trackComponentRightClick(final Component compo) {
		if (Experiment.experiment) {
			final ComponentRecord record = new ComponentOpenPopupMenuRecord(compo);
			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackNodeClick(final Node node) {
		if (Experiment.experiment) {
			final NodeRecord record = new NodeClickRecord(node);
			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackNodeRightClick(final Node node) {
		if (Experiment.experiment) {
			final NodeRecord record = new NodeOpenPopupMenuRecord(node);
			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackNodeGroupDoubleClick(final NodeGroup nodeGroup) {
		if (Experiment.experiment) {
			final NodeGroupRecord record = (nodeGroup.isOpened()) ? new NodeGroupCloseRecord(
					nodeGroup) : new NodeGroupOpenRecord(nodeGroup);

					UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackSystemDoubleClick(final System system) {
		if (Experiment.experiment) {
			final SystemRecord record = (system.isOpened()) ? new SystemCloseRecord(system)
			: new SystemOpenRecord(system);

			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackCameraMovedX(final float newX) {
		if (Experiment.experiment) {
			// UsertrackingService.putUsertrackingRecord(new
			// CameraMovedXRecord(newX));
		}
	}

	public static void trackCameraMovedY(final float newY) {
		if (Experiment.experiment) {
			// UsertrackingService.putUsertrackingRecord(new
			// CameraMovedYRecord(newY));
		}
	}

	public static void trackCameraMovedUp(final float newCameraY) {
		if (Experiment.experiment) {
			// UsertrackingService.putUsertrackingRecord(new
			// CameraMovedUpRecord(newCameraY));
		}
	}

	public static void trackCameraMovedDown(final float newCameraY) {
		if (Experiment.experiment) {
			// UsertrackingService.putUsertrackingRecord(new
			// CameraMovedDownRecord(newCameraY));
		}
	}

	public static void trackCameraMovedLeft(final float newCameraX) {
		if (Experiment.experiment) {
			// UsertrackingService.putUsertrackingRecord(new
			// CameraMovedLeftRecord(newCameraX));
		}
	}

	public static void trackCameraMovedRight(final float newCameraX) {
		if (Experiment.experiment) {
			// UsertrackingService.putUsertrackingRecord(new
			// CameraMovedRightRecord(newCameraX));
		}
	}

	public static void trackCameraZoomedOut(final float newCameraZ) {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new CameraZoomedOutRecord(newCameraZ));
		}
	}

	public static void trackCameraZoomedIn(final float newCameraZ) {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new CameraZoomedInRecord(newCameraZ));
		}
	}

	public static void trackBackToLandscape() {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new BackToLandscapeRecord());
		}
	}

	public static void trackExport3DModel(final Application application) {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new Export3DModelRecord(application));
		}
	}

	public static void trackContinuedLandscapeExchange() {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new ContinuedLandscapeExchangeRecord());
		}
	}

	public static void trackStoppedLandscapeExchange(final String timestamp) {
		if (Experiment.experiment) {
			UsertrackingService
			.putUsertrackingRecord(new StoppedLandscapeExchangeRecord(timestamp));
		}
	}

	public static void trackFetchedSpecifcLandscape(final String timestamp) {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new FetchedSpecifcLandscapeRecord(timestamp));
		}
	}

	public static void trackClickedExplorVizTab() {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new SwitchedToExplorVizTabRecord());
		}
	}

	public static void trackClickedTutorialTab() {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new SwitchedToTutorialTabRecord());
		}
	}

	public static void trackClickedConfigurationTab() {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new SwitchedToConfigurationTabRecord());
		}
	}

	public static void trackClazzClick(final Clazz clazz) {
		if (Experiment.experiment) {
			final ClazzRecord record = (clazz.isHighlighted()) ? new ClazzHighlightRecord(clazz)
			: new ClazzUnhighlightRecord(clazz);

			UsertrackingService.putUsertrackingRecord(record);
		}
	}

	public static void trackClazzMouseHover(final Clazz clazz) {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new ClazzInformationRecord(clazz));
		}
	}

	public static void trackClazzDoubleClick(final Clazz clazz) {
		if (Experiment.experiment) {
			UsertrackingService.putUsertrackingRecord(new ClazzTriedDoubleClickRecord(clazz));
		}
	}

	public static void trackCommunicationMouseHover(final CommunicationAppAccumulator accumulator) {
		if (Experiment.experiment) {
			UsertrackingService
			.putUsertrackingRecord(new CommunicationClazzHoverRecord(accumulator));
		}
	}

}
