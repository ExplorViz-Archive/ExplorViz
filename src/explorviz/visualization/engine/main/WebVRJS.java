package explorviz.visualization.engine.main;

public class WebVRJS {

	// Alternative to browser console (better due to fullscreen)
	// @explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("debug");

	public static native void goFullScreen() /*-{

		var canvas = $doc.getElementById("threeJSCanvas");

		@explorviz.visualization.engine.main.WebGLStart::setWebVRMode(Z)(true)
		@explorviz.visualization.engine.navigation.TouchNavigationJS::changeTapInterval(I)(500)
		$wnd.jQuery("#view-wrapper").css("cursor", "none")

		//////////////////////////////
		///  Choose canvas for HMD ///
		//////////////////////////////

		var renderingContext = $wnd.renderingObj;

		if ($wnd.vrDisplay.isPresenting) {
			$wnd.vrDisplay.exitPresent();
		} else {

			$wnd.vrDisplay.requestPresent([ {
				source : renderingContext.canvas
			} ]);

		}

		function PerspectiveMatrixFromVRFieldOfView(fov, zNear, zFar) {
			var out = new Float32Array(16);
			var upTan = Math.tan(fov.upDegrees * Math.PI / 180.0);
			var downTan = Math.tan(fov.downDegrees * Math.PI / 180.0);
			var leftTan = Math.tan(fov.leftDegrees * Math.PI / 180.0);
			var rightTan = Math.tan(fov.rightDegrees * Math.PI / 180.0);

			var xScale = 2.0 / (leftTan + rightTan);
			var yScale = 2.0 / (upTan + downTan);
			out[0] = xScale;
			out[4] = 0.0;
			out[8] = -((leftTan - rightTan) * xScale * 0.5);
			out[12] = 0.0;

			out[1] = 0.0;
			out[5] = yScale;
			out[9] = ((upTan - downTan) * yScale * 0.5);
			out[13] = 0.0;

			out[2] = 0.0;
			out[6] = 0.0;
			out[10] = zFar / (zNear - zFar);
			out[14] = (zFar * zNear) / (zNear - zFar);

			out[3] = 0.0;
			out[7] = 0.0;
			out[11] = -1.0;
			out[15] = 0.0;

			return out;
		}

		function resizeFOV() {

			var fovLeft, fovRight;

			var vrDisplay = $wnd.vrDisplay;

			if (!vrDisplay) {
				return;
			}

			fovLeft = vrDisplay.getEyeParameters("left").fieldOfView;
			fovRight = vrDisplay.getEyeParameters("right").fieldOfView;

			var projectionMatrixLeftEye = PerspectiveMatrixFromVRFieldOfView(
					fovLeft, 0.1, 100000);
			@explorviz.visualization.engine.main.SceneDrawer::setPerspectiveLeftEye([F)(projectionMatrixLeftEye);
			var projectionMatrixRightEye = PerspectiveMatrixFromVRFieldOfView(
					fovRight, 0.1, 100000);
			@explorviz.visualization.engine.main.SceneDrawer::setPerspectiveRightEye([F)(projectionMatrixRightEye);
		}

		resizeFOV();

		function onAnimationFrame(t) {

			var vrDisplay = $wnd.vrDisplay;

			if (vrDisplay) {
				// When presenting content to the VRDisplay we want to update at its
				// refresh rate if it differs from the refresh rate of the main
				// display. Calling VRDisplay.requestAnimationFrame ensures we render
				// at the right speed for VR.
				vrDisplay.requestAnimationFrame(onAnimationFrame);

				// As a general rule you want to get the pose as late as possible
				// and call VRDisplay.submitFrame as early as possible after
				// retrieving the pose. Do any work for the frame that doesn't need
				// to know the pose earlier to ensure the lowest latency possible.
				var pose = vrDisplay.getPose();

				if (vrDisplay.isPresenting) {
					// When presenting render a stereo view.
					//					gl.viewport(0, 0, webglCanvas.width * 0.5,
					//							webglCanvas.height);
					//					renderSceneView(pose, vrDisplay.getEyeParameters("left"));
					//
					//					gl.viewport(webglCanvas.width * 0.5, 0,
					//							webglCanvas.width * 0.5, webglCanvas.height);
					//					renderSceneView(pose, vrDisplay.getEyeParameters("right"));

					// If we're currently presenting to the VRDisplay we need to
					// explicitly indicate we're done rendering and inform the
					// display which pose was used to render the current frame.
					//vrControls.update();
					//vrEffect.render(scene, camera);
					vrDisplay.submitFrame(pose);
				}
			}
		}

		$wnd.requestAnimationFrame(onAnimationFrame);

	}-*/;

	public static native void resetSensor() /*-{
		var vrDisplay = $wnd.vrDisplay;
		if (vrDisplay)
			vrDisplay.resetPose();
	}-*/;

	public static native void setDevice() /*-{

		if (navigator.getVRDisplays) {
			navigator.getVRDisplays().then(EnumerateVRDisplays);
		}

		function EnumerateVRDisplays(displays) {
			if (displays.length > 0) {
				$wnd.vrDisplay = displays[0];
				console.log($wnd.vrDisplay)

				var eyeOffsetLeft = $wnd.vrDisplay.getEyeParameters("left").offset;
				var eyeOffsetRight = $wnd.vrDisplay.getEyeParameters("right").offset;

				@explorviz.visualization.engine.main.SceneDrawer::setBothEyesCameras([F[F)(eyeOffsetLeft, eyeOffsetRight);
			}
		}

	}-*/;

	public static native void animationTick() /*-{

		var vrDisplay = $wnd.vrDisplay;

		if (vrDisplay) {
			var vrState = vrDisplay.getImmediatePose();

			var RADTODEG = 57.2957795;

			//update rotation
			@explorviz.visualization.engine.navigation.Camera::rotateAbsoluteY(F)(vrState.orientation[1]*RADTODEG*3);
			@explorviz.visualization.engine.navigation.Camera::rotateAbsoluteX(F)(vrState.orientation[0]*RADTODEG*3);

			//update position
			@explorviz.visualization.engine.navigation.Camera::moveXInVR(F)(vrState.position[0]*RADTODEG*-3);
			@explorviz.visualization.engine.navigation.Camera::moveYInVR(F)(vrState.position[1]*RADTODEG*-3);
			@explorviz.visualization.engine.navigation.Camera::moveZInVR(F)(vrState.position[2]*RADTODEG*-3);
		}
	}-*/;
}
