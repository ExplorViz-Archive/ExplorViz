package explorviz.visualization.engine.navigation;

import com.google.gwt.user.client.Timer;

import explorviz.visualization.engine.picking.ObjectPicker;

public class MouseHoverDelayTimer extends Timer {

	private final int x;
	private final int y;
	private final int width;
	private final int height;

	@Override
	public void run() {
		ObjectPicker.handleMouseMove(x, y, width, height);
	}

	MouseHoverDelayTimer(final int x, final int y, final int width, final int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
