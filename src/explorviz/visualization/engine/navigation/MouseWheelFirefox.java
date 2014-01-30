package explorviz.visualization.engine.navigation;

public class MouseWheelFirefox {
	public static native void addNativeMouseWheelListener() /*-{
		var scrollWheelHandler = function scrollWheelMove(e) {
			if ($wnd.event || $wnd.Event) {
				if (!e)
					e = $wnd.event;
				if (e.wheelDelta <= 0 || e.detail > 0) {
					@explorviz.visualization.engine.navigation.Camera::zoomOut()()
				} else {
					@explorviz.visualization.engine.navigation.Camera::zoomIn()()
				}
			}
		}
		$wnd.addEventListener("DOMMouseScroll", scrollWheelMove, false);
	}-*/;

	public static native void removeNativeMouseWheelListener() /*-{
//		$wnd.removeEventListener("DOMMouseScroll", scrollWheelHandler);
	}-*/;
}
