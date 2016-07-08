package explorviz.visualization.engine.main;

public class WebVRJS {

	// Alternative to browser console (better due to fullscreen)
	// @explorviz.visualization.engine.Logging::log(Ljava/lang/String;)("debug");

	public static native void goFullScreen() /*-{

		var renderingContext = $wnd.renderingObj;

		var THREE = $wnd.THREE;

		renderingContext.vrEffect.requestPresent();

		$wnd.jQuery("#view-wrapper").css("cursor", "none")

		var controller1, controller2;

		// controllers

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

		// 

		function animate() {

			$wnd.requestAnimationFrame(animate);

			//
			var gamepads = navigator.getGamepads();

			//

			render();

		}

		function render() {

			renderingContext.vrControls.update();
			renderingContext.vrEffect.render(renderingContext.scene,
					renderingContext.camera);
		}

		animate();

	}-*/;

	public static native void resetSensor() /*-{
		var vrDisplay = $wnd.vrDisplay;
		if (vrDisplay)
			vrDisplay.resetPose();
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
