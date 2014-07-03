package explorviz.visualization.engine.navigation;

import com.google.gwt.user.client.Timer;

import explorviz.visualization.engine.picking.ObjectPicker;

public class MouseHoverDelayTimer extends Timer {

	private boolean myCanceled = true;
	private int x;
	private int y;
	private int width;
	private int height;

	@Override
	public void run() {
		if (!myCanceled) {
			ObjectPicker.handleMouseMove(getX(), getY(), getWidth(), getHeight());
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

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public boolean isMyCanceled() {
		return myCanceled;
	}

	public void setMyCanceled(final boolean myCanceled) {
		this.myCanceled = myCanceled;
	}
}
