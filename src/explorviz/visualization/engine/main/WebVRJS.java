package explorviz.visualization.engine.main;

public class WebVRJS {

	// Alternative to browser console (better due to fullscreen)
	// @explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("debug");

	public static native void initVR() /*-{

		var renderingContext = $wnd.renderingObj;
		var landscape = renderingContext.scene.children[2];

		var THREE = $wnd.THREE;
		var Leap = $wnd.Leap;

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
		var origin = controller1.position;
		var hexColor = 0x000000;

		var controllerRay = new THREE.ArrowHelper(dir, origin, 100, hexColor);
		controllerRay.visible = false;
		renderingContext.scene.add(controllerRay);

		var triggerPressed = new Array(4);
		var xOld = 0.0;
		var yOld = 0.0;
		var zOld = 0.0;
		var initialPressed = false;

		var showControllerRay = false;

		function animate() {
			$wnd.requestAnimationFrame(animate);

			showControllerRay = false;
			handleControllers();
			render();
		}
		animate();

		var counterRunning = false;
		var sideButtonPressed = false;

		function render() {

			if (showControllerRay) {
				var matrix = new THREE.Matrix4();
				matrix.extractRotation(controller1.matrix);

				var direction = new THREE.Vector3(0, 0, 1);
				direction = direction.applyMatrix4(matrix);
				direction.multiplyScalar(-1);

				// controller offset
				direction.x += 0.05;
				direction.y += 0.005;

				controllerRay.setDirection(direction);

				controllerRay.position.x = controller1.position.x + 0.7;
				controllerRay.position.y = controller1.position.y + 1.0;
				controllerRay.position.z = controller1.position.z;

				if (!counterRunning) {

					var intersectedObj = renderingContext.raycasting(
							controller1.position, direction);

					if (intersectedObj
							&& intersectedObj.userData.type == 'package') {

						counterRunning = true;
						setTimeout(function() {
							counterRunning = false;
						}, 600);

						if (sideButtonPressed) {
							@explorviz.visualization.engine.threejs.ThreeJSWrapper::toggleOpenStatus(Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizObj);
						} else {
							@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,intersectedObj.userData.explorVizObj);
						}

					}
				}
			}

			renderingContext.vrControls.update();
			renderingContext.vrEffect.render(renderingContext.scene,
					renderingContext.camera);
		}

		var initialSet = false;
		var oldGamepadX = 0.0;
		var oldGamepadY = 0.0;

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

					if (gamepad.index == 0) {
						if (gamepad.buttons[1].pressed) {
							// trigger pressed
							showControllerRay = true;
							controllerRay.visible = true;
						}

						if (gamepad.buttons[0].pressed) {
							// pad pressed
							sideButtonPressed = true;
						} else {
							sideButtonPressed = false;
						}

					} else if (gamepad.index == 1) {

						var xPos = gamepad.pose.position[0];
						var yPos = gamepad.pose.position[1];
						var zPos = gamepad.pose.position[2];

						if (!initialPressed) {
							initialPressed = true;
							xOld = xPos;
							yOld = yPos;
							zOld = zPos;
						}

						var xDiff = xPos - xOld;
						var yDiff = yPos - yOld;
						var zDiff = zPos - zOld;

						if (gamepad.buttons[1].pressed) {
							// pad pressed
							resetPos = false;

							landscape.position.x += xDiff * 100;
							landscape.position.y += yDiff * 100;
							landscape.position.z += zDiff * 100;
						}

						xOld = xPos;
						yOld = yPos;
						zOld = zPos;

						if (gamepad.axes[1]) {

							if (!initialSet) {
								initialSet = true;
								oldGamePadX = gamepad.axes[1];
								oldGamePadY = gamepad.axes[0];
							}

							// rotate based on trackpad					
							//renderingContext.landscape.rotation.y = gamepad.axes[0];
							//							renderingContext.landscape.rotateZ(gamepad.axes[1]
							//									- oldGamePadX);

							//							renderingContext.landscape.rotateY(gamepad.axes[0]
							//									- oldGamePadY);
							//							renderingContext.landscape.rotateX(gamepad.axes[1]
							//									- oldGamePadX);

							renderingContext.landscape.rotation.y += gamepad.axes[0]
									- oldGamePadY;

							renderingContext.landscape.rotation.x += gamepad.axes[1]
									- oldGamePadX;

							oldGamePadX = gamepad.axes[1];
							oldGamePadY = gamepad.axes[0];
						} else {
							initialSet = false;
						}
					}
				}
			}

			if (resetPos) {
				initialPressed = false;
				xOld = 0.0;
				yOld = 0.0;
				zOld = 0.0;
			}

		}

		// init leap

		var controller = Leap.loop({
			enableGestures : true
		}, function(frame) {
		});

		Leap.loopController.use('transform', {
			vr : true,
			effectiveParent : renderingContext.camera
		});

		//		controller.use('riggedHand', {
		//	parent : renderingContext.scene,
		//			scale : 1.3
		//		});

		Leap.loopController.use('boneHand', {
			scene : renderingContext.scene,
			arm : false
		});

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
