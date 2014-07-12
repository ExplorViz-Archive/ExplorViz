package explorviz.visualization.engine.navigation;

public class TouchNavigationJS {
	public static native void register() /*-{
		hammertime = $wnd.jQuery("#view").hammer({})

		//		hammertime
		//				.bind(
		//						"tap",
		//						function(ev) {
		//							@explorviz.visualization.engine.navigation.Navigation::mouseDownHandler(IIII)(ev.gesture.srcEvent.clientX, ev.gesture.srcEvent.clientY, ev.gesture.srcEvent.srcElement.clientWidth, ev.gesture.srcEvent.srcElement.clientHeight);
		//							@explorviz.visualization.engine.navigation.Navigation::mouseUpHandler(IIIIZ)(ev.gesture.srcEvent.clientX, ev.gesture.srcEvent.clientY, ev.gesture.srcEvent.srcElement.clientWidth, ev.gesture.srcEvent.srcElement.clientHeight, false);
		//						});

		hammertime
				.bind(
						"doubletap",
						function(ev) {
							console.log(ev)
							if (ev.gesture.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::mouseDoubleClickHandler(IIII)(ev.gesture.srcEvent.offsetX, ev.gesture.srcEvent.offsetY, ev.target.clientWidth, ev.target.clientHeight);
							} else if (ev.gesture.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::mouseDoubleClickHandler(IIII)(ev.gesture.pointers[0].clientX, ev.gesture.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.clientWidth, ev.target.clientHeight);
							}
						});

		$wnd
				.jQuery("#view")
				.on(
						"contextmenu",
						function(ev) {
							if (ev.originalEvent.offsetY < ev.target.clientWidth
									- @explorviz.visualization.engine.main.WebGLStart::timeshiftHeight) {
								ev.preventDefault();
								@explorviz.visualization.engine.picking.ObjectPicker::handleRightClick(IIII)(ev.originalEvent.offsetX, ev.originalEvent.offsetY, ev.target.clientWidth, ev.target.clientHeight)
							}
						});

		hammertime
				.bind(
						"pinchin",
						function(ev) {
							@explorviz.visualization.engine.navigation.Navigation::mouseWheelHandler(I)(1);
						});

		hammertime
				.bind(
						"pinchout",
						function(ev) {
							@explorviz.visualization.engine.navigation.Navigation::mouseWheelHandler(I)(-1);
						});
		hammertime
				.bind(
						"press",
						function(ev) {
							if (ev.gesture.pointerType == "mouse") {
								if (ev.gesture.srcEvent.offsetY < ev.target.clientWidth
										- @explorviz.visualization.engine.main.WebGLStart::timeshiftHeight) {
									@explorviz.visualization.engine.picking.ObjectPicker::handleMouseMove(IIII)(ev.gesture.srcEvent.offsetX, ev.gesture.srcEvent.offsetY, ev.target.clientWidth, ev.target.clientHeight);
								}
							} else if (ev.gesture.pointerType == "touch") {
								if (ev.gesture.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight < ev.target.clientWidth
										- @explorviz.visualization.engine.main.WebGLStart::timeshiftHeight) {
									@explorviz.visualization.engine.picking.ObjectPicker::handleMouseMove(IIII)(ev.gesture.pointers[0].clientX, ev.gesture.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.clientWidth, ev.target.clientHeight);
								}
							}
						});

		hammertime
				.bind(
						"panstart",
						function(ev) {
							if (ev.gesture.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::mouseDownHandler(IIII)(ev.gesture.srcEvent.offsetX, ev.gesture.srcEvent.offsetY, ev.gesture.srcEvent.srcElement.clientWidth, ev.gesture.srcEvent.srcElement.clientHeight);
							} else if (ev.gesture.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::mouseDownHandler(IIII)(ev.gesture.pointers[0].clientX, ev.gesture.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.clientWidth, ev.target.clientHeight);
							}
						});
		hammertime
				.bind(
						"panmove",
						function(ev) {
							if (ev.gesture.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::panningHandler(IIII)(ev.gesture.srcEvent.offsetX, ev.gesture.srcEvent.offsetY, ev.gesture.srcEvent.srcElement.clientWidth, ev.gesture.srcEvent.srcElement.clientHeight);
							} else if (ev.gesture.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::panningHandler(IIII)(ev.gesture.pointers[0].clientX, ev.gesture.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.clientWidth, ev.target.clientHeight);
							}
						});
		hammertime
				.bind(
						"panend pancancel",
						function(ev) {
							if (ev.gesture.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::mouseUpHandler(IIII)(ev.gesture.srcEvent.offsetX, ev.gesture.srcEvent.offsetY, ev.gesture.srcEvent.srcElement.clientWidth, ev.gesture.srcEvent.srcElement.clientHeight);
							} else if (ev.gesture.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::mouseUpHandler(IIII)(ev.gesture.pointers[0].clientX, ev.gesture.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.clientWidth, ev.target.clientHeight);
							}
						});
	}-*/;

	public static native void deregister() /*-{
		hammertime.unbind("doubletap");
		hammertime.unbind("contextmenu");
	}-*/;
}
