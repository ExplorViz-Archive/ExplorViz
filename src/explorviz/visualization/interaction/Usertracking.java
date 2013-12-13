package explorviz.visualization.interaction;

import explorviz.shared.usertracking.records.application.*;
import explorviz.shared.usertracking.records.codeviewer.CodeviewerOpenFileRecord;
import explorviz.shared.usertracking.records.codeviewer.CodeviewerRecord;
import explorviz.shared.usertracking.records.landscape.*;
import explorviz.visualization.engine.usertracking.UsertrackingService;
import explorviz.visualization.model.*;

public class Usertracking {
	public static void trackApplicationDoubleClick(final ApplicationClientSide app) {
		final ApplicationRecord record = new ApplicationOpenSystemLevelRecord(app);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackApplicationRightClick(final ApplicationClientSide app) {
		final ApplicationRecord record = new ApplicationOpenPopupMenuRecord(app);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackClazzRightClick(final ClazzClientSide clazz) {
		final ClazzRecord record = new ClazzOpenPopupMenuRecord(clazz);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackCodeviewerCode(final String project, final String filepath,
			final String filename) {
		final CodeviewerRecord record = new CodeviewerOpenFileRecord(project, filepath, filename);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackCommunicationClick(final CommunicationClazzClientSide comclazz) {
		final CommunicationClazzRecord record = new CommunicationClazzClickRecord();
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackComponentDoubleClick(final ComponentClientSide compo) {
		final ComponentRecord record = (compo.isOpened()) ? new ComponentCloseRecord(compo)
				: new ComponentOpenRecord(compo);

		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackComponentRightClick(final ComponentClientSide compo) {
		final ComponentRecord record = new ComponentOpenPopupMenuRecord(compo);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackNodeClick(final NodeClientSide node) {
		final NodeRecord record = new NodeClickRecord(node);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackNodeRightClick(final NodeClientSide node) {
		final NodeRecord record = new NodeOpenPopupMenuRecord(node);
		UsertrackingService.putUsertrackingRecord(record);
	}

	public static void trackNodeGroupDoubleClick(final NodeGroupClientSide nodeGroup) {
		final NodeGroupRecord record = (nodeGroup.isOpened()) ? new NodeGroupCloseRecord(nodeGroup)
				: new NodeGroupOpenRecord(nodeGroup);

		UsertrackingService.putUsertrackingRecord(record);
	}
}
