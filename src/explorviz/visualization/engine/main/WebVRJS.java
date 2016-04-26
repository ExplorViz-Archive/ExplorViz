package explorviz.visualization.engine.main;

public class WebVRJS {

	public static native void goFullScreen() /*-{

		var canvas = $doc.getElementById("webglcanvas");
		var divContainer = $doc.getElementById("webglDiv");
		var foreground = $doc.getElementById("leapcanvas");

		@explorviz.visualization.engine.main.WebGLStart::setWebVRMode(Z)(true)
		@explorviz.visualization.engine.navigation.TouchNavigationJS::changeTapInterval(I)(500)
		$wnd.jQuery("#view-wrapper").css("cursor", "none")

		//////////////////////////////
		// Leap Motion scene set-up //
		//////////////////////////////

		//foreground.style.top = 0;
		//foreground.style.left = 0;
		//foreground.style.position = 'absolute';

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

		var controller = $wnd.Leap.loop({
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
				frame.gestures.forEach(function(gesture) {
					switch (gesture.type) {
					case "screenTap":
						handleClicks();
						break;
					}
				});
			}
		});

		$wnd.Leap.loopController.use('transform', {
			vr : true,
			effectiveParent : camera

		});

		$wnd.Leap.loopController.use('boneHand', {
			scene : scene,
			arm : true,
		//jointColor : (new $wnd.THREE.Color).setHex(0xff0000),
		//boneColor : (new $wnd.THREE.Color).setHex(0x000000)
		});

		// As alternative to the boneHand plugin, we can draw the hands via 
		// riggedHand plugin.

		//		$wnd.Leap.loopController.use('riggedHand', {
		//			parent : scene,
		//			renderer : renderer,
		//			camera : camera,
		//			renderFn : function() {
		//				renderer.render(scene, camera);
		//			},
		//			boneColors : function(boneMesh, leapHand) {
		//				return {
		//					hue : 0.0,
		//					saturation : 1.0,
		//					lightness : 0.5
		//				};
		//			}
		//		});
		//
		//		$wnd.Leap.loopController.on('riggedHand.meshAdded', function(handMesh, leapHand) {
		//			handMesh.material.opacity = 1;
		//		});

		var vrControls = new $wnd.THREE.VRControls(camera, function(message) {
		});

		var vrEffect = new $wnd.THREE.VREffect(renderer, function(message) {
		});

		vrEffect.setFullScreen(true);

		var requestId = null;

		//////////////////////////////
		///    Render textures     ///
		//////////////////////////////

		var camera2, scene2, renderer2, geometry2, material2, mesh2, texture2, texture3;

		var vertShader = $doc.getElementById('vertexShader').innerHTML;
		var fragShader = $doc.getElementById('fragmentShader').innerHTML;

		texture2 = new $wnd.THREE.Texture(canvas);
		texture3 = new $wnd.THREE.Texture(foreground);
		texture2.minFilter = $wnd.THREE.LinearFilter;
		texture3.minFilter = $wnd.THREE.LinearFilter;

		var uniforms = {
			tOne : {
				type : "t",
				value : texture2
			},
			tSec : {
				type : "t",
				value : texture3
			}
		};

		var material_shh = new $wnd.THREE.ShaderMaterial({
			uniforms : uniforms,
			vertexShader : vertShader,
			fragmentShader : fragShader
		});

		var c2 = $doc.createElement('canvas');
		c2.id = 'oculusCanvas'

		scene2 = new $wnd.THREE.Scene();

		camera2 = new $wnd.THREE.PerspectiveCamera(75, viewportWidth / viewportHeight, 0.1, 10000);

		camera2.position.z = 500;
		scene2.add(camera2);

		geometry2 = new $wnd.THREE.PlaneGeometry(viewportWidth, viewportHeight);

		mesh2 = new $wnd.THREE.Mesh(geometry2, material_shh);
		scene2.add(mesh2);

		renderer2 = new $wnd.THREE.WebGLRenderer({
			canvas : c2
		});
		renderer2.setSize(viewportWidth, viewportHeight);

		$wnd.jQuery("#webglcanvas").hide();
		$wnd.jQuery("#leapcanvas").hide();
		$wnd.jQuery("#webGLCanvasDiv").append(c2);

		//////////////////////////////
		///  Choose canvas for HMD ///
		//////////////////////////////

		//		if ($wnd.vrDisplay.isPresenting) {
		//			$wnd.vrDisplay.exitPresent();
		//		} else {
		//			$wnd.vrDisplay.requestPresent([ {
		//				source : c2
		//			} ]);
		//
		//		}

		//
		//		@explorviz.visualization.engine.navigation.TouchNavigationJS::setTapRecognizer(Z)(true)
		//		@explorviz.visualization.engine.navigation.TouchNavigationJS::changeTapInterval(I)(250)
		//		@explorviz.visualization.engine.main.WebGLStart::setWebVRMode(Z)(false)
		//		$wnd.jQuery("#view-wrapper").css("cursor", "auto")

		//		removeLeap();

		//		$doc.removeEventListener("fullscreenchange", changeHandler,
		//				false);

		//		@explorviz.visualization.engine.main.SceneDrawer::showVRObjects = false;

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

			var projectionMatrixLeftEye = PerspectiveMatrixFromVRFieldOfView(fovLeft, 0.1, 100000);
			@explorviz.visualization.engine.main.SceneDrawer::setPerspectiveLeftEye([F)(projectionMatrixLeftEye);
			var projectionMatrixRightEye = PerspectiveMatrixFromVRFieldOfView(fovRight, 0.1, 100000);
			@explorviz.visualization.engine.main.SceneDrawer::setPerspectiveRightEye([F)(projectionMatrixRightEye);
		}

		resizeFOV();

		// pointer lock
		var x = 320
		var y = 400
		var z = 0

		canvas.requestPointerLock = canvas.requestPointerLock;

		$doc.exitPointerLock = $doc.exitPointerLock;

		canvas.requestPointerLock();

		$doc.addEventListener("pointerlockchange", changeLockCallback, false);
		$doc.addEventListener("mousemove", mouseCallback, false);
		$doc.addEventListener("mousedown", mouseDown, false);

		function changeLockCallback() {
			if (($doc.pointerLockElement === canvas) || ($doc.mozPointerLockElement === canvas)
					|| ($doc.webkitPointerLockElement === canvas)) {
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
			$doc.removeEventListener("mousemove", mouseCallback, false);
			$doc.removeEventListener("mousedown", mouseDown, false);
		}

		/////////////////////////////////////////////////////////////////////////////////
		//////////// Alternative to browser console (better due to fullscreen) //////////
		//                                                                             //
		// @explorviz.visualization.engine.Logging::log(Ljava/lang/String;) ("debug"); //
		/////////////////////////////////////////////////////////////////////////////////

		/////////////////////
		// scene rendering //
		/////////////////////

		function render() {

			vrControls.update();
			vrEffect.render(scene, camera);

			texture2.needsUpdate = true;
			texture3.needsUpdate = true;

			renderer2.render(scene2, camera2);

			requestId = requestAnimationFrame(render);
		}

		render();

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
					vrControls.update();
					vrEffect.render(scene, camera);
					renderer2.render(scene2, camera2);
					vrDisplay.submitFrame(pose);
				}
			}
		}

		$wnd.requestAnimationFrame(onAnimationFrame);

		/////////////////////////
		// Gesture recognition //
		/////////////////////////

		function gestureDetection() {

			if (checkHands()) {
				zoom();
				translation();
				rotation();
			}

		}

		function checkHands() {
			var previousHandsAvail = typeof previousHands != 'undefined';
			var currentHandsAvail = typeof currentHands != 'undefined';
			var sameCountOfHands = previousHands.length == currentHands.length;
			var maxTwoHands = currentHands.length == 1 || currentHands.length == 2;

			return previousHandsAvail && currentHandsAvail && sameCountOfHands && maxTwoHands;
		}

		var flags = new Array(0, 0, 0, 0, 0, 0);
		var frameCounter = 0;

		var anchorTransZoom;
		var anchorRot;

		function translation() {

			var zoomIdx = 0;
			var transIdx = 1;
			var transZoomTimerIdx = 2;

			if (flags[zoomIdx] > 1) {
				return;
			}

			currentHands
					.forEach(function(element, index) {
						if ((element.grabStrength >= 0.95) && (element.type == "right")) {

							// check for: new hand in view or hand reappeared
							// => id change => anchor reset
							if ((anchorTransZoom != null) && (anchorTransZoom.id != element.id)) {
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

								var previousHand = controller.frame(1).hand(anchorTransZoom.id);

								if (previousHand == null)
									return;

								var movementX = (element.palmPosition[0] - previousHand.palmPosition[0])
										* (viewportWidth);
								var movementY = (previousHand.palmPosition[1] - element.palmPosition[1])
										* (viewportWidth);

								x += movementX;
								y += movementY;

								@explorviz.visualization.engine.navigation.Navigation::mouseMoveVRHandler(IIZZ)(x, y, true, false);

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
						if ((element.grabStrength >= 0.95) && (element.type == "left")) {

							// check for: new hand in view or hand reappeared
							// => id change => anchor reset
							if ((anchorRot != null) && (anchorRot.id != element.id)) {
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
								if ((Math.abs(element.palmPosition[0] - anchorRot.palmPosition[0]) > 0.07 || Math
										.abs(element.palmPosition[1] - anchorRot.palmPosition[1]) > 0.07)
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

								var previousHand = controller.frame(1).hand(anchorRot.id);

								if (previousHand == null)
									return;

								var movementX = (element.palmPosition[0] - previousHand.palmPosition[0])
										* (viewportWidth);
								var movementY = (previousHand.palmPosition[1] - element.palmPosition[1])
										* (viewportWidth);

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

			if (flags[transIdx] > 1) {
				return;
			}

			currentHands
					.forEach(function(element, index) {
						if ((element.grabStrength >= 0.95) && (element.type == "right")) {

							// check for: new hand in view or hand reappeared
							// => id change => anchor reset
							if ((anchorTransZoom != null) && (anchorTransZoom.id != element.id)) {
								flags[transIdx] = 0;
								flags[transZoomTimerIdx] = 0;
							}

							frameCounter = ++frameCounter % 6;

							if (frameCounter != 0) {
								return;
							}

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
								var previousHand = controller.frame(1).hand(anchorTransZoom.id);

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

		function handleClicks() {

			var clickedOnceIdx = 3;

			if (flags[clickedOnceIdx] == 0) {
				flags[clickedOnceIdx] = 1;

				setTimeout(function() {
					if (flags[clickedOnceIdx] == 1) {
						flags[clickedOnceIdx] = 0;
						@explorviz.visualization.engine.navigation.Navigation::mouseSingleClickHandler(II)(0,0);
					}

				}, 1000);

			} else {
				flags[clickedOnceIdx] = 0;
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

		// vertex shader
		$wnd
				.jQuery("#view-wrapper")
				.append("<script id='vertexShader' type='x-shader/x-vertex'> varying vec2 vUv; void main() { vUv = uv; vec4 mvPosition = modelViewMatrix * vec4( position, 1.0 ); gl_Position = projectionMatrix * mvPosition; } </script>")

		// fragment shader
		$wnd
				.jQuery("#view-wrapper")
				.append("<script id='fragmentShader' type='x-shader/x-fragment'> uniform sampler2D tOne; uniform sampler2D tSec; varying vec2 vUv; void main(void) { vec3 c; vec4 Ca = texture2D(tOne, vUv); vec4 Cb = texture2D(tSec, vUv); c = Ca.rgb * Ca.a + Cb.rgb * Cb.a; gl_FragColor = vec4( mix( Ca.rgb, Cb.rgb, Cb.a ), 1.0 );}</script>")

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
			@explorviz.visualization.engine.navigation.Camera::moveXInVR(F)(vrState.position[0]*RADTODEG*-2);
			@explorviz.visualization.engine.navigation.Camera::moveYInVR(F)(vrState.position[1]*RADTODEG*-3);
			@explorviz.visualization.engine.navigation.Camera::moveZInVR(F)(vrState.position[2]*RADTODEG*-3);
		}
	}-*/;
}
