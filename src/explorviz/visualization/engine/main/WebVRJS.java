package explorviz.visualization.engine.main;

public class WebVRJS {

	// Alternative to console.log(String)
	// @explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("debug");

	public static native void initVR() /*-{

		var modal = "<div class='modal fade' id='modalVRGuide' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>"
				+ "<div class='modal-dialog modal-dialog-center' role='document'>"
				+ "<div class='modal-content'>"
				+ "<div class='modal-header'>"
				+ "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>"
				+ "<span aria-hidden='true'>&times;</span>"
				+ "</button>"
				+ "<h4 align='center' class='modal-title' id='modalVRGuideTitle'>Experiment details</h4>"
				+ "</div>"
				+ "<div id='modalVRGuideBody' class='modal-body' style='text-align: center; color: #333333; font-size: 150%;'>"
				+ "</div>"
				+ "<div class='modal-footer' style='text-align: center;'>"
				+ "<button id='modalVRGuideStartButton' type='button' class='btn btn-success' data-dismiss='modal'>Enter VR</button>"
				+ "<button id='modalVRGuideExitButton' type='button' class='btn btn-secondary' data-dismiss='modal'>Go back to the real world</button>"
				+ "</div>" + "</div>" + "</div>" + "</div>";

		if ($wnd.jQuery("#modalVRGuide").length == 0) {
			$wnd.jQuery("body").prepend(modal);
			$wnd.jQuery("#modalVRGuide").modal({
				backdrop : 'static',
				keyboard : false
			})
		}

		$wnd.jQuery("#modalVRGuideTitle").text(
				"Welcome to the Virtual Reality mode");

		var img = $wnd.jQuery('<img />', {
			id : 'vrGuideLogo',
			src : 'images/vr/vrGuideLogo.png'
		});
		img.css('width', '100%');

		var infoText = "Please setup your HTC Vive or Oculus Rift CV1 first. "
				+ "You can manipulate the model with the HMD related controllers or/and an additional Leap Motion sensor.";

		$wnd.jQuery("#modalVRGuideBody").html(img);
		$wnd.jQuery("#modalVRGuideBody").append("<br/><br/>" + infoText);

		$wnd.jQuery("#modalVRGuide").modal("show");

		$wnd.jQuery("#modalVRGuideStartButton").on("click", function(e) {
			finishInit();
		});

		function finishInit() {
			var renderingContext = $wnd.renderingObj;
			var scene = renderingContext.scene;
			var camera = renderingContext.camera;
			var landscape = scene.children[3];

			// libraries
			var THREE = $wnd.THREE;
			var Leap = $wnd.Leap;

			// vive variables
			var controller1, controller2;

			var previousGamepad = {
				position : {
					x : 0.0,
					y : 0.0,
					z : 0.0
				},
				axes : {
					x : 0.0,
					y : 0.0
				}
			};

			// leap motion variables
			var leapController, leapRay;

			var leapVars = {
				leapHand : null,
				currentHands : null,
				previousHands : null,
				anchors : {
					translation : null,
					rotation : null
				},
				showHands : false,
				boneHandsConnected : false,
				handMeshes : []
			};

			// gesture detection variables
			var flags = new Array(0, 0, 0, 0, 0, 0, 0);

			// logic
			initLeap();
			initControllers();

			renderingContext.vrEffect.requestPresent();

			function animate() {
				$wnd.requestAnimationFrame(animate);
				handleControllers();
				handleLeap();
				render();
			}
			animate();

			// detached render method
			// to minimize delay between
			// scene update and sending scene
			// to HMD
			function render() {
				renderingContext.vrControls.update();
				renderingContext.vrEffect.render(scene, camera);
			}

			// handlers
			function handleControllers() {
				var gamepads = navigator.getGamepads();

				var resetPos = true;

				var numOfControllers = gamepads.length;

				for (var i = 0; i < numOfControllers; i++) {
					if (gamepads[i]) {

						var gamepad = gamepads[i];

						if (!gamepad.pose)
							return;

						if (gamepad.index == 0) {
							// first controller

							if (gamepad.buttons[1].pressed) {
								// trigger pressed
								controller1.ray.visible = true;

								// delete leap meshes as this controller action
								// indicates, that leap is not used atm
								leapVars.handMeshes = [];

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
							// trigger not pressed
							else {
								controller1.ray.visible = false;
							}

							if (gamepad.buttons[0].pressed) {
								// pad pressed
								controller1.padPressed = true;
							} else {
								controller1.padPressed = false;
							}

							if (gamepad.buttons[2].pressed) {
								// sidebutton pressed
								controller1.sideButtonPressed = true;
							} else {
								controller1.sideButtonPressed = false;
							}

						} else if (gamepad.index == 1) {
							// second controller

							var xPos = gamepad.pose.position[0];
							var yPos = gamepad.pose.position[1];
							var zPos = gamepad.pose.position[2];

							if (previousGamepad.position.x == null) {
								previousGamepad.position.x = xPos;
								previousGamepad.position.y = yPos;
								previousGamepad.position.z = zPos;
							}

							var xDiff = xPos - previousGamepad.position.x;
							var yDiff = yPos - previousGamepad.position.y;
							var zDiff = zPos - previousGamepad.position.z;

							if (gamepad.buttons[1].pressed) {
								// trigger pressed

								resetPos = false;

								//							landscape.translateX(xDiff * 100);
								//							landscape.translateY(yDiff * 100);
								//							landscape.translateZ(zDiff * 100);

								landscape.position.x += xDiff * 100;
								landscape.position.y += yDiff * 100;
								landscape.position.z += zDiff * 100;
							}

							previousGamepad.position.x = xPos;
							previousGamepad.position.y = yPos;
							previousGamepad.position.z = zPos;

							if (gamepad.axes[0]) {
								// trackpad touched

								if (previousGamepad.axes.x == null) {
									previousGamepad.axes.x = gamepad.axes[1];
									previousGamepad.axes.y = gamepad.axes[0];
								}

								// rotafte based on trackpad
								landscape.rotation.y += gamepad.axes[0]
										- previousGamepad.axes.y;

								landscape.rotation.x += gamepad.axes[1]
										- previousGamepad.axes.x;

								previousGamepad.axes.x = gamepad.axes[1];
								previousGamepad.axes.y = gamepad.axes[0];
							} else {
								previousGamepad.axes.x = null;
								previousGamepad.axes.y = null;
							}
						}
					}
				}

				if (resetPos) {
					previousGamepad.position.x = null;
					previousGamepad.position.y = null;
					previousGamepad.position.z = null;
				}

				// handle controllerRay		
				if (controller1.ray.visible) {
					var matrix = new THREE.Matrix4();
					matrix.extractRotation(controller1.matrix);

					var direction = new THREE.Vector3(0, 0, 1);
					direction.applyMatrix4(matrix);
					direction.multiplyScalar(-1);

					var globalController = new THREE.Vector3();
					globalController
							.setFromMatrixPosition(controller1.matrixWorld);

					if (!controller1.ray.counterRunning) {

						var intersectedObj = renderingContext.raycasting(
								globalController, direction, false);

						if (intersectedObj) {

							var type = intersectedObj.userData.type;

							controller1.ray.counterRunning = true;
							setTimeout(function() {
								controller1.ray.counterRunning = false;
							}, 300);

							if (controller1.sideButtonPressed
									&& type == "package") {
								@explorviz.visualization.engine.threejs.ThreeJSWrapper::toggleOpenStatus(Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizObj);
							} else if (controller1.padPressed) {
								if (type == "package") {
									@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,intersectedObj.userData.explorVizObj);
								} else if (type == "class") {
									@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,null);
								}
							}
						}
					}
				}

				// check if controller is in sight (activate Leap if not)
				if ((controller1 && controller1.children[1])
						&& (controller2 && controller2.children[0])) {

					var frustum = new THREE.Frustum();
					var cameraViewProjectionMatrix = new THREE.Matrix4();

					camera.updateMatrixWorld();
					camera.matrixWorldInverse.getInverse(camera.matrixWorld);
					cameraViewProjectionMatrix.multiplyMatrices(
							camera.projectionMatrix, camera.matrixWorldInverse);
					frustum.setFromMatrix(cameraViewProjectionMatrix);

					var controller1Mesh = controller1.children[1].children[0];
					var controller1Geometry = controller1Mesh.geometry;

					var controller2Mesh = controller2.children[1].children[0];
					var controller2Geometry = controller2Mesh.geometry;

					//console.log(controller2Mesh);

					controller1Geometry.computeBoundingBox();
					controller2Geometry.computeBoundingBox();

					if (controller1Mesh && controller2Mesh) {
						if (frustum.intersectsObject(controller1Mesh)
								|| (frustum.intersectsObject(controller2Mesh))) {
							leapVars.showHands = false;
						} else {
							console.log("true");
							leapVars.showHands = true;
						}
					}
				}
			}

			function handleLeap() {

				if (!leapVars.showHands) {

					leapVars.handMeshes.forEach(function(hand) {
						hand.hide();
					});

					leapRay.visible = false;
					renderingContext.crosshair.visible = false;
					return;
				}

				if (!leapVars.leapHand) {
					leapRay.visible = false;
					return;
				}

				leapVars.handMeshes.forEach(function(hand) {
					hand.show();
				});

				// hand is visible and rendered => update ray position

				//leapRay.visible = true;
				renderingContext.crosshair.visible = true;

				// below code is for intersection between landscape and leapHand !

				//			var bboxLandscape = new THREE.Box3().setFromObject(landscape);
				//
				//			var threeHand = scene
				//					.getObjectByName("hand-bone-0");
				//
				//			var bboxHand = new THREE.Box3().setFromObject(threeHand);
				//
				//			if (bboxHand.intersectsBox(bboxLandscape))
				//				console.log("intersecting");

				// index finger ray

				//			var indexFinger = new THREE.Vector3()
				//					.fromArray(leapVars.leapHand.indexFinger.tipPosition);
				//			var indexDirection = new THREE.Vector3()
				//					.fromArray(leapVars.leapHand.indexFinger.direction);
				//
				//			leapRay.setDirection(indexDirection.normalize());
				//			leapRay.position.x = indexFinger.x;
				//			leapRay.position.y = indexFinger.y;
				//			leapRay.position.z = indexFinger.z;

				//			if (leapRay.calculateLeapRay && !leapRay.counterRunning) {			
				//
				//				var intersectedObj = renderingContext.raycasting(indexFinger,
				//						indexDirection, false);
				//
				//				leapRay.counterRunning = true;
				//				setTimeout(function() {
				//					leapRay.counterRunning = false;
				//				}, 300);
				//
				//				if (intersectedObj) {
				//
				//					var type = intersectedObj.userData.type;
				//
				//					if (type == "package") {
				//						@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,intersectedObj.userData.explorVizObj);
				//					} else if (type == "class") {
				//						@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,null);
				//					}
				//				}
				//			}
			}

			// 

			function initControllers() {
				// add vive controllers to scene
				controller1 = new THREE.ViveController(0);
				controller1.standingMatrix = renderingContext.vrControls
						.getStandingMatrix();
				controller1.ray.visible = true;
				controller1.ray.counterRunning = false;
				controller1.name = "controller1";
				controller1.sideButtonPressed = false;
				controller1.padPressed = false;
				scene.add(controller1);

				controller2 = new THREE.ViveController(1);
				controller2.standingMatrix = renderingContext.vrControls
						.getStandingMatrix();
				controller2.ray.visible = false;
				scene.add(controller2);

				var loader = new THREE.OBJLoader();

				loader.load('js/threeJS/' + 'vr_controller_vive_1_5.obj',
						function(object) {

							var loader = new THREE.TextureLoader();

							var controller = object.children[0];
							controller.material.map = loader.load('js/threeJS/'
									+ 'onepointfive_texture.png');
							controller.material.specularMap = loader
									.load('js/threeJS/'
											+ 'onepointfive_spec.png');

							controller1.add(object.clone());
							controller2.add(object.clone());

						});
			}

			// init Leap Motion loop and gesture recognition
			function initLeap() {

				var initialIndex = 0;

				leapController = Leap.loop({
					enableGestures : true
				}, function(frame) {
					if (leapVars.showHands && frame.valid
							&& frame.hands.length > 0) {
						leapVars.leapHand = frame.hands[0];

						if (frame.gestures.length > 0
								&& leapVars.leapHand.type == 'right') {
							frame.gestures.forEach(function(gesture) {
								switch (gesture.type) {
								case "keyTap":
									handleClicks(true);
									//leapRay.calculateLeapRay = true;
									break;
								case "screenTap":
									handleClicks(false);
									break;
								default:
									//leapRay.calculateLeapRay = false;
									break;
								}
							});
						} else {
							leapRay.calculateLeapRay = false;
						}

						if (frame.hands[0] != null) {
							leapVars.currentHands = frame.hands;
							if (leapVars.currentHands == null) {
								leapVars.previousHands = frame.hands;
							} else {
								leapVars.previousHands = leapController
										.frame(1).hands;
								gestureRecognition();
							}
						} else {
							leapVars.currentHands = null;
							leapVars.previousHands = null;
						}
					} else {
						leapVars.leapHand = null;
					}
				});

				leapController.on('handMeshUsed', function(hand) {
					leapVars.handMeshes.push(hand);
				});

				leapController.use('transform', {
					vr : true,
					effectiveParent : camera
				});

				leapController.use('boneHand', {
					scene : scene,
					arm : true
				});

				// add leap index finger ray to scene
				// TODO Check if smoothing fixes jitter
				var dir = new THREE.Vector3(0, 0, 1);
				var origin = new THREE.Vector3(0, 0, 0);

				leapRay = new THREE.ArrowHelper(dir, origin, 100, 0x000000, 1,
						1);
				leapRay.name = "LeapRay";
				leapRay.visible = false;
				leapRay.calculateLeapRay = false;
				leapRay.counterRunning = false;
				scene.add(leapRay);
			}

			// Gesture Recognition
			function gestureRecognition() {
				if (checkHands()) {
					translation();
					rotation();
				}
			}

			function checkHands() {
				var previousHandsAvail = typeof leapVars.previousHands != 'undefined';
				var currentHandsAvail = typeof leapVars.currentHands != 'undefined';
				var sameCountOfHands = leapVars.previousHands.length == leapVars.currentHands.length;
				var maxTwoHands = leapVars.currentHands.length == 1
						|| leapVars.currentHands.length == 2;
				return previousHandsAvail && currentHandsAvail
						&& sameCountOfHands && maxTwoHands;
			}

			function translation() {
				var zoomIdx = 0;
				var transIdx = 1;
				var transZoomTimerIdx = 2;
				if (flags[zoomIdx] > 1)
					return;
				leapVars.currentHands
						.forEach(function(element, index) {
							if (element.grabStrength >= 0.95
									&& element.type == "right") {
								// check for: new hand in view or hand reappeared
								// => id change => anchor reset
								if (leapVars.anchors.translation != null
										&& leapVars.anchors.translation.id != element.id) {
									flags[transIdx] = 0;
									flags[transZoomTimerIdx] = 0;
								}
								// set anchor and start timer for
								// (non-)intentional interaction
								if (flags[transIdx] == 0) {
									flags[transZoomTimerIdx] = 0;
									flags[transIdx] = 1;
									leapVars.anchors.translation = element;
									if (flags[transZoomTimerIdx] == 0) {
										flags[transZoomTimerIdx] = 1;
										setTimeout(function() {
											flags[transZoomTimerIdx] = 2;
										}, 250);
									}
								}
								// check if intentional
								if (flags[transIdx] == 1) {
									if ((Math
											.abs(element.palmPosition[0]
													- leapVars.anchors.translation.palmPosition[0]) > 0.07 || Math
											.abs(element.palmPosition[1]
													- leapVars.anchors.translation.palmPosition[1]) > 0.07)
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
									var previousHand = leapController
											.frame(1)
											.hand(
													leapVars.anchors.translation.id);
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
				var rotIdx = 5;
				var rotTimerIdx = 6;
				leapVars.currentHands
						.forEach(function(element, index) {
							if (element.grabStrength >= 0.95
									&& element.type == "left") {
								// check for: new hand in view or hand reappeared
								// => id change => anchor reset
								if (leapVars.anchors.rotation != null
										&& leapVars.anchors.rotation.id != element.id) {
									flags[rotIdx] = 0;
									flags[rotTimerIdx] = 0;
								}
								// set anchor and start timer for
								// (non-)intentional interaction
								if (flags[rotIdx] == 0) {
									flags[rotTimerIdx] = 0;
									flags[rotIdx] = 1;
									leapVars.anchors.rotation = element;
									if (flags[rotTimerIdx] == 0) {
										flags[rotTimerIdx] = 1;
										setTimeout(function() {
											flags[rotTimerIdx] = 2;
										}, 250);
									}
								}
								// check if intentional
								if (flags[rotIdx] == 1) {
									if ((Math
											.abs(element.palmPosition[0]
													- leapVars.anchors.rotation.palmPosition[0]) > 0.07 || Math
											.abs(element.palmPosition[1]
													- leapVars.anchors.rotation.palmPosition[1]) > 0.07)
											&& flags[rotTimerIdx] == 2) {
										flags[rotIdx] = 2;
									} else {
										if (flags[rotIdx] == 2)
											flags[rotIdx] = 0;
										return;
									}
								}
								// proceed with calculation if intentional 
								if (flags[rotIdx] == 2) {
									var previousHand = leapController.frame(1)
											.hand(leapVars.anchors.rotation.id);
									if (previousHand == null)
										return;
									var movementX = (element.palmPosition[0] - previousHand.palmPosition[0]);
									var movementY = (previousHand.palmPosition[1] - element.palmPosition[1]);

									landscape.rotation.x += movementY * 20;
									landscape.rotation.y += movementX * 20;

									return;
								}
							} else if (element.type == "left") {
								// reset
								flags[rotIdx] = 0;
								flags[rotTimerIdx] = 0;
							}
						});
			}

			function handleClicks(open) {

				var openFlagIndex = 3;
				var highlightFlagIndex = 4;

				var intersectedObj = renderingContext.raycasting(camera
						.getWorldPosition(), camera.getWorldDirection(), false);

				var type;

				if (intersectedObj) {

					type = intersectedObj.userData.type;

					if (!open) {

						if (flags[highlightFlagIndex] == 0) {

							flags[highlightFlagIndex] = 1;
							setTimeout(function() {
								flags[highlightFlagIndex] = 0;
							}, 300);
							if (type == "package") {
								@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,intersectedObj.userData.explorVizObj);
							} else if (type == "class") {
								@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,null);
							}
						}

					} else {

						if (flags[openFlagIndex] == 0) {

							flags[openFlagIndex] = 1;
							setTimeout(function() {
								flags[openFlagIndex] = 0;
							}, 300);

							@explorviz.visualization.engine.threejs.ThreeJSWrapper::toggleOpenStatus(Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizObj);
						}
					}
				}
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
}
