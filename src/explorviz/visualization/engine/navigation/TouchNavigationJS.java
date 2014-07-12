package explorviz.visualization.engine.navigation;

public class TouchNavigationJS {
	public static native void register() /*-{
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
		
		var hammertime = $wnd.jQuery().newHammerManager($doc.getElementById("view"),{});

		
		var tapHammer = $wnd.jQuery().newHammerTap({event: 'tap'});
		var doubleTapHammer = $wnd.jQuery().newHammerTap({event: 'doubleTap', taps: 2 });
		var panHammer = $wnd.jQuery().newHammerPan({});
		var pressHammer = $wnd.jQuery().newHammerPress({});
		var pinchHammer = $wnd.jQuery().newHammerPinch({enable: true});
		
		hammertime.add([doubleTapHammer, tapHammer, panHammer, pressHammer, pinchHammer]);
		doubleTapHammer.recognizeWith(tapHammer);
		tapHammer.requireFailure(doubleTapHammer);
		
		hammertime
				.on(
						"tap",
						function(ev) {
							if (ev.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::mouseSingleClickHandler(IIII)(ev.srcEvent.offsetX, ev.srcEvent.offsetY, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							} else if (ev.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::mouseSingleClickHandler(IIII)(ev.pointers[0].clientX, ev.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							}
						});

		hammertime
				.on(
						"doubleTap",
						function(ev) {
							console.log(ev)
							if (ev.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::mouseDoubleClickHandler(IIII)(ev.srcEvent.offsetX, ev.srcEvent.offsetY, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							} else if (ev.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::mouseDoubleClickHandler(IIII)(ev.pointers[0].clientX, ev.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							}
						});

		hammertime
				.on(
						"pinchin",
						function(ev) {
							@explorviz.visualization.engine.navigation.Navigation::mouseWheelHandler(I)(1);
						});

		hammertime
				.on(
						"pinchout",
						function(ev) {
							@explorviz.visualization.engine.navigation.Navigation::mouseWheelHandler(I)(-1);
						});
		hammertime
				.on(
						"press",
						function(ev) {
							if (ev.pointerType == "mouse") {
								if (ev.srcEvent.offsetY < ev.target.parentElement.parentElement.clientWidth
										- @explorviz.visualization.engine.main.WebGLStart::timeshiftHeight) {
									@explorviz.visualization.engine.picking.ObjectPicker::handleMouseMove(IIII)(ev.srcEvent.offsetX, ev.srcEvent.offsetY, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
								}
							} else if (ev.pointerType == "touch") {
								if (ev.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight < ev.target.parentElement.parentElement.clientWidth
										- @explorviz.visualization.engine.main.WebGLStart::timeshiftHeight) {
									@explorviz.visualization.engine.picking.ObjectPicker::handleMouseMove(IIII)(ev.pointers[0].clientX, ev.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
								}
							}
						});

		hammertime
				.on(
						"panstart",
						function(ev) {
							if (ev.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::mouseDownHandler(IIII)(ev.srcEvent.offsetX, ev.srcEvent.offsetY, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							} else if (ev.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::mouseDownHandler(IIII)(ev.pointers[0].clientX, ev.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							}
						});
		hammertime
				.on(
						"panmove",
						function(ev) {
							if (ev.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::panningHandler(IIII)(ev.srcEvent.offsetX, ev.srcEvent.offsetY, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							} else if (ev.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::panningHandler(IIII)(ev.pointers[0].clientX, ev.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							}
						});
		hammertime
				.on(
						"panend pancancel",
						function(ev) {
							if (ev.pointerType == "mouse") {
								@explorviz.visualization.engine.navigation.Navigation::mouseUpHandler(IIII)(ev.srcEvent.offsetX, ev.srcEvent.offsetY, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							} else if (ev.pointerType == "touch") {
								@explorviz.visualization.engine.navigation.Navigation::mouseUpHandler(IIII)(ev.pointers[0].clientX, ev.pointers[0].clientY - @explorviz.visualization.engine.main.WebGLStart::navigationHeight, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
							}
						});
	}-*/;

	public static native void deregister() /*-{
		hammertime.unbind("doubletap");
		hammertime.unbind("contextmenu");
	}-*/;
}
