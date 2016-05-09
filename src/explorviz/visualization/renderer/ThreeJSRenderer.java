package explorviz.visualization.renderer;

public class ThreeJSRenderer {

	public static native void mouseHandler() /*-{

		var canvas = $wnd.canvas;
		var camera = $wnd.camera;
		var instances = $wnd.landscapeInstances;
		var system = $wnd.landscapeSystem;
		var packages = $wnd.landscapePackages;
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
			// first object "instances"
			instances.rotation.y += deltaX / movementSpeed;
			instances.rotation.x += deltaY / movementSpeed;

			// second object "system"
			system.rotation.y += deltaX / movementSpeed;
			system.rotation.x += deltaY / movementSpeed;

			// third object "system"
			packages.rotation.y += deltaX / movementSpeed;
			packages.rotation.x += deltaY / movementSpeed;
		}

		function translateCamera(deltaX, deltaY) {
			camera.position.x -= deltaX / movementSpeed;
			camera.position.y += deltaY / movementSpeed;
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
			// TODO
			// forbid zooming through object?
		}

		canvas.addEventListener('mousemove', onMouseMove, false);
		canvas.addEventListener('mouseup', onMouseUp, false);
		canvas.addEventListener('mousedown', onMouseDown, false);
		canvas.addEventListener('mousewheel', onMouseWheelPressed, false);

	}-*/;

	public static native void drawPrototypeLandscape() /*-{

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
			canvas : $wnd.canvas
			antialias : true
		});

		renderer.setSize(viewportWidth, viewportHeight);

		// set background color to white
		renderer.setClearColor(0xffffff, 1);

		renderer.shadowMapEnabled = true;

		// sportlight
		spotlight = new THREE.SpotLight(0xFFFFFF, 1.0);
		spotlight.position.set(-400, 1200, 300);
		spotlight.angle = 20 * Math.PI / 180;
		spotlight.exponent = 1;
		spotlight.target.position.set(0, 200, 0);
		scene.add(spotlight);

		// inserting objects
		createInstances(scene);
		createSystem(scene);
		createPackages(scene);

		createArrowHelpers(scene);

		//		// testing shadows
		//		var spotlight = new THREE.LightShadow($wnd.camera);
		//		spotlight.castShadow = true;
		//		// opacity of the shadow. 0.0 no shadow, 1.0 means pure black shadow
		//		spotlight.shadowDarkness = 1.0;
		//
		//		// debugging the shadow
		//		spotlight.shadowCameraVisible = true;
		//
		//		$wnd.landscapeInstances.castShadow = true;
		//		$wnd.landscapeInstances.receiveShadow = true;

		// inject into website
		$wnd.jQuery("#webglcanvas").hide();
		$wnd.jQuery("#webglDiv").append($wnd.canvas);

		// Rendering Section
		animate();

		function animate() {
			requestAnimationFrame(animate);
			render();
		}

		function render() {
			renderer.render(scene, $wnd.camera);
		}

		// Functions
		function createPackages(scene) {
			var packageSize = 13;
			var outerGeometryPackages = new THREE.Geometry();
			var sizeVectorPackages = new THREE.Vector3(packageSize, 1,
					packageSize);
			var positionVectorPackages = new THREE.Vector3(0, -3, 0);

			var meshPackages = createBox(sizeVectorPackages,
					positionVectorPackages);
			outerGeometryPackages.merge(meshPackages.geometry,
					meshPackages.matrix);

			// translate center to (0,0,0)
			//		outerGeometrySystem.computeBoundingSphere();
			//		outerGeometrySystem.center();

			// color instance
			var materialPackages = new THREE.MeshBasicMaterial();
			materialPackages.color = setColor("background");

			$wnd.landscapePackages = new THREE.Mesh(outerGeometryPackages,
					materialPackages);
			scene.add($wnd.landscapePackages);
		}

		/// Testing adding instances
		function createInstances(scene) {
			var outerGeometryInstances = new THREE.Geometry();

			for (var i = 0; i < 3; i++) {
				var sizeVector = new THREE.Vector3(1, 5, 1);
				var positionVector = new THREE.Vector3(0, 0, 0);
				positionVector.x = 5 * i;
				var mesh = createBox(sizeVector, positionVector);
				outerGeometryInstances.merge(mesh.geometry, mesh.matrix);
			}

			// translate center to (0,0,0)
			outerGeometryInstances.computeBoundingSphere();
			outerGeometryInstances.center();

			// color instance
			var material = new THREE.MeshBasicMaterial();
			material.color = setColor("instance");

			$wnd.landscapeInstances = new THREE.Mesh(outerGeometryInstances,
					material);
			scene.add($wnd.landscapeInstances);
		}

		/// Testing adding a system
		function createSystem(scene) {
			var outerGeometrySystem = new THREE.Geometry();
			var sizeVectorSystem = new THREE.Vector3(15, 1, 15);
			var positionVectorSystem = new THREE.Vector3(0, -4, 0);

			var meshSystem = createBox(sizeVectorSystem, positionVectorSystem);
			outerGeometrySystem.merge(meshSystem.geometry, meshSystem.matrix);

			// translate center to (0,0,0)
			//		outerGeometrySystem.computeBoundingSphere();
			//		outerGeometrySystem.center();

			// color instance
			var materialSystem = new THREE.MeshBasicMaterial();
			materialSystem.color = setColor("system");

			$wnd.landscapeSystem = new THREE.Mesh(outerGeometrySystem,
					materialSystem);
			scene.add($wnd.landscapeSystem);
		}

		// creates and postiones a parametric box
		function createBox(sizeVector, positionVector) {
			var material = new THREE.MeshBasicMaterial();
			material.color = new THREE.Color(0x00ff00);
			var cube = new THREE.BoxGeometry(sizeVector.x, sizeVector.y,
					sizeVector.z);

			var mesh = new THREE.Mesh(cube, material);

			mesh.position.set(positionVector.x, positionVector.y,
					positionVector.z);
			mesh.updateMatrix();

			return mesh;
		}

		function setColor(name) {
			var color = new THREE.Color(0x000000);
			switch (name) {
			case "system":
				color.set(0xcecece);
				break;
			case "background":
				color.set(0x169e2b);
				break;
			case "foreground":
				color.set(0x00c143);
				break;
			case "instance":
				color.set(0x4818ba);
				break;
			case "communication":
				color.set(0xf9941d);
				break;
			default:
			}
			return color;
		}

		function createArrowHelpers(scene) {
			// Arrowhelper X
			var dir = new THREE.Vector3(1, 0, 0);
			var origin = new THREE.Vector3(0, 0, 0);
			var length = 1;
			var hex = 0xff0000;

			var arrowHelperX = new THREE.ArrowHelper(dir, origin, length, hex);

			// Arrowhelper Y
			dir = new THREE.Vector3(0, 1, 0);
			origin = new THREE.Vector3(0, 0, 0);
			length = 1;
			hex = 0x00ff00;

			var arrowHelperY = new THREE.ArrowHelper(dir, origin, length, hex);

			// Arrowhelper Z
			dir = new THREE.Vector3(0, 0, 1);
			origin = new THREE.Vector3(0, 0, 0);
			length = 1;
			hex = 0x0000ff;

			var arrowHelperZ = new THREE.ArrowHelper(dir, origin, length, hex);

			scene.add(arrowHelperX);
			scene.add(arrowHelperY);
			scene.add(arrowHelperZ);
		}

	}-*/;

	public static void init() {
		drawPrototypeLandscape();
		mouseHandler();
	}
}
