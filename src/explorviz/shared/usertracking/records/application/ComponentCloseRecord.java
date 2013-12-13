package explorviz.shared.usertracking.records.application;

import explorviz.visualization.model.ComponentClientSide;

public class ComponentCloseRecord extends ComponentRecord {
	protected ComponentCloseRecord() {
	}

	public ComponentCloseRecord(final ComponentClientSide compo) {
		super(compo);
	}
}
