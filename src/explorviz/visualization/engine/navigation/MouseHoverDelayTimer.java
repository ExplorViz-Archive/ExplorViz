package explorviz.visualization.engine.navigation;

import com.google.gwt.user.client.Timer;

import explorviz.visualization.engine.picking.ObjectPicker;

public class MouseHoverDelayTimer extends Timer {

	private boolean myCanceled = true;
	private int x;
	private int y;

	@Override
	public void run() {
		if (!myCanceled) {
			ObjectPicker.handleMouseMove(getX(), getY());
		}
	}

	public int getX() {
		return x;
	}

	public void setX(final int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(final int y) {
		this.y = y;
	}

	public boolean isMyCanceled() {
		return myCanceled;
	}

	public void setMyCanceled(final boolean myCanceled) {
		this.myCanceled = myCanceled;
	}
}
