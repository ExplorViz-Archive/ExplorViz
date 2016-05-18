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

		var scene = $wnd.scene;
		var landscape = $wnd.landscape;

		var mouseX = 0, mouseY = 0;
		var mouseDownLeft = false, mouseDownRight = false;
		var mouseWheelPressed = false;
		var cameraTranslateX = 0, cameraTranslateY = 0;

		// low value => high speed
		var movementSpeed = 100;
		var mouse = new THREE.Vector2();
		mouse.leftClicked = false;

		// get offset from parent element (navbar) : {top, left}
		var canvasOffset = $wnd.jQuery($wnd.canvas).offset();

		function onMouseMove(evt) {
			if (!mouseDownLeft && !mouseDownRight) {
				return;
			}

			evt.preventDefault();

			// reset possible left click -> no raycasting when mouse moves
			mouse.leftClicked = false;

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
			mouse.x = ((evt.clientX + canvasOffset.left) / $wnd.innerWidth) * 2 - 1;
			mouse.y = -((evt.clientY + canvasOffset.top) / $wnd.innerHeight) * 2 + 1;

			raycasting();

			mouse.leftClicked == false;
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

		var raycaster = new THREE.Raycaster();
		var mouse = new THREE.Vector2();
		var INTERSECTED;
		var oldColor = new THREE.Color();

		function raycasting() {
			// TODO
			// ray has a little offset, needs to be fixed
			// Maybe still an offset problem with the canvas?

			// update the picking ray with the camera and mouse position
			raycaster.setFromCamera(mouse, $wnd.camera);
			// calculate objects intersecting the picking ray (true => recursive)
			var intersections = raycaster
					.intersectObjects(scene.children, true);

			if (intersections.length > 0 && mouse.leftClicked == true) {

				var obj = intersections[0].object;

				if (INTERSECTED != obj) {
					if (INTERSECTED != undefined) {
						INTERSECTED.material.color.set(oldColor);
					}

					// update tooltip, if object has name
					if (obj.name) {
						updateTooltip(obj.name, true);
					}

					INTERSECTED = obj;
					oldColor.copy(obj.material.color);
					obj.material.color.setRGB(1, 0, 0);
				} else {
					updateTooltip("", false);
					obj.material.color.set(oldColor);
					INTERSECTED = null;
				}
			}
		}

		function updateTooltip(message, showing) {

			$wnd.tooltipContext.clearRect(0, 0, $wnd.tooltipCanvas.width,
					$wnd.tooltipCanvas.height);

			if (showing) {
				//var message = intersects[0].object.name;
				//var metrics = $wnd.tooltipContext.measureText(message);
				//var width = metrics.width;

				// draw background
				$wnd.tooltipContext.beginPath();
				$wnd.tooltipContext.fillStyle = "white";
				$wnd.tooltipContext.fillRect(20, 20, 150, 50);
				$wnd.tooltipContext.fill();

				// draw string
				$wnd.tooltipContext.beginPath();
				$wnd.tooltipContext.font = "Bold 20px Arial";
				$wnd.tooltipContext.textAlign = "center";
				$wnd.tooltipContext.textBaseline = "middle";
				$wnd.tooltipContext.fillStyle = "black";
				$wnd.tooltipContext.fillText(message, 80, 40);
				$wnd.tooltipContext.fill();

				$wnd.tooltipTexture.needsUpdate = true;

				var viewportWidth = @explorviz.visualization.engine.main.WebGLStart::viewportWidth;
				var viewportHeight = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;
				var canvasOffset = $wnd.jQuery($wnd.canvas).offset();

				var x = cameraTranslateX - viewportWidth / 2;
				var y = -((cameraTranslateY - canvasOffset.top) - viewportHeight / 2);

				// set(0,0,1) = center due to ortho
				$wnd.tooltipSprite.position.set(x, y, 1);
			} else {
				$wnd.tooltipTexture.needsUpdate = true;
			}

		}

		function rotateScene(deltaX, deltaY) {
			landscape.rotation.y += deltaX / movementSpeed;
			landscape.rotation.x += deltaY / movementSpeed;
			//			textMesh.rotation.y += deltaX / movementSpeed;
			//			textMesh.rotation.x += deltaY / movementSpeed;
		}

		function translateCamera(deltaX, deltaY) {
			camera.position.x -= deltaX / movementSpeed;
			camera.position.y += deltaY / movementSpeed;

			// TODO
			// fix textMesh changes position
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
		canvas.addEventListener('mousewheel', onMouseWheelPressed, false)

	}-*/;

	public static native void drawPrototypeLandscape() /*-{

		var THREE = $wnd.THREE;
		var Leap = $wnd.Leap;
		var vrControls;
		var vrEffect;

		var viewportWidth = @explorviz.visualization.engine.main.WebGLStart::viewportWidth;
		var viewportHeight = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

		// needs 0.1 near value for leap motion
		$wnd.camera = new THREE.PerspectiveCamera(75, viewportWidth
				/ viewportHeight, 0.1, 1000);

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

		// allows to resize the window => resize the canvas
		$wnd.addEventListener('resize', function() {
			var resizedWidth = viewportWidth;
			var resizedHeight = viewportHeight;

			if ($wnd.innerWidth <= viewportWidth) {
				resizedWidth = $wnd.innerWidth;
				if ($wnd.innerHeight <= viewportHeight) {
					resizedHeight = $wnd.innerHeight;
				}
			}

			renderer.setSize(resizedWidth, resizedHeight);
			$wnd.camera.aspect = resizedWidth / resizedHeight;
			$wnd.camera.updateProjectionMatrix();
		});

		// set background color to white
		renderer.setClearColor(0xffffff, 1);

		// To allow render sprite-overlay on top
		renderer.autoClear = false;

		renderer.shadowMap.enabled = true;
		// soften the shadows
		renderer.shadowMapSoft = true;

		// Define the spotlight for the scene
		// TODO
		// needs a little "color" tuning to be like the origional ExplorViz 3D visualization
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

		// container for all landscape related objects
		$wnd.landscape = new THREE.Group();
		$wnd.scene.add($wnd.landscape);

		var testSystem = createSystem($wnd.landscape);

		// default package height: y = 1.0
		// create some test packages
		var dataTestPackageA = {
			name : 'org',
			size : 13,
			instances : [ {
				name : 'doA',
				numOfCalls : 5
			}, {
				name : 'doB',
				numOfCalls : 10
			} ]
		};
		var dataTestPackageB = {
			name : 'neo4j',
			size : 8,
			instances : [ {
				name : 'doC',
				numOfCalls : 5
			}, {
				name : 'doD',
				numOfCalls : 10
			} ]
		};
		var dataTestPackageC = {
			name : 'unsafe',
			size : 3,
			instances : [ {
				name : 'doE',
				numOfCalls : 3
			}, {
				name : 'doF',
				numOfCalls : 7
			} ]
		};

		var testPackageA = createPackage(testSystem, dataTestPackageA);
		var testPackageB = createPackage(testPackageA, dataTestPackageB);
		var testPackageC = createPackage(testPackageB, dataTestPackageC)
		var testInstance1 = createInstance(testPackageC, dataTestPackageC);

		//		console.log(testPackageB);

		//		createInstances($wnd.landscape);
		//		createLabel($wnd.landscape);
		//		createAxisHelpers($wnd.scene);

		// create tooltips
		createTooltips();

		//		createLabel(scene);

		// rotates the model towards 45 degree and zooms out
		resetCamera();

		// inject into website
		$wnd.jQuery("#webglcanvas").hide();
		$wnd.jQuery("#webglDiv").append($wnd.canvas);

		// possible option for future work:
		// reposition camera if translating objects is not working, e.g.
		// camera.position.set(0,-12,5);
		// camera.lookAt(new THREE.Vector3( 0, 5, 0 ));

		// initialize Leap Motion
		initLeap();

		// Rendering Section
		animate();

		function animate() {
			requestAnimationFrame(animate);
			vrControls.update();
			render();
		}

		function render() {
			renderer.clear();
			vrEffect.render($wnd.scene, $wnd.camera);
			renderer.clearDepth();
			vrEffect.render($wnd.tooltipScene, $wnd.tooltipCamera);
		}

		// Functions
		function createTooltips() {
			$wnd.tooltipCamera = new THREE.OrthographicCamera(
					-viewportWidth / 2, viewportWidth / 2, viewportHeight / 2,
					-viewportHeight / 2, 1, 10);
			$wnd.tooltipCamera.position.z = 10;

			$wnd.tooltipScene = new THREE.Scene();

			$wnd.tooltipCanvas = document.createElement('canvas');
			$wnd.tooltipContext = $wnd.tooltipCanvas.getContext('2d');

			$wnd.tooltipTexture = new THREE.Texture($wnd.tooltipCanvas);
			$wnd.tooltipTexture.needsUpdate = true;
			$wnd.tooltipTexture.minFilter = THREE.LinearFilter;

			$wnd.tooltipMaterial = new THREE.SpriteMaterial({
				map : $wnd.tooltipTexture
			});

			$wnd.tooltipSprite = new THREE.Sprite($wnd.tooltipMaterial);
			$wnd.tooltipSprite.scale.set(200, 200, 1);

			$wnd.tooltipScene.add($wnd.tooltipSprite);
		}

		// create label on element
		function createLabel(scene) {
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

			var dynamicTexture = new $wnd.THREEx.DynamicTexture(512, 512);
			dynamicTexture.texture.needsUpdate = true;
			dynamicTexture.context.font = "bolder 90px Verdana";
			dynamicTexture.texture.anisotropy = renderer.getMaxAnisotropy()
			dynamicTexture.clear();

			var geometry = new THREE.PlaneGeometry(1, 1);
			var material = new THREE.MeshBasicMaterial({
				map : dynamicTexture.texture,
				transparent : true
			})
			$wnd.textMesh = new THREE.Mesh(geometry, material);
			// rotate 90 degrees
			$wnd.textMesh.rotateX(Math.PI * 2);

			$wnd.textMesh.position.x = 0;
			$wnd.textMesh.position.y = 0;
			$wnd.textMesh.position.z = 10;

			dynamicTexture.drawText(textList[0].text, 96, 256, 'black')
			$wnd.scene.add($wnd.textMesh);
		}

		// initializes the LEAP Motion library for gesture control
		function initLeap() {
			Leap.loop();

			Leap.loopController.use('transform', {
				vr : true,
				effectiveParent : $wnd.camera
			});

			Leap.loopController.use('boneHand', {
				scene : $wnd.scene,
				arm : true
			});

			vrControls = new THREE.VRControls($wnd.camera);
			vrEffect = new THREE.VREffect(renderer);

			// handler if necessary
			var onkey = function(event) {
				if (event.key === 'z' || event.keyCode === 122) {
					vrControls.zeroSensor();
				}
				if (event.key === 'f' || event.keyCode === 102) {
					console.log('f');
					return vrEffect.setFullScreen(true);
				}
			};
		}

		// creates texts and places them on a given position
		// TODO
		// cannot access textMesh outside this "inner" function => no rotation
		// possible;
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

		// Resets the camera/model towards an predefined position (45 degree)
		function resetCamera() {
			var rotationX = 0.57;
			var rotationY = -0.76;
			var cameraPositionZ = 20;

			$wnd.landscape.rotation.x = rotationX;
			$wnd.landscape.rotation.y = rotationY;
			$wnd.camera.position.z = cameraPositionZ;
		}

		// Testing adding a system at (0,0,0)
		// TODO
		// size depending on children
		function createSystem(parentObject) {
			var geometry = new THREE.Geometry();
			var size = new THREE.Vector3(15, 1, 15);
			var position = new THREE.Vector3(0, 0, 0);

			var mesh = createBox(size, position);
			geometry.merge(mesh.geometry, mesh.matrix);

			// color system
			var material = new THREE.MeshLambertMaterial();
			material.side = THREE.DoubleSide;
			material.color = createColor('system');

			var newSystem = new THREE.Mesh(geometry, material);

			// internal user-definded type
			newSystem.userData = {
				type : 'system'
			};

			parentObject.add(newSystem);
			return newSystem;
		}

		// adds a package to the parent object
		function createPackage(parentObject, packageDefintion) {
			var packageName = packageDefintion.name ? packageDefintion.name
					: '<unnamed package>';
			var packageSize = packageDefintion.size ? packageDefintion.size
					: 10;
			var geometry = new THREE.Geometry();
			var size = new THREE.Vector3(packageSize, 1, packageSize);

			var material = new THREE.MeshLambertMaterial();
			material.side = THREE.DoubleSide;
			material.color = createColor('black');

			var position = new THREE.Vector3(0, 0, 0);
			var mesh = createBox(size, position);
			geometry.merge(mesh.geometry, mesh.matrix);

			var newPackage = new THREE.Mesh(geometry, material);
			newPackage.name = packageName;

			newPackage.userData = {
				type : 'package'
			};

			// there is no parent package, just the system
			if (parentObject.userData.type == 'system') {
				newPackage.material.color = createColor('lightGreen');
			} else if (parentObject.userData.type == 'package') {
				// alternate colors for package hierarchy
				if (parentObject.material.color
						.equals(createColor('lightGreen'))) {
					newPackage.material.color = createColor('darkGreen');
				} else {
					newPackage.material.color = createColor('lightGreen');
				}
			}

			// adjust the height for hierarchy - based on parent package
			newPackage.translateY(1.0);

			parentObject.add(newPackage);
			return newPackage;
		}

		function createInstance(parentObject, instanceDefinition) {
			// first test with a single instance
			var firstInstance = instanceDefinition.instances[0];

			var instanceName = firstInstance.name ? firstInstance.name
					: '<unnamed instance>';
			var instanceNumOfCalls = firstInstance.numOfCalls ? firstInstance.numOfCalls
					: 10;

			var geometry = new THREE.Geometry();
			var sizeFactor = 0.5;
			var size = new THREE.Vector3(sizeFactor, sizeFactor
					* instanceNumOfCalls, sizeFactor);

			var position = new THREE.Vector3(0, 0, 0);
			var mesh = createBox(size, position);
			geometry.merge(mesh.geometry, mesh.matrix);

			var material = new THREE.MeshLambertMaterial();
			material.side = THREE.DoubleSide;
			material.color = createColor('instance');

			var newInstance = new THREE.Mesh(geometry, material);
			newInstance.name = instanceName;

			// internal user-definded type
			newInstance.userData = {
				type : 'instance',
				numOfCalls : instanceNumOfCalls
			};

			// TODO
			// currently invalid centering of box
			newInstance.translateY(1.0);

			parentObject.add(newInstance);

			// TODO fix iteration for rearrangement
			// Rearrange instances, if necessary
			parentObject.traverse(function(child) {
				if ((child instanceof THREE.Mesh)
						&& (child.userData.type == 'instance')) {
					console.log(child);
				}
			});

			return newInstance;
		}

		// creates and positiones a parametric box
		function createBox(sizeVector, positionVector) {
			var material = new THREE.MeshBasicMaterial();
			material.color = createColor('black');
			var cube = new THREE.BoxGeometry(sizeVector.x, sizeVector.y,
					sizeVector.z);

			var mesh = new THREE.Mesh(cube, material);

			mesh.position.set(positionVector.x, positionVector.y,
					positionVector.z);
			mesh.updateMatrix();

			return mesh;
		}

		function createColor(name) {
			var color = new THREE.Color(0x000000);
			switch (name) {
			case 'system':
				color.set(0xcecece);
				break;
			case 'darkGreen':
				color.set(0x169e2b);
				break;
			case 'lightGreen':
				color.set(0x00c143);
				break;
			case 'instance':
				color.set(0x4818ba);
				break;
			case 'communication':
				color.set(0xf9941d);
				break;
			case 'black':
				color.set(0x000000);
				break;
			default:
			}
			return color;
		}

		function createAxisHelpers(scene) {
			var axisHelper = new THREE.AxisHelper(5);
			scene.add(axisHelper);
		}

	}-*/;

	public static void init() {
		drawPrototypeLandscape();
		mouseHandler();
	}
}
