package explorviz.visualization.renderer;

/**
 * First prototype for switching the 3D visualization from plain WebGL towards
 * ThreeJS
 *
 * @author Christian Zirkelbach
 *
 */

public class ThreeJSRenderer {

	public static native void mouseHandler() /*-{

		var THREE = $wnd.THREE;
		var canvas = $wnd.canvas;
		var camera = $wnd.camera;
		var instances = $wnd.landscapeInstances;
		var system = $wnd.landscapeSystem;
		var packages = $wnd.landscapePackages;
		var textmesh = $wnd.textMesh;

		var mouseX = 0, mouseY = 0;
		var mouseDownLeft = false, mouseDownRight = false;
		var mouseWheelPressed = false;
		var cameraTranslateX = 0, cameraTranslateY = 0;
		// low value => high speed
		var movementSpeed = 100;
		var mouse = new THREE.Vector2();
		mouse.leftClicked = false;

		function onMouseMove(evt) {
			if (!mouseDownLeft && !mouseDownRight) {
				return;
			}

			evt.preventDefault();

			// reset possible left click -> no raycasting when mouse moves
			mouse.leftClicked = false;

			// rotate around center of mesh group
			if (mouseDownRight) {
				var deltaX = evt.clientX - mouseX, deltaY = evt.clientY - mouseY;
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
				mouse.leftClicked = false;
			}

			// translation
			// !right && left
			else if (btnCode == 1) {
				mouseDownLeft = true;
				mouseDownRight = false;
				cameraTranslateX = evt.clientX;
				cameraTranslateY = evt.clientY;

				mouse.leftClicked = true;
			} else {
				mouse.leftClicked = false;
			}
		}

		function onMouseUp(evt) {
			evt.preventDefault();

			// normalize coordinates
			mouse.x = (evt.clientX / $wnd.innerWidth) * 2 - 1;
			mouse.y = -(evt.clientY / $wnd.innerHeight) * 2 + 1;

			raycasting();

			mouse.leftClicked == false;
			mouseDownLeft = false;
			mouseDownRight = false;
		}

		function onMouseWheelPressed(evt) {
			var delta = Math.max(-1, Math.min(1, (evt.wheelDelta || -evt.detail)));

			mouseWheelPressed = true;
			zoomCamera(delta);
			mouseWheelPressed = false;
		}

		var raycaster = new THREE.Raycaster();
		var mouse = new THREE.Vector2();
		var INTERSECTED;
		var oldColor = new THREE.Color();

		function raycasting() {
			// ray has offset, need fix
			raycaster.setFromCamera(mouse, $wnd.camera);
			var intersections = raycaster.intersectObjects($wnd.scene.children);

			if (intersections.length > 0 && mouse.leftClicked == true) {

				var obj = intersections[0].object;

				if (INTERSECTED != obj) {
					if (INTERSECTED != undefined) {
						INTERSECTED.material.color.set(oldColor);
					}
					INTERSECTED = obj;
					oldColor.copy(obj.material.color);
					console.log(oldColor);
					obj.material.color.setRGB(1, 0, 0);
				} else {
					console.log(oldColor);
					obj.material.color.set(oldColor);
					INTERSECTED = null;
				}
			}
		}

		function rotateScene(deltaX, deltaY) {
			instances.rotation.y += deltaX / movementSpeed;
			instances.rotation.x += deltaY / movementSpeed;

			system.rotation.y += deltaX / movementSpeed;
			system.rotation.x += deltaY / movementSpeed;

			packages.rotation.y += deltaX / movementSpeed;
			packages.rotation.x += deltaY / movementSpeed;

			console.log(textmesh);

			//			textmesh.rotateY += deltaX / movementSpeed;
			//			textmesh.rotateX += deltaY / movementSpeed;
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

		$wnd.camera = new THREE.PerspectiveCamera(75, viewportWidth / viewportHeight, 0.1, 1000);

		$wnd.camera.position.z = 20;

		$wnd.canvas = document.createElement('canvas');
		$wnd.canvas.id = "threeCanvas";

		$wnd.scene = new THREE.Scene();
		var renderer = new THREE.WebGLRenderer({
			canvas : $wnd.canvas,
			antialias : true,
			alpha : true
		});

		renderer.setSize(viewportWidth, viewportHeight);

		// set background color to white
		renderer.setClearColor(0xffffff, 1);

		renderer.shadowMap.enabled = true;
		// soften the shadows
		renderer.shadowMapSoft = true;

		// Define the spotlight for the scene
		// TODO
		// needs "color" tuning to be like the origional ExplorViz 3D visualization
		// basic hex colors are identical to ExplorViz
		var spotLight = new THREE.SpotLight(0xffffff, 1.3, 1000, 1.56, 0, 0);
		spotLight.position.set(100, 100, 100);
		//		spotLight.castShadow = false;
		//		spotLight.shadow.camera.near = 6;
		//		spotLight.shadow.camera.far = 13;
		$wnd.scene.add(spotLight);

		// allows to debug the spotlight
		//		var spotLightHelper = new THREE.SpotLightHelper(spotLight);
		//		scene.add(spotLightHelper);

		// inserting objects
		createSystem($wnd.scene);
		createPackages($wnd.scene);
		createInstances($wnd.scene);

		createArrowHelpers($wnd.scene);

		var font = undefined;

		// List of texts and related positions
		var textList = [ {
			text : "upper",
			position : {
				x : 1,
				y : 1,
				z : 1
			}
		}, {
			text : "lower",
			position : {
				x : 1,
				y : 3,
				z : 1
			}
		} ];

		loadFont(textList);

		//		createLabel(scene);

		// allow receiving shadow
		//		$wnd.landscapeInstances.receiveShadow = true;
		//		$wnd.landscapeSystem.receiveShadow = true;
		//		$wnd.landscapePackages.receiveShadow = true;

		// rotates the model towards 45 degree and zooms out
		resetCamera();

		// inject into website
		$wnd.jQuery("#webglcanvas").hide();
		$wnd.jQuery("#webglDiv").append($wnd.canvas);

		// possible option for future work:
		// reposition camera if translating objects is not working, e.g.
		// camera.position.set(0,-12,5);
		// camera.lookAt(new THREE.Vector3( 0, 5, 0 ));

		// Rendering Section
		animate();

		function animate() {
			requestAnimationFrame(animate);
			render();
		}

		function render() {

			renderer.render($wnd.scene, $wnd.camera);
		}

		// Functions
		// Loads the font and create afterwards the texts
		function loadFont(textList) {
			var loader = new THREE.FontLoader();
			loader.load('js/threeJS/fonts/helvetiker_regular.typeface.js', function(response) {
				font = response;
				createTexts(textList);
			});
		}

		// creates texts and places them on a given position
		// TODO
		// cannot access textMesh outside this "inner" function => no rotation possible;
		function createTexts(textList) {

			var textGeo;
			var textMesh;

			var textMaterial = new THREE.MeshPhongMaterial({
				color : 0x0000000,
				specular : 0x000000
			});

			for (var i = 0; i < textList.length; i++) {
				textGeo = new THREE.TextGeometry(textList[i].text, {
					font : font,
					size : 0.4,
					height : 0.01,
					curveSegments : 12,
				});

				textGeo.computeBoundingBox();
				textGeo.computeVertexNormals();

				textMesh = new THREE.Mesh(textGeo, textMaterial);

				textMesh.position.x = textList[i].position.x;
				textMesh.position.y = textList[i].position.y;
				textMesh.position.z = textList[i].position.z;

				$wnd.scene.add(textMesh);
			}

		}

		//		// Testing adding a label
		//		function createLabel(scene) {
		//
		//			var texts = [ "upper", "lower" ];
		//
		//			// Regarding undefined reference in helvetiker_regular.typeface.js (chrome console log)
		//			// see: https://github.com/mrdoob/three.js/issues/7360#issuecomment-148841200
		//			// Nevertheless the label is drawn
		//			// see also: https://github.com/mrdoob/three.js/issues/7360#issuecomment-183119398
		//			// and https://jsfiddle.net/287rumst/1/			
		//			// there is no undefined reference in chrome's log (still, same code)
		//			// but error only occurs once. Bug with GWT?
		//
		//			var loader = new THREE.FontLoader();
		//			loader.load('js/threeJS/fonts/helvetiker_regular.typeface.js',
		//					function(font) {
		//
		//						$wnd.opts = font;
		//
		//						console.log(font);
		//
		//						var textGeo = new THREE.TextGeometry(texts[0], {
		//							font : font,
		//							size : 0.4,
		//							height : 0.01,
		//							curveSegments : 12,
		//							bevelThickness : 1,
		//							bevelSize : 1,
		//							bevelEnabled : false
		//						});
		//
		//						textGeo.computeBoundingBox();
		//
		//						var textMaterial = new THREE.MeshPhongMaterial({
		//							color : 0x0000000,
		//							//color : 0xFFFFFFF,
		//							specular : 0x000000
		//						});
		//						var textMesh = new THREE.Mesh(textGeo, textMaterial);
		//
		//						textMesh.position.x = 1;
		//						textMesh.position.y = 1;
		//						textMesh.position.z = 1;
		//
		//						scene.add(textMesh);
		//					});
		//		}

		// Resets the camera/model towards an predefined position (45 degree)
		function resetCamera() {
			var rotationX = 0.57;
			var rotationY = -0.76;
			var cameraPositionZ = 20;

			$wnd.landscapeSystem.rotation.x = rotationX;
			$wnd.landscapeSystem.rotation.y = rotationY;
			$wnd.landscapePackages.rotation.x = rotationX;
			$wnd.landscapePackages.rotation.y = rotationY;
			$wnd.landscapeInstances.rotation.x = rotationX;
			$wnd.landscapeInstances.rotation.y = rotationY;
			$wnd.camera.position.z = cameraPositionZ;

			//			$wnd.textMesh.rotation.x = rotationX;
			//			$wnd.textMesh.rotation.y = rotationY;
		}

		// Testing adding a system
		function createSystem(scene) {
			var outerGeometrySystem = new THREE.Geometry();
			var sizeVectorSystem = new THREE.Vector3(15, 1, 15);
			var positionVectorSystem = new THREE.Vector3(0, -2, 0);

			var meshSystem = createBox(sizeVectorSystem, positionVectorSystem);
			outerGeometrySystem.merge(meshSystem.geometry, meshSystem.matrix);

			// translate center to (0,0,0)
			//		outerGeometrySystem.computeBoundingSphere();
			//		outerGeometrySystem.center();

			// color system
			var materialSystem = new THREE.MeshLambertMaterial();
			materialSystem.color = setColor("system");

			$wnd.landscapeSystem = new THREE.Mesh(outerGeometrySystem, materialSystem);
			$wnd.scene.add($wnd.landscapeSystem);
		}

		// Testing adding packages
		function createPackages(scene) {
			var packageSize = 13;
			var outerGeometryPackages = new THREE.Geometry();
			var sizeVectorPackages = new THREE.Vector3(packageSize, 1, packageSize);
			var positionVectorPackages = new THREE.Vector3(0, -1, 0);

			var meshPackages = createBox(sizeVectorPackages, positionVectorPackages);
			outerGeometryPackages.merge(meshPackages.geometry, meshPackages.matrix);

			// translate center to (0,0,0)
			//		outerGeometrySystem.computeBoundingSphere();
			//		outerGeometrySystem.center();

			// color package
			var materialPackages = new THREE.MeshLambertMaterial();
			//			materialPackages.color = setColor("background");
			materialPackages.color = setColor("foreground");

			$wnd.landscapePackages = new THREE.Mesh(outerGeometryPackages, materialPackages);
			$wnd.scene.add($wnd.landscapePackages);
		}

		/// Testing adding instances
		function createInstances(scene) {
			var outerGeometryInstances = new THREE.Geometry();
			var sizeFactor = 0.5;

			for (var i = 0; i < 3; i++) {
				var sizeVector = new THREE.Vector3(sizeFactor * 1, sizeFactor * 5, sizeFactor * 1);
				var positionVector = new THREE.Vector3(0, 0, 0);
				positionVector.x = 5 * i;
				var mesh = createBox(sizeVector, positionVector);
				outerGeometryInstances.merge(mesh.geometry, mesh.matrix);
			}

			// translate center to (0,0,0)
			outerGeometryInstances.computeBoundingSphere();
			outerGeometryInstances.center();

			// color instance
			var material = new THREE.MeshLambertMaterial();
			material.color = setColor("instance");

			$wnd.landscapeInstances = new THREE.Mesh(outerGeometryInstances, material);
			$wnd.scene.add($wnd.landscapeInstances);
		}

		// creates and positiones a parametric box
		function createBox(sizeVector, positionVector) {
			var material = new THREE.MeshBasicMaterial();
			material.color = new THREE.Color(0x000000);
			var cube = new THREE.BoxGeometry(sizeVector.x, sizeVector.y, sizeVector.z);

			var mesh = new THREE.Mesh(cube, material);

			mesh.position.set(positionVector.x, positionVector.y, positionVector.z);
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
			var axisHelper = new THREE.AxisHelper(5);
			$wnd.scene.add(axisHelper);
		}

	}-*/;

	public static void init() {
		drawPrototypeLandscape();
		mouseHandler();
	}
}
