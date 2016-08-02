package explorviz.visualization.engine.main;

public class WebVRJS {

	// Alternative to browser console (better due to fullscreen)
	// @explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("debug");

	public static native void goFullScreen() /*-{

		var renderingContext = $wnd.renderingObj;
		var landscape = renderingContext.scene.children[2];

		var THREE = $wnd.THREE;

		renderingContext.vrEffect.requestPresent();

		$wnd.jQuery("#view-wrapper").css("cursor", "none")

		// init controllers
		var controller1, controller2;

		controller1 = new THREE.ViveController(0);
		controller1.standingMatrix = renderingContext.vrControls
				.getStandingMatrix();
		renderingContext.scene.add(controller1);

		controller2 = new THREE.ViveController(1);
		controller2.standingMatrix = renderingContext.vrControls
				.getStandingMatrix();
		renderingContext.scene.add(controller2);

		var vivePath = 'js/threeJS/';
		var loader = new THREE.OBJLoader();

		loader.load(vivePath + 'vr_controller_vive_1_5.obj', function(object) {

			var loader = new THREE.TextureLoader();

			var controller = object.children[0];
			controller.material.map = loader.load(vivePath
					+ 'onepointfive_texture.png');
			controller.material.specularMap = loader.load(vivePath
					+ 'onepointfive_spec.png');

			controller1.add(object.clone());
			controller2.add(object.clone());

		});

		// init controller ray
		var dir = new THREE.Vector3(0, 0, 1);
		var origin = controller.position;
		var hexColor = 0xffff00;

		var controllerRay = new THREE.ArrowHelper(dir, origin, 5, hex);
		controllerRay.visible = false;
		renderingContext.scene.add(controllerRay);

		var triggerPressed = new Array(4);
		var xOld = 0.0;
		var yOld = 0.0;
		var zOld = 0.0;

		var showControllerRay = false;

		function animate() {
			$wnd.requestAnimationFrame(animate);

			showControllerRay = false;
			handleControllers();
			render();
		}
		animate();

		function render() {

			if (showControllerRay) {
				var matrix = new THREE.Matrix4();
				matrix.extractRotation(controller.matrix);

				var direction = new THREE.Vector3(0, 0, 1);
				direction = direction.applyMatrix4(matrix);
				controllerRay.setDirection(direction);

				// TODO Raycast
			}

			renderingContext.vrControls.update();
			renderingContext.vrEffect.render(renderingContext.scene,
					renderingContext.camera);
		}

		function handleControllers() {
			var gamepads = navigator.getGamepads();

			var resetPos = true;
			controllerRay.visible = false;

			var numOfControllers = gamepads.length;

			for (var i = 0; i < numOfControllers; i++) {
				if (gamepads[i]) {

					var gamepad = gamepads[i];

					if (!gamepad.pose)
						return;

					var xPos = gamepad.pose.position[0];
					var yPos = gamepad.pose.position[1];
					var zPos = gamepad.pose.position[2];

					var xDiff = xPos - xOld;
					var yDiff = yPos - yOld;
					var zDiff = zPos - zOld;

					if (gamepad.buttons[1].pressed) {
						// trigger pulled
						resetPos = false;
						landscape.translateX(xDiff * 100);
						landscape.translateY(yDiff * 100);
						landscape.translateZ(zDiff * 100);
					} else if (gamepad.buttons[1].pressed) {
						// pad presses
						showControllerRay = true;
						controllerRay.visible = true;
					}

					else {

					}
					xOld = xPos;
					yOld = yPos;
					zOld = zPos;
				}
			}

			if (resetPos) {
				xOld = 0.0;
				yOld = 0.0;
				zOld = 0.0;
			}

		}

	}-*/;

	public static native void resetSensor() /*-{
		var renderingContext = $wnd.renderingObj;

		renderingContext.vrControls.resetSensor();

		//		var vrDisplay = $wnd.vrDisplay;
		//		if (vrDisplay)
		//			vrDisplay.resetPose();
	}-*/;

	public static native void setDevice() /*-{

//		if (navigator.getVRDisplays) {
//			navigator.getVRDisplays().then(EnumerateVRDisplays);
//		}
//
//		function EnumerateVRDisplays(displays) {
//			if (displays.length > 0) {
//
//				@explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("display found");
//
//				$wnd.vrDisplay = displays[0];
//				console.log($wnd.vrDisplay)
//
//				var eyeOffsetLeft = $wnd.vrDisplay.getEyeParameters("left").offset;
//				var eyeOffsetRight = $wnd.vrDisplay.getEyeParameters("right").offset;
//
//				@explorviz.visualization.engine.main.SceneDrawer::setBothEyesCameras([F[F)(eyeOffsetLeft, eyeOffsetRight);
//			}
//		}

	}-*/;

	public static native void animationTick() /*-{

//		var vrDisplay = $wnd.vrDisplay;
//
//		if (vrDisplay) {
//			var vrState = vrDisplay.getImmediatePose();
//
//			var RADTODEG = 57.2957795;

			//update rotation
//			@explorviz.visualization.engine.navigation.Camera::rotateAbsoluteY(F)(vrState.orientation[1]*RADTODEG*3);
//			@explorviz.visualization.engine.navigation.Camera::rotateAbsoluteX(F)(vrState.orientation[0]*RADTODEG*3);

			//update position
			//@explorviz.visualization.engine.navigation.Camera::moveXInVR(F)(vrState.position[0]*RADTODEG*-3);
			//@explorviz.visualization.engine.navigation.Camera::moveYInVR(F)(vrState.position[1]*RADTODEG*-3);
			//@explorviz.visualization.engine.navigation.Camera::moveZInVR(F)(vrState.position[2]*RADTODEG*-3);
	//	}
	}-*/;
}
