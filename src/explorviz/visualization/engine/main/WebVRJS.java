package explorviz.visualization.engine.main;

public class WebVRJS {

	// Alternative to browser console (better due to fullscreen)
	// @explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("debug");

	public static native void initVR() /*-{

		var renderingContext = $wnd.renderingObj;
		var landscape = renderingContext.scene.children[2];

		var THREE = $wnd.THREE;
		var Leap = $wnd.Leap;
		var leapHand = null;

		initLeap();

		renderingContext.vrEffect.requestPresent();

		$wnd.jQuery("#view-wrapper").css("cursor", "none")

		// init controllers
		var controller1, controller2;

		controller1 = new THREE.ViveController(0);
		controller1.standingMatrix = renderingContext.vrControls
				.getStandingMatrix();
		controller1.name = "controller1";
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

		var controllerRay = new THREE.ArrowHelper(dir, origin, 100, hexColor,
				1, 1);
		controllerRay.visible = false;
		renderingContext.scene.add(controllerRay);

		var leapRay = new THREE.ArrowHelper(dir, origin, 100, hexColor, 1, 1);
		leapRay.visible = false;
		renderingContext.scene.add(leapRay);

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
			handleLeap();
			render();
		}
		animate();

		var counterRunning = false;
		var padPressed = false;
		var sideButtonPressed = false;
		var showLeap = false;

		// detached render method
		// to minimize delay between
		// scene update and sending scene
		// to HMD
		function render() {

			if (showControllerRay) {
				var matrix = new THREE.Matrix4();
				matrix.extractRotation(controller1.matrix);

				var direction = new THREE.Vector3(0, 0, 1);
				direction.applyMatrix4(matrix);
				direction.multiplyScalar(-1);

				controllerRay.setDirection(direction);

				var globalController = new THREE.Vector3();
				globalController.setFromMatrixPosition(controller1.matrixWorld);

				controllerRay.position.x = globalController.x;
				controllerRay.position.y = globalController.y;
				controllerRay.position.z = globalController.z;

				if (!counterRunning) {

					var intersectedObj = renderingContext.raycasting(
							controllerRay.position, direction, false);

					if (intersectedObj) {

						var type = intersectedObj.userData.type;

						counterRunning = true;
						setTimeout(function() {
							counterRunning = false;
						}, 600);

						if (sideButtonPressed && type == "package") {
							@explorviz.visualization.engine.threejs.ThreeJSWrapper::toggleOpenStatus(Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizObj);
						} else if (padPressed) {
							if (type == "package") {
								@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,intersectedObj.userData.explorVizObj);
							} else if (type == "class") {
								@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,null);
							}

						}
					}
				}
			}

			if (controller1 && controller1.children[0]) {

				// check if controller is in sight

				var frustum = new THREE.Frustum();
				var cameraViewProjectionMatrix = new THREE.Matrix4();

				renderingContext.camera.updateMatrixWorld();
				renderingContext.camera.matrixWorldInverse
						.getInverse(renderingContext.camera.matrixWorld);
				cameraViewProjectionMatrix.multiplyMatrices(
						renderingContext.camera.projectionMatrix,
						renderingContext.camera.matrixWorldInverse);
				frustum.setFromMatrix(cameraViewProjectionMatrix);

				var controller1Mesh = controller1.children[0].children[0];
				var controller1Geometry = controller1Mesh.geometry;

				controller1Geometry.computeBoundingBox();

				if (controller1Mesh) {
					if (frustum.intersectsObject(controller1Mesh)) {
						showLeap = false;
					} else {
						showLeap = true;
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

							if ("vibrate" in gamepad) {
								console.log("vibration possible");
								gamepad.vibrate(100);

								//					  var vibrationDelay = (500 * (1.0 - gamepad.buttons[j].value)) + 100;
								//                    if (t - lastVibration > vibrationDelay) {
								//                      gamepad.vibrate(100);
								//                      lastVibration = t;
								//                    }
							}
						}

						if (gamepad.buttons[0].pressed) {
							// pad pressed
							padPressed = true;
						} else {
							padPressed = false;
						}

						if (gamepad.buttons[2].pressed) {
							// sidebutton pressed
							sideButtonPressed = true;
						} else {
							sideButtonPressed = false;
						}

					} else if (gamepad.index == 1) {

						var xPos = gamepad.pose.position[2] * -1;
						var yPos = gamepad.pose.position[1];
						var zPos = gamepad.pose.position[0];

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
							landscape.rotation.y += gamepad.axes[0]
									- oldGamePadY;

							landscape.rotation.x += gamepad.axes[1]
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

		function handleLeap() {

			if (!showLeap) {
				leapController.use('boneHand').disconnect();
				leapRay.visible = false;
				return;
			} else {
				leapController.use('boneHand').connect();
			}

			if (!leapHand) {
				leapRay.visible = false;
				return;
			}

			// hand is visible and rendered

			leapRay.visible = true;

			var bboxLandscape = new THREE.Box3().setFromObject(landscape);

			var threeHand = renderingContext.scene
					.getObjectByName("hand-bone-0");

			var bboxHand = new THREE.Box3().setFromObject(threeHand);

			if (bboxHand.intersectsBox(bboxLandscape))
				console.log("intersecting");

			// index finger ray
			//			var indexFinger = roundArray(leapHand.indexFinger.tipPosition);
			//			var indexDirection = roundArray(leapHand.indexFinger.direction);

			var indexFinger = new THREE.Vector3()
					.fromArray(leapHand.indexFinger.tipPosition);
			var indexDirection = new THREE.Vector3()
					.fromArray(leapHand.indexFinger.direction);

			leapRay.setDirection(indexDirection.normalize());
			leapRay.position.x = indexFinger.x;
			leapRay.position.y = indexFinger.y;
			leapRay.position.z = indexFinger.z;

			if (calculateLeapRay && !counterRunning) {

				console.log("calculating");

				var intersectedObj = renderingContext.raycasting(indexFinger,
						indexDirection, false);

				counterRunning = true;
				setTimeout(function() {
					counterRunning = false;
				}, 600);

				if (intersectedObj) {

					console.log("intersecting");

					var type = intersectedObj.userData.type;

					if (type == "package") {
						@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,intersectedObj.userData.explorVizObj);
					} else if (type == "class") {
						@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,null);
					}
				}

			}

			//			var globalController = new THREE.Vector3();
			//			globalController.setFromMatrixPosition(controller1.matrixWorld);
			//
			//			controllerRay.position.x = globalController.x;
			//			controllerRay.position.y = globalController.y;
			//			controllerRay.position.z = globalController.z;
		}

		// init leap
		// initializes the LEAP Motion library for gesture control

		var leapController;
		var calculateLeapRay = false;
		var initial = true;

		var currentHands = null;
		previousHands = null;

		function initLeap() {

			leapController = Leap.loop({
				enableGestures : true
			}, function(frame) {
				if (frame.valid && frame.hands.length > 0) {
					leapHand = frame.hands[0];

					if (frame.gestures.length > 0) {
						frame.gestures.forEach(function(gesture) {
							if (gesture.type == "screenTap") {
								calculateLeapRay = true;
							} else {
								calculateLeapRay = false;
							}
						});
					} else {
						calculateLeapRay = false;
					}

					if (frame.hands[0] != null) {
						if (initial) {
							currentHands = frame.hands;
							previousHands = frame.hands;
							initial = false;
						} else {
							currentHands = frame.hands;
							previousHands = leapController.frame(1).hands;
							gestureDetection();
						}
					}

				} else {
					leapHand = null;
				}
			});

			leapController.use('transform', {
				vr : true,
				effectiveParent : renderingContext.camera
			});

			leapController.use('boneHand', {
				scene : renderingContext.scene,
				arm : true
			});

			//			Leap.loopController.use('riggedHand', {
			//				scene : renderingContext.scene,
			//				renderer : renderingContext.renderer,
			//				camera : renderingContext.camera
			//			});
		}

		function gestureDetection() {
			if (checkHands()) {
				translation();
				rotation();
			}
		}

		function checkHands() {
			var previousHandsAvail = typeof previousHands != 'undefined';
			var currentHandsAvail = typeof currentHands != 'undefined';
			var sameCountOfHands = previousHands.length == currentHands.length;
			var maxTwoHands = currentHands.length == 1
					|| currentHands.length == 2;
			return previousHandsAvail && currentHandsAvail && sameCountOfHands
					&& maxTwoHands;
		}

		var flags = new Array(0, 0, 0, 0, 0, 0);
		var frameCounter = 0;
		var anchorTransZoom;
		var anchorRot;

		function translation() {
			var zoomIdx = 0;
			var transIdx = 1;
			var transZoomTimerIdx = 2;
			if (flags[zoomIdx] > 1)
				return;
			currentHands
					.forEach(function(element, index) {
						if (element.grabStrength >= 0.95
								&& element.type == "right") {
							// check for: new hand in view or hand reappeared
							// => id change => anchor reset
							if (anchorTransZoom != null
									&& anchorTransZoom.id != element.id) {
								flags[transIdx] = 0;
								flags[transZoomTimerIdx] = 0;
							}
							// set anchor and start timer for
							// (non-)intentional interaction
							if (flags[transIdx] == 0) {
								flags[transZoomTimerIdx] = 0;
								flags[transIdx] = 1;
								anchorTransZoom = element;
								if (flags[transZoomTimerIdx] == 0) {
									flags[transZoomTimerIdx] = 1;
									setTimeout(function() {
										flags[transZoomTimerIdx] = 2;
									}, 250);
								}
							}
							// check if intentional
							if (flags[transIdx] == 1) {
								if ((Math.abs(element.palmPosition[0]
										- anchorTransZoom.palmPosition[0]) > 0.07 || Math
										.abs(element.palmPosition[1]
												- anchorTransZoom.palmPosition[1]) > 0.07)
										&& flags[transZoomTimerIdx] == 2) {
									flags[transIdx] = 2;
								} else {
									if (flags[transIdx] == 2)
										flags[transIdx] = 0;
									return;
								}
							}
							// proceed if intentional with calculation
							if (flags[transIdx] == 2) {
								var previousHand = leapController.frame(1)
										.hand(anchorTransZoom.id);
								if (previousHand == null)
									return;
								var movementX = (element.palmPosition[0] - previousHand.palmPosition[0]);
								var movementY = (element.palmPosition[1] - previousHand.palmPosition[1]);
								var movementZ = (previousHand.palmPosition[2] - element.palmPosition[2]);

								landscape.position.x += movementX * 200.0;
								landscape.position.y += movementY * 200.0;
								landscape.position.z += movementZ * -200.0;
								return;
							}
						} else if (element.type == "right") {
							// reset
							flags[transIdx] = 0;
							flags[transZoomTimerIdx] = 0;
						}
					});
		}
		function rotation() {
			var rotIdx = 4;
			var rotTimerIdx = 5;
			currentHands
					.forEach(function(element, index) {
						if (element.grabStrength >= 0.95
								&& element.type == "left") {
							// check for: new hand in view or hand reappeared
							// => id change => anchor reset
							if (anchorRot != null && anchorRot.id != element.id) {
								flags[rotIdx] = 0;
								flags[rotTimerIdx] = 0;
							}
							// set anchor and start timer for
							// (non-)intentional interaction
							if (flags[rotIdx] == 0) {
								flags[rotTimerIdx] = 0;
								flags[rotIdx] = 1;
								anchorRot = element;
								if (flags[rotTimerIdx] == 0) {
									flags[rotTimerIdx] = 1;
									setTimeout(function() {
										flags[rotTimerIdx] = 2;
									}, 250);
								}
							}
							// check if intentional
							if (flags[rotIdx] == 1) {
								if ((Math.abs(element.palmPosition[0]
										- anchorRot.palmPosition[0]) > 0.07 || Math
										.abs(element.palmPosition[1]
												- anchorRot.palmPosition[1]) > 0.07)
										&& flags[rotTimerIdx] == 2) {
									flags[rotIdx] = 2;
								} else {
									if (flags[rotIdx] == 2)
										flags[rotIdx] = 0;
									return;
								}
							}
							// proceed if intentional with calculation
							if (flags[rotIdx] == 2) {
								var previousHand = leapController.frame(1)
										.hand(anchorRot.id);
								if (previousHand == null)
									return;
								var movementX = (element.palmPosition[0] - previousHand.palmPosition[0])
										* (renderingContext.renderer.domElement.clientWidth);
								var movementY = (previousHand.palmPosition[1] - element.palmPosition[1])
										* (renderingContext.renderer.domElement.clientWidth);
								x += movementX;
								y += movementY;
								@explorviz.visualization.engine.navigation.Navigation::mouseMoveVRHandler(IIZZ)(x, y, false, true);
								return;
							}
						} else if (element.type == "left") {
							// reset
							flags[rotIdx] = 0;
							flags[rotTimerIdx] = 0;
						}
					});
		}
		function zoom() {
			var zoomIdx = 0;
			var transIdx = 1;
			var transZoomTimerIdx = 2;
			if (flags[transIdx] > 1)
				return;
			currentHands
					.forEach(function(element, index) {
						if (element.grabStrength >= 0.95
								&& element.type == "right") {
							// check for: new hand in view or hand reappeared
							// => id change => anchor reset
							if (anchorTransZoom != null
									&& anchorTransZoom.id != element.id) {
								flags[transIdx] = 0;
								flags[transZoomTimerIdx] = 0;
							}
							frameCounter = ++frameCounter % 6;
							if (frameCounter != 0)
								return;
							// set anchor and start timer for
							// (non-)intentional interaction
							if (flags[zoomIdx] == 0) {
								flags[transZoomTimerIdx] = 0;
								flags[zoomIdx] = 1;
								anchorTransZoom = element;
								if (flags[transZoomTimerIdx] == 0) {
									flags[transZoomTimerIdx] = 1;
									setTimeout(function() {
										flags[transZoomTimerIdx] = 2;
									}, 250);
								}
							}
							// check if intentional
							if (flags[zoomIdx] == 1) {
								if (Math.abs(element.palmPosition[2]
										- anchorTransZoom.palmPosition[2]) > 0.06
										&& flags[transZoomTimerIdx] == 2) {
									flags[zoomIdx] = 2;
								} else {
									if (flags[zoomIdx] == 2)
										flags[zoomIdx] = 0;
									return;
								}
							}
							// proceed if intentional with calculation
							if (flags[zoomIdx] == 2) {
								var previousHand = leapController.frame(1)
										.hand(anchorTransZoom.id);
								if (previousHand == null)
									return;
								var movementZ = (previousHand.palmPosition[2] - element.palmPosition[2]);
								@explorviz.visualization.engine.navigation.Navigation::mouseWheelHandler(I)(movementZ);
							}
						} else if (element.type == "right") {
							// reset
							flags[zoomIdx] = 0;
							flags[transZoomTimerIdx] = 0;
						}
					});
		}

	}-*/;

	public static native void resetSensor() /*-{
		var renderingContext = $wnd.renderingObj;

		renderingContext.vrControls.resetSensor();

		//		var vrDisplay = $wnd.vrDisplay;
		//		if (vrDisplay)
		//			vrDisplay.resetPose();
	}-*/;
}
