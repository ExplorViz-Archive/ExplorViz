package explorviz.visualization.engine.navigation;

public class TouchNavigationJS {

	public static native void register() /*-{

		$wnd
				.jQuery("#webglcanvas")
				.on("contextmenu", function(ev) {
					if (ev.originalEvent.clientY < ev.target.clientWidth
							- @explorviz.visualization.engine.main.WebGLStart::tempTimeshiftHeight) {
						ev.preventDefault();
						@explorviz.visualization.engine.navigation.Navigation::mouseRightClick(II)(ev.originalEvent.clientX, ev.originalEvent.clientY - @explorviz.visualization.engine.main.WebGLStart::tempNavigationHeight)
					}
				});

		var hammertime = $wnd.jQuery().newHammerManager($doc.getElementById("webglcanvas"), {});
		$wnd.jQuery.fn.hammerTimeInstance = hammertime

		var tapHammer = $wnd.jQuery().newHammerTap({
			event : 'singletap',
			interval : 250
		});
		var doubleTapHammer = $wnd.jQuery().newHammerDblTap({
			event : 'doubletap',
			interval : 250
		});
		var panHammer = $wnd.jQuery().newHammerPan({});
		var pressHammer = $wnd.jQuery().newHammerPress({});
		var pinchHammer = $wnd.jQuery().newHammerPinch({
			enable : true
		});

		hammertime.add([ doubleTapHammer, tapHammer, panHammer, pressHammer, pinchHammer ]);
		doubleTapHammer.recognizeWith(tapHammer);
		tapHammer.requireFailure(doubleTapHammer);

		hammertime
				.on("singletap", function(ev) {
					@explorviz.visualization.engine.navigation.Navigation::mouseSingleClickHandler(II)(ev.srcEvent.clientX, ev.srcEvent.clientY - @explorviz.visualization.engine.main.WebGLStart::tempNavigationHeight);
				});

		hammertime
				.on("doubletap", function(ev) {
					@explorviz.visualization.engine.navigation.Navigation::mouseDoubleClickHandler(II)(ev.srcEvent.clientX, ev.srcEvent.clientY - @explorviz.visualization.engine.main.WebGLStart::tempNavigationHeight);
				});

		hammertime.on("pinchin", function(ev) {
			@explorviz.visualization.engine.navigation.Navigation::mouseWheelHandler(I)(1);
		});

		hammertime.on("pinchout", function(ev) {
			@explorviz.visualization.engine.navigation.Navigation::mouseWheelHandler(I)(-1);
		});

		hammertime
				.on("press", function(ev) {
					if (!@explorviz.visualization.engine.main.WebGLStart::webVRMode) {
						if (ev.srcEvent.clientY
								- @explorviz.visualization.engine.main.WebGLStart::tempNavigationHeight < ev.target.parentElement.parentElement.clientWidth
								- @explorviz.visualization.engine.main.WebGLStart::tempTimeshiftHeight) {
							@explorviz.visualization.engine.picking.ObjectPicker::handleMouseMove(II)(ev.srcEvent.clientX, ev.srcEvent.clientY - @explorviz.visualization.engine.main.WebGLStart::tempNavigationHeight);
						}
					}
				});

		hammertime
				.on("panstart", function(ev) {
					@explorviz.visualization.engine.navigation.Navigation::mouseDownHandler(II)(ev.srcEvent.clientX, ev.srcEvent.clientY - @explorviz.visualization.engine.main.WebGLStart::tempNavigationHeight);

				});
		hammertime
				.on("panmove", function(ev) {
					@explorviz.visualization.engine.navigation.Navigation::panningHandler(IIII)(ev.srcEvent.clientX, ev.srcEvent.clientY - @explorviz.visualization.engine.main.WebGLStart::tempNavigationHeight, ev.target.parentElement.parentElement.clientWidth, ev.target.parentElement.parentElement.clientHeight);
				});
		hammertime
				.on("panend pancancel", function(ev) {
					@explorviz.visualization.engine.navigation.Navigation::mouseUpHandler(II)(ev.srcEvent.clientX, ev.srcEvent.clientY - @explorviz.visualization.engine.main.WebGLStart::tempNavigationHeight);
				});

	}-*/;

	public static native void deregister() /*-{
		$wnd.jQuery().hammerTimeInstance.destroy()
	}-*/;

	public static native void changeTapInterval(int intervalForClick) /*-{

		$wnd.jQuery().hammerTimeInstance.get('singletap').set({
			interval : intervalForClick
		});

		$wnd.jQuery().hammerTimeInstance.get('doubletap').set({
			interval : intervalForClick
		});

	}-*/;

	public static native void setTapRecognizer(boolean status) /*-{

		$wnd.jQuery().hammerTimeInstance.get('singletap').set({
			enable : status
		});

		$wnd.jQuery().hammerTimeInstance.get('doubletap').set({
			enable : status
		});
	}-*/;

}
