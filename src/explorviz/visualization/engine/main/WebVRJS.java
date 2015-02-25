package explorviz.visualization.engine.main;

public class WebVRJS {
	public static native void goFullScreen() /*-{
		var changeHandler = function() {
			if ($doc.fullscreen || $doc.webkitIsFullScreen
					|| $doc.msFullscreenElement || $doc.mozFullScreen) {
				@explorviz.visualization.engine.main.WebGLStart::setWebVRMode(Z)(true)
				$wnd.jQuery("#view-wrapper").css("cursor", "none")
			} else {
				@explorviz.visualization.engine.main.WebGLStart::setWebVRMode(Z)(false)
				$wnd.jQuery("#view-wrapper").css("cursor", "auto")
			}

		}

		$doc.addEventListener("fullscreenchange", changeHandler, false);
		$doc.addEventListener("webkitfullscreenchange", changeHandler, false);
		$doc.addEventListener("mozfullscreenchange", changeHandler, false);
		$doc.addEventListener("msfullscreenchange", changeHandler, false);

		var hmdDevice = null
		var hmdSensor = null
		var RADTODEG = 57.2957795;
		var renderTargetWidth = 1920;
		var renderTargetHeight = 1080;

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

		var fovScale = 1.0;
		function resizeFOV(amount) {
			var fovLeft, fovRight;

			if (!hmdDevice) {
				return;
			}
			if (amount != 0 && 'setFieldOfView' in hmdDevice) {
				fovScale += amount;
				if (fovScale < 0.1) {
					fovScale = 0.1;
				}

				fovLeft = hmdDevice.getRecommendedEyeFieldOfView("left");
				fovRight = hmdDevice.getRecommendedEyeFieldOfView("right");

				fovLeft.upDegrees *= fovScale;
				fovLeft.downDegrees *= fovScale;
				fovLeft.leftDegrees *= fovScale;
				fovLeft.rightDegrees *= fovScale;

				fovRight.upDegrees *= fovScale;
				fovRight.downDegrees *= fovScale;
				fovRight.leftDegrees *= fovScale;
				fovRight.rightDegrees *= fovScale;

				hmdDevice.setFieldOfView(fovLeft, fovRight);
			}

			if ('getRecommendedEyeRenderRect' in hmdDevice) {
				var leftEyeViewport = hmdDevice
						.getRecommendedEyeRenderRect("left");
				var rightEyeViewport = hmdDevice
						.getRecommendedEyeRenderRect("right");
				renderTargetWidth = leftEyeViewport.width
						+ rightEyeViewport.width;
				renderTargetHeight = Math.max(leftEyeViewport.height,
						rightEyeViewport.height);
			}

			//			$wnd.jQuery("#webglcanvas")[0].width = renderTargetWidth;
			//			$wnd.jQuery("#webglcanvas")[0].height = renderTargetHeight;

			if ('getCurrentEyeFieldOfView' in hmdDevice) {
				fovLeft = hmdDevice.getCurrentEyeFieldOfView("left");
				fovRight = hmdDevice.getCurrentEyeFieldOfView("right");
			} else {
				fovLeft = hmdDevice.getRecommendedEyeFieldOfView("left");
				fovRight = hmdDevice.getRecommendedEyeFieldOfView("right");
			}

			var projectionMatrixLeftEye = PerspectiveMatrixFromVRFieldOfView(
					fovLeft, 0.1, 1000);
			@explorviz.visualization.engine.main.SceneDrawer::setPerspectiveLeftEye([F)(projectionMatrixLeftEye);
			var projectionMatrixRightEye = PerspectiveMatrixFromVRFieldOfView(
					fovRight, 0.1, 1000);
			@explorviz.visualization.engine.main.SceneDrawer::setPerspectiveRightEye([F)(projectionMatrixRightEye);
		}

		function EnumerateVRDevices(devices) {

			//find hmdDevice
			for (var i = 0; i < devices.length; ++i) {
				if (devices[i] instanceof HMDVRDevice) {
					hmdDevice = devices[i];

					var eyeOffsetLeft = hmdDevice.getEyeTranslation("left");
					var eyeOffsetRight = hmdDevice.getEyeTranslation("right");

					@explorviz.visualization.engine.main.SceneDrawer::setLeftEyeCamera([F)(eyeOffsetLeft);
					@explorviz.visualization.engine.main.SceneDrawer::setRightEyeCamera([F)(eyeOffsetRight);

				}
			}

			// find hmdSensor
			for (var i = 0; i < devices.length; ++i) {
				if (devices[i] instanceof PositionSensorVRDevice
						&& (!hmdDevice || devices[i].hardwareUnitId == hmdDevice.hardwareUnitId)) {
					hmdSensor = devices[i];
				}
			}

			//resize FieldOfView, go to fullscreen
			resizeFOV(0.0);
			$wnd.jQuery("#webglcanvas")[0].webkitRequestFullscreen({
				vrDisplay : hmdDevice
			});
		}

		if (navigator.getVRDevices) {
			navigator.getVRDevices().then(EnumerateVRDevices);
		} else if (navigator.mozGetVRDevices) {
			navigator.mozGetVRDevices(EnumerateVRDevices);
		} else {
		}
	}-*/;

	public static native void animationTick() /*-{
		if (hmdSensor) {
			var vrState = hmdSensor.getState();

			//update rotation
			@explorviz.visualization.engine.navigation.Camera::rotateAbsoluteY(F)(vrState.orientation.y*RADTODEG*-3);
			@explorviz.visualization.engine.navigation.Camera::rotateAbsoluteX(F)(vrState.orientation.x*RADTODEG*-4);

			//update position
			//						@explorviz.visualization.engine.navigation.Camera::moveY(F)(vrState.orientation.y*RADTODEG*2);
			//						@explorviz.visualization.engine.navigation.Camera::moveX(F)(vrState.orientation.x*RADTODEG*4);
		}
	}-*/;
}