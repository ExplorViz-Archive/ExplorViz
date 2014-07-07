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

public class Usertracking {
	public static void trackApplicationDoubleClick(final Application app) {
		final ApplicationRecord record = new ApplicationOpenSystemLevelRecord(app);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackApplicationRightClick(final Application app) {
		final ApplicationRecord record = new ApplicationOpenPopupMenuRecord(app);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackClazzRightClick(final Clazz clazz) {
		final ClazzRecord record = new ClazzOpenPopupMenuRecord(clazz);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackCodeviewerCode(final String project, final String filepath,
			final String filename) {
		final CodeviewerRecord record = new CodeviewerOpenFileRecord(project, filepath, filename);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackCommunicationClick(final CommunicationAppAccumulator comclazz) {
		final CommunicationClazzRecord record = new CommunicationClazzClickRecord();
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackComponentDoubleClick(final Component compo) {
		final ComponentRecord record = (compo.isOpened()) ? new ComponentCloseRecord(compo)
				: new ComponentOpenRecord(compo);

		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackComponentRightClick(final Component compo) {
		final ComponentRecord record = new ComponentOpenPopupMenuRecord(compo);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackNodeClick(final Node node) {
		final NodeRecord record = new NodeClickRecord(node);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackNodeRightClick(final Node node) {
		final NodeRecord record = new NodeOpenPopupMenuRecord(node);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackNodeGroupDoubleClick(final NodeGroup nodeGroup) {
		final NodeGroupRecord record = (nodeGroup.isOpened()) ? new NodeGroupCloseRecord(nodeGroup)
				: new NodeGroupOpenRecord(nodeGroup);

		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackSystemDoubleClick(final System system) {
		final SystemRecord record = (system.isOpened()) ? new SystemCloseRecord(system)
				: new SystemOpenRecord(system);

		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackCameraMovedX(final float newX) {
		// UsertrackingService.putUsertrackingRecord(new
		// CameraMovedXRecord(newX));
	}

	public static void trackCameraMovedY(final float newY) {
		// UsertrackingService.putUsertrackingRecord(new
		// CameraMovedYRecord(newY));
	}

	public static void trackCameraMovedUp(final float newCameraY) {
		// UsertrackingService.putUsertrackingRecord(new
		// CameraMovedUpRecord(newCameraY));
	}

	public static void trackCameraMovedDown(final float newCameraY) {
		// UsertrackingService.putUsertrackingRecord(new
		// CameraMovedDownRecord(newCameraY));
	}

	public static void trackCameraMovedLeft(final float newCameraX) {
		// UsertrackingService.putUsertrackingRecord(new
		// CameraMovedLeftRecord(newCameraX));
	}

	public static void trackCameraMovedRight(final float newCameraX) {
		// UsertrackingService.putUsertrackingRecord(new
		// CameraMovedRightRecord(newCameraX));
	}

	public static void trackCameraZoomedOut(final float newCameraZ) {
		// UsertrackingService.putUsertrackingRecord(new
		// CameraZoomedOutRecord(newCameraZ));
	}

	public static void trackCameraZoomedIn(final float newCameraZ) {
		// UsertrackingService.putUsertrackingRecord(new
		// CameraZoomedInRecord(newCameraZ));
	}

	public static void trackBackToLandscape() {
		UsertrackingService.putUsertrackingRecord(new BackToLandscapeRecord());
	}

	public static void trackExport3DModel(final Application application) {
		UsertrackingService.putUsertrackingRecord(new Export3DModelRecord(application));
	}

	public static void trackContinuedLandscapeExchange() {
		UsertrackingService.putUsertrackingRecord(new ContinuedLandscapeExchangeRecord());
	}

	public static void trackStoppedLandscapeExchange(final String timestamp) {
		UsertrackingService.putUsertrackingRecord(new StoppedLandscapeExchangeRecord(timestamp));
	}

	public static void trackFetchedSpecifcLandscape(final String timestamp) {
		UsertrackingService.putUsertrackingRecord(new FetchedSpecifcLandscapeRecord(timestamp));
	}

	public static void trackClickedExplorVizTab() {
		UsertrackingService.putUsertrackingRecord(new SwitchedToExplorVizTabRecord());
	}

	public static void trackClickedTutorialTab() {
		UsertrackingService.putUsertrackingRecord(new SwitchedToTutorialTabRecord());
	}

	public static void trackClickedConfigurationTab() {
		UsertrackingService.putUsertrackingRecord(new SwitchedToConfigurationTabRecord());
	}

	public static void trackComponentClick(final Component component) {
	}

	public static void trackComponentMouseHover(final Component component) {
	}

	public static void trackClazzClick(final Clazz clazz) {
	}

	public static void trackClazzMouseHover(final Clazz clazz) {
	}

	public static void trackClazzDoubleClick(final Clazz clazz) {
	}

	public static void trackCommunicationMouseHover(final CommunicationAppAccumulator accumulator) {
	}

}
