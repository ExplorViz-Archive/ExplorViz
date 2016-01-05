package explorviz.visualization.engine.main;

public class WebVRJS {

	public static native void goFullScreen() /*-{

		var changeHandler = function() {

			if ($doc.fullscreenElement || $doc.webkitFullscreenElement || $doc.msFullscreenElement
					|| $doc.mozFullScreenElement) {
				@explorviz.visualization.engine.main.WebGLStart::setWebVRMode(Z)(true)
				@explorviz.visualization.engine.navigation.TouchNavigationJS::changeTapInterval(I)(500)
				$wnd.jQuery("#view-wrapper").css("cursor", "none")

			} else {
				@explorviz.visualization.engine.navigation.TouchNavigationJS::setTapRecognizer(Z)(true)
				@explorviz.visualization.engine.navigation.TouchNavigationJS::changeTapInterval(I)(250)
				@explorviz.visualization.engine.main.WebGLStart::setWebVRMode(Z)(false)
				$wnd.jQuery("#view-wrapper").css("cursor", "auto")

				removeLeap();

				$doc.removeEventListener("fullscreenchange", changeHandler, false);
				$doc.removeEventListener("webkitfullscreenchange", changeHandler, false);
				$doc.removeEventListener("mozfullscreenchange", changeHandler, false);
				$doc.removeEventListener("msfullscreenchange", changeHandler, false);

				@explorviz.visualization.engine.main.SceneDrawer::showVRObjects = false;
			}

		}

		$doc.addEventListener("fullscreenchange", changeHandler, false);
		$doc.addEventListener("webkitfullscreenchange", changeHandler, false);
		$doc.addEventListener("mozfullscreenchange", changeHandler, false);
		$doc.addEventListener("msfullscreenchange", changeHandler, false);

		var renderTargetWidth = 1920;
		var renderTargetHeight = 1080;

		// needs to be out of this scope, because of
		// animationTick()-method
		$wnd.hmdSensor = null

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

			if (!$wnd.hmdDevice) {
				return;
			}

			if (amount != 0 && 'setFieldOfView' in $wnd.hmdDevice) {

				fovScale += amount;
				if (fovScale < 0.1) {
					fovScale = 0.1;
				}

				fovLeft = $wnd.hmdDevice.getEyeParameters("left").recommendedFieldOfView;
				fovRight = $wnd.hmdDevice.getEyeParameters("right").recommendedFieldOfView;

				fovLeft.upDegrees *= fovScale;
				fovLeft.downDegrees *= fovScale;
				fovLeft.leftDegrees *= fovScale;
				fovLeft.rightDegrees *= fovScale;

				fovRight.upDegrees *= fovScale;
				fovRight.downDegrees *= fovScale;
				fovRight.leftDegrees *= fovScale;
				fovRight.rightDegrees *= fovScale;

				$wnd.hmdDevice.setFieldOfView(fovLeft, fovRight);
			}

			if ('getRecommendedEyeRenderRect' in $wnd.hmdDevice) {
				var leftEyeViewport = $wnd.hmdDevice.getEyeParameters("left").recommendedFieldOfView;
				var rightEyeViewport = $wnd.hmdDevice.getEyeParameters("right").recommendedFieldOfView;
				renderTargetWidth = leftEyeViewport.width + rightEyeViewport.width;
				renderTargetHeight = Math.max(leftEyeViewport.height, rightEyeViewport.height);
			}

			if ('getCurrentEyeFieldOfView' in $wnd.hmdDevice) {
				fovLeft = $wnd.hmdDevice.getCurrentEyeFieldOfView("left");
				fovRight = $wnd.hmdDevice.getCurrentEyeFieldOfView("right");
			} else {
				fovLeft = $wnd.hmdDevice.getEyeParameters("left").recommendedFieldOfView;
				fovRight = $wnd.hmdDevice.getEyeParameters("right").recommendedFieldOfView;
			}

			var projectionMatrixLeftEye = PerspectiveMatrixFromVRFieldOfView(fovLeft, 0.1, 100000);
			@explorviz.visualization.engine.main.SceneDrawer::setPerspectiveLeftEye([F)(projectionMatrixLeftEye);
			var projectionMatrixRightEye = PerspectiveMatrixFromVRFieldOfView(fovRight, 0.1, 100000);
			@explorviz.visualization.engine.main.SceneDrawer::setPerspectiveRightEye([F)(projectionMatrixRightEye);
		}

		var canvas = $doc.getElementById("webglcanvas");

		resizeFOV(0.0);
		$doc.getElementById("webglDiv").webkitRequestFullscreen({
			vrDisplay : $wnd.hmdDevice,
		});

		// pointer lock
		var x = 320
		var y = 400
		var z = 0

		canvas.requestPointerLock = canvas.requestPointerLock || canvas.mozRequestPointerLock
				|| canvas.webkitRequestPointerLock;

		$doc.exitPointerLock = $doc.exitPointerLock || $doc.mozExitPointerLock
				|| $doc.webkitExitPointerLock;

		canvas.requestPointerLock();

		$doc.addEventListener("pointerlockchange", changeLockCallback, false);
		$doc.addEventListener("mozpointerlockchange", changeLockCallback, false);
		$doc.addEventListener("webkitpointerlockchange", changeLockCallback, false);
		$doc.addEventListener("mousemove", mouseCallback, false);
		$doc.addEventListener("mousedown", mouseDown, false);

		function changeLockCallback() {
			if ($doc.pointerLockElement === canvas || $doc.mozPointerLockElement === canvas
					|| $doc.webkitPointerLockElement === canvas) {
				// lock already initialized
			} else {
				$doc.exitPointerLock();
				removePointerListener();
			}
		}

		function mouseDown(e) {
			@explorviz.visualization.engine.navigation.TouchNavigationJS::setTapRecognizer(Z)(true)
		}

		function mouseCallback(e) {

			var movementX = e.movementX || e.mozMovementX || e.webkitMovementX || 0;
			var movementY = e.movementY || e.mozMovementY || e.webkitMovementY || 0;

			// mouse moved: disable SingleTap
			if (movementX != 0 || movementY != 0) {
				@explorviz.visualization.engine.navigation.TouchNavigationJS::setTapRecognizer(Z)(false)
			}

			x += movementX;
			y += movementY;

			var btnCode = e.which;
			var left;
			var right;

			switch (btnCode) {
			case 1:
				left = true
				right = false
				break;
			case 3:
				left = false
				right = true
				break;
			default:
				left = false
				right = false
			}

			@explorviz.visualization.engine.navigation.Navigation::mouseMoveVRHandler(IIZZ)(x, y, left, right)
		}

		function removePointerListener() {

			$doc.removeEventListener("pointerlockchange", changeLockCallback, false);
			$doc.removeEventListener("mozpointerlockchange", changeLockCallback, false);
			$doc.removeEventListener("webkitpointerlockchange", changeLockCallback, false);
			$doc.removeEventListener("mousemove", mouseCallback, false);
			$doc.removeEventListener("mousedown", mouseDown, false);
		}

		//////////////////////////////
		// Leap Motion scene set-up //
		//////////////////////////////

		var foreground = $doc.getElementById("leapcanvas");

		foreground.style.top = 0;
		foreground.style.left = 0;
		foreground.style.position = 'absolute';

		var camera, scene;

		var viewportWidth = @explorviz.visualization.engine.main.WebGLStart::viewportWidth;
		var viewportHeight = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

		camera = new $wnd.THREE.PerspectiveCamera(75, viewportWidth / viewportHeight, 0.1, 10000);

		scene = new $wnd.THREE.Scene();

		renderer = new $wnd.THREE.WebGLRenderer({
			canvas : foreground,
			alpha : true,
			antialias : true
		});

		renderer.setSize(viewportWidth, viewportHeight);

		var light = new $wnd.THREE.PointLight(0xffffff, 1, 1000);
		scene.add(light);

		var previousHands = null;
		var currentHands = null;
		var initial = true;

		var controller = $wnd.Leap
				.loop({
					enableGestures : true
				}, function(frame) {
					if (frame.valid) {
						if (frame.hands[0] != null) {
							if (initial) {
								currentHands = frame.hands;
								previousHands = frame.hands;
								initial = false;
							} else {
								currentHands = frame.hands;
								previousHands = controller.frame(1).hands;
								gestureDetection();
							}
						}

					}
					if (frame.gestures.length > 0) {
						frame.gestures
								.forEach(function(gesture) {
									switch (gesture.type) {
									case "screenTap":
										@explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("ScreenTap");
										handleClicks();
										break;
									}
								});
					}
				});

		$wnd.Leap.loopController.use('transform', {

			// This matrix flips the x, y, and z axis, scales to meters, and offsets the hands by -8cm.
			vr : true,

			// This causes the camera's matrix transforms (position, rotation, scale) to be applied to the hands themselves
			// The parent of the bones remain the scene, allowing the data to remain in easy-to-work-with world space.
			// (As the hands will usually interact with multiple objects in the scene.)
			effectiveParent : camera

		});

		$wnd.Leap.loopController.use('boneHand', {

			// If you already have a scene or want to create it yourself, you can pass it in here
			// Alternatively, you can pass it in whenever you want by doing
			// Leap.loopController.plugins.boneHand.scene = myScene.
			scene : scene,

			// Display the arm
			arm : false

		});

		var vrControls = new $wnd.THREE.VRControls(camera, function(message) {
		});

		var vrEffect = new $wnd.THREE.VREffect(renderer, function(message) {
		});

		vrEffect.setFullScreen(true);

		var requestId = null;

		/////////////////////
		// scene rendering //
		/////////////////////

		function render() {

			vrControls.update();
			vrEffect.render(scene, camera);

			requestId = requestAnimationFrame(render);
		}

		render();

		/////////////////////////
		// Gesture recognition //
		/////////////////////////

		function gestureDetection() {

			if (checkHands()) {
				//@explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("1: " + currentHands[0].palmPosition[0].toString());
				translation();
				rotation();
				zoom();
			}

		}

		function checkHands() {
			var previousHandsAvail = typeof previousHands != 'undefined';
			var currentHandsAvail = typeof currentHands != 'undefined';
			var sameCountOfHands = previousHands.length == currentHands.length;

			return previousHandsAvail && currentHandsAvail && sameCountOfHands;
		}

		var frameCounter = 0;

		var zoomingStatus = 0;
		var zoomingActive = 0;
		var anchorX;
		var anchorY;
		var anchorZ;
		var translationStatus = 0;

		function translation() {

			if (zoomingStatus > 1)
				return;

			currentHands
					.forEach(function(element, index) {
						if (element.grabStrength >= 0.95 && element.type == "right") {

							if (translationStatus == 0) {
								translationStatus = 1;
								anchorX = element.palmPosition[0];
								anchorY = element.palmPosition[1];
							}

							if (translationStatus == 1) {

								if (Math.abs(element.palmPosition[0] - anchorX) > 0.015
										|| Math.abs(element.palmPosition[1] - anchorY) > 0.015) {
									translationStatus = 2;
								} else {
									return;
								}
							}

							if (translationStatus == 2) {

								//@explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("Translation");

								var movementX = (element.palmPosition[0] - previousHands[index].palmPosition[0])
										* (viewportWidth);
								var movementY = (previousHands[index].palmPosition[1] - element.palmPosition[1])
										* (viewportWidth);

								x += movementX;
								y += movementY;

								@explorviz.visualization.engine.navigation.Navigation::mouseMoveVRHandler(IIZZ)(x, y, true, false);

							}

						} else {
							translationStatus = 0;
						}
					});
		}

		function rotation() {
			currentHands
					.forEach(function(element, index) {
						if (element.grabStrength >= 0.95 && element.type == "left") {
							var movementX = (element.palmPosition[0] - previousHands[index].palmPosition[0])
									* (viewportWidth);
							var movementY = (previousHands[index].palmPosition[1] - element.palmPosition[1])
									* (viewportWidth);

							x += movementX;
							y += movementY;

							@explorviz.visualization.engine.navigation.Navigation::mouseMoveVRHandler(IIZZ)(x, y, false, true);

							return;
						}
					});
		}

		function zoom() {

			if (translationStatus > 1)
				return;

			currentHands
					.forEach(function(element, index) {
						if (element.grabStrength >= 0.95 && element.type == "right") {

							frameCounter = ++frameCounter % 5;

							if (frameCounter != 0)
								return;

							if (zoomingStatus == 0) {
								zoomingStatus = 1;
								anchorZ = element.palmPosition[2];
							}

							if (zoomingStatus == 1) {

								@explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("Anchor");

								if (Math.abs(element.palmPosition[2] - anchorZ) > 0.01) {
									zoomingStatus = 2;
								} else {
									return;
								}
							}

							if (zoomingStatus == 2) {
								var movementZ = (previousHands[index].palmPosition[2] - element.palmPosition[2]);
								@explorviz.visualization.engine.navigation.Navigation::mouseWheelHandler(I)(movementZ);
							}
						} else {
							zoomingStatus = 0;
						}
					});
		}

		var clickedOnce = false;

		function handleClicks() {

			if (!clickedOnce) {
				clickedOnce = true;

				setTimeout(function() {
					if (clickedOnce) {
						clickedOnce = false;
						@explorviz.visualization.engine.navigation.Navigation::mouseSingleClickHandler(II)(0,0);
					}

				}, 1000);

			} else {
				clickedOnce = false;
				@explorviz.visualization.engine.navigation.Navigation::mouseDoubleClickHandler(II)(0,0);
			}
		}

		/////////////
		// cleanup //
		/////////////

		function removeLeap() {

			if (requestId) {
				cancelAnimationFrame(requestId);
				requestId = null;
			}

			renderer = null;
			scene = null;
			projector = null;
			camera = null;
			vrControls = null;
			vrEffect = null;
			controller = null;

			$wnd.Leap.loopController.stopUsing('boneHand');
			$wnd.Leap.loopController.stopUsing('transform');
			$wnd.Leap.loopController.disconnect();

			foreground.style.position = 'relative';
		}

	}-*/;

	public static native void resetSensor() /*-{
		var sensor = $wnd.hmdSensor;
		if (sensor)
			sensor.resetSensor();
	}-*/;

	public static native void setDevice() /*-{

		if (navigator.getVRDevices) {
			navigator.getVRDevices().then(EnumerateVRDevices);
		} else if (navigator.mozGetVRDevices) {
			navigator.mozGetVRDevices(EnumerateVRDevices);
		}

		function EnumerateVRDevices(devices) {
			//find hmdDevice
			for (var i = 0; i < devices.length; ++i) {
				if (devices[i] instanceof HMDVRDevice) {
					$wnd.hmdDevice = devices[i];

					var eyeOffsetLeft = $wnd.hmdDevice.getEyeParameters("left").eyeTranslation;
					var eyeOffsetRight = $wnd.hmdDevice.getEyeParameters("right").eyeTranslation;

					@explorviz.visualization.engine.main.SceneDrawer::setBothEyesCameras([F[F)(eyeOffsetLeft, eyeOffsetRight);

				}
			}

			// find hmdSensor
			for (var i = 0; i < devices.length; ++i) {
				if (devices[i] instanceof PositionSensorVRDevice
						&& (!$wnd.hmdDevice || devices[i].hardwareUnitId == $wnd.hmdDevice.hardwareUnitId)) {
					$wnd.hmdSensor = devices[i];
					$wnd.hmdSensor.resetSensor();
				}
			}
		}
	}-*/;

	public static native void animationTick() /*-{

		var sensor = $wnd.hmdSensor;

		if (sensor) {
			var vrState = sensor.getState();

			var RADTODEG = 57.2957795;

			//update rotation
			@explorviz.visualization.engine.navigation.Camera::rotateAbsoluteY(F)(vrState.orientation.y*RADTODEG*-3);
			@explorviz.visualization.engine.navigation.Camera::rotateAbsoluteX(F)(vrState.orientation.x*RADTODEG*-4);

			//update position
			//@explorviz.visualization.engine.navigation.Camera::moveY(F)(vrState.orientation.y*RADTODEG*2);
			//@explorviz.visualization.engine.navigation.Camera::moveX(F)(vrState.orientation.x*RADTODEG*4);
		}
	}-*/;
}
