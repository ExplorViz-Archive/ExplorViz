package explorviz.visualization.renderer;

public class ThreeJSRenderer {

	public static native void mouseHandler() /*-{

		var canvas = $wnd.canvas;
		var camera = $wnd.camera;
		var landscape = $wnd.landscape;
		var bbox = $wnd.bbox;

		var mouseX = 0, mouseY = 0;
		var mouseDownLeft = false, mouseDownRight = false;
		var mouseWheelPressed = false;
		var cameraTranslateX = 0, cameraTranslateY = 0;
		// low value => high speed
		var movementSpeed = 100;

		function onMouseMove(evt) {
			if (!mouseDownLeft && !mouseDownRight) {
				return;
			}

			evt.preventDefault();

			// rotate around center of mesh group
			if (mouseDownRight) {
				var deltaX = evt.clientX - mouseX, deltaY = evt.clientY
						- mouseY;
				mouseX = evt.clientX;
				mouseY = evt.clientY;

				rotateScene(deltaX, deltaY);
			}
			// translate
			else if (mouseDownLeft) {
				var deltaX = evt.clientX - cameraTranslateX, deltaY = evt.clientY
						- cameraTranslateY;
				cameraTranslateX = evt.clientX;
				cameraTranslateY = evt.clientY;

				translateCamera(deltaX, deltaY);
			}
		}

		function onMouseDown(evt) {

			var btnCode = evt.which;
			evt.preventDefault();

			// rotation
			// right && !left
			if (btnCode == 3) {
				mouseDownLeft = false;
				mouseDownRight = true;
				mouseX = evt.clientX;
				mouseY = evt.clientY;
			}

			// translation
			// !right && left
			else if (btnCode == 1) {
				mouseDownLeft = true;
				mouseDownRight = false;
				cameraTranslateX = evt.clientX;
				cameraTranslateY = evt.clientY;
			}
		}

		function onMouseUp(evt) {
			evt.preventDefault();

			mouseDownLeft = false;
			mouseDownRight = false;
		}

		function onMouseWheelPressed(evt) {
			var delta = Math.max(-1, Math.min(1,
					(evt.wheelDelta || -evt.detail)));
			mouseWheelPressed = true;
			zoomCamera(delta);
			mouseWheelPressed = false;
		}

		function rotateScene(deltaX, deltaY) {
			// single object => correct rotation behaviour
			landscape.rotation.y += deltaX / movementSpeed;
			landscape.rotation.x += deltaY / movementSpeed;

			// single boundary box => correct rotation behaviour
			//			bbox.rotation.y += deltaX / movementSpeed;
			//			bbox.rotation.x += deltaY / movementSpeed;

		}

		function translateCamera(deltaX, deltaY) {
			camera.position.x -= deltaX / movementSpeed;
			camera.position.y += deltaY / movementSpeed;
			//			landscape.position.x += deltaX / movementSpeed;
			//			landscape.position.y -= deltaY / movementSpeed;
		}

		function zoomCamera(delta) {
			// zoom in
			if (delta > 0) {
				camera.position.z -= delta;
			}
			// zoom out
			else {
				camera.position.z -= delta;
			}
			// forbid zooming through object?
		}

		canvas.addEventListener('mousemove', onMouseMove, false);
		canvas.addEventListener('mouseup', onMouseUp, false);
		canvas.addEventListener('mousedown', onMouseDown, false);
		canvas.addEventListener('mousewheel', onMouseWheelPressed, false);

	}-*/;

	public static native void draw3JSBox() /*-{

		var THREE = $wnd.THREE;

		var viewportWidth = @explorviz.visualization.engine.main.WebGLStart::viewportWidth;
		var viewportHeight = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

		$wnd.camera = new THREE.PerspectiveCamera(75, viewportWidth
				/ viewportHeight, 0.1, 1000);

		$wnd.camera.position.z = 5;

		$wnd.canvas = document.createElement('canvas');
		$wnd.canvas.id = "threeCanvas";

		var scene = new THREE.Scene();
		var renderer = new THREE.WebGLRenderer({
			canvas : $wnd.canvas,
			antialias : true
		});
		renderer.setClearColor(0xffffff, 1);

		renderer.setSize(viewportWidth, viewportHeight);

		var cube = new THREE.BoxGeometry(1, 1, 1);
		var material = new THREE.MeshNormalMaterial();

		//		$wnd.landscape = new THREE.Object3D();
		//		for (var i = 0; i < 3; i++) {
		//			var mesh = new THREE.Mesh(cube, material);
		//			var center = new THREE.Vector3();
		//			center.x = 5 * i;
		//			mesh.position.set(center.x, center.y, center.z);
		//			$wnd.landscape.add(mesh);
		//		}

		var outerGeometry = new THREE.Geometry();

		for (var i = 0; i < 3; i++) {
			var mesh = new THREE.Mesh(cube, material);
			var center = new THREE.Vector3();
			center.x = 5 * i;
			mesh.position.set(center.x, center.y, center.z);
			mesh.updateMatrix();

			outerGeometry.merge(mesh.geometry, mesh.matrix);
		}

		outerGeometry.computeBoundingSphere();
		outerGeometry.center();

		$wnd.landscape = new THREE.Mesh(outerGeometry, material);
		scene.add($wnd.landscape);

		//		// Bounding Box for centering the rotation of the landscape
		//		$wnd.bbox = new THREE.BoundingBoxHelper($wnd.landscape, 0xffffff);
		//		//		$wnd.bbox.visible = false;
		//		$wnd.bbox.update();
		//		scene.add($wnd.bbox);

		// Arrowhelper
		var dir = new THREE.Vector3(1, 1, 1);
		var origin = new THREE.Vector3(0, 0, 0);
		var length = 1;
		var hex = 0xffff00;

		var arrowHelper = new THREE.ArrowHelper(dir, origin, length, hex);
		scene.add(arrowHelper)

		$wnd.jQuery("#webglcanvas").hide();
		$wnd.jQuery("#webglDiv").append($wnd.canvas);

		animate();

		function animate() {
			requestAnimationFrame(animate);
			render();
		}

		function render() {
			renderer.render(scene, $wnd.camera);
		}

	}-*/;

	public static void init() {
		draw3JSBox();
		mouseHandler();
	}
}
