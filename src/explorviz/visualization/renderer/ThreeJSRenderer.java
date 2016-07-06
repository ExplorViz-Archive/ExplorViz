package explorviz.visualization.renderer;

import explorviz.visualization.engine.primitives.Box;

/**
 * First prototype for switching the 3D visualization from plain WebGL towards
 * ThreeJS
 *
 * @author Christian Zirkelbach, Alexander Krause
 *
 */
public class ThreeJSRenderer {

	public static native void createRenderingObject() /*-{

		RenderingObject = function() {
			this.THREE = $wnd.THREE;
			this.Leap = $wnd.Leap;
		};

		$wnd.renderingObj = new RenderingObject();

	}-*/;

	public static native void initApplicationDrawer() /*-{

		RenderingObject.prototype.applicationDrawer = function() {

			var self = this;

			var THREE = self.THREE;
			var Leap = self.Leap;

			var viewportWidth = @explorviz.visualization.engine.main.WebGLStart::viewportWidth;
			var viewportHeight = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;

			// needs 0.1 near value for leap motion
			self.camera = new THREE.PerspectiveCamera(75, viewportWidth
					/ viewportHeight, 0.1, 1000);

			//			this.camera.position.z = 20;
			self.camera.position.z = 150; // integration test

			self.canvas = $doc.getElementById("threeJSCanvas");

			self.scene = new THREE.Scene();
			self.renderer = new THREE.WebGLRenderer({
				canvas : this.canvas,
				antialias : true,
				alpha : true
			});

			self.renderer.setSize(viewportWidth, viewportHeight);

			// set background color to white
			self.renderer.setClearColor(0xffffff, 1);

			// To allow render sprite-overlay on top
			self.renderer.autoClear = false;

			self.renderer.shadowMap.enabled = true;
			// soften the shadows
			self.renderer.shadowMapSoft = true;

			// Define the spotlight for the scene
			// TODO
			// needs a little "color" tuning to be like the original ExplorViz 3D visualization
			// basic hex colors are identical to ExplorViz
			var spotLight = new THREE.SpotLight(0xffffff, 1.3, 1000, 1.56, 0, 0);
			spotLight.position.set(100, 100, 100);
			//		spotLight.castShadow = false;
			//		spotLight.shadow.camera.near = 6;
			//		spotLight.shadow.camera.far = 13;
			self.scene.add(spotLight);

			// allows to debug the spotlight
			//		var spotLightHelper = new THREE.SpotLightHelper(spotLight);
			//		scene.add(spotLightHelper);

			// define height of packages and system
			var levelHeight = 0.5;

			// container for all landscape related objects
			self.landscape = new THREE.Group();
			self.scene.add(self.landscape);

			//			createLandscape(self.landscape);

			// create tooltips
			createTooltips();

			//		createAxisHelpers(self.scene);

			// rotates the model towards 45 degree and zooms out
			//			resetCamera();

			// inject into website
			//$wnd.jQuery("#webglcanvas").hide();
			$wnd.jQuery("#webglDiv").append(this.canvas);

			// possible option for future work:
			// reposition camera if translating objects is not working, e.g.
			// camera.position.set(0,-12,5);
			// camera.lookAt(new THREE.Vector3( 0, 5, 0 ));

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

				self.renderer.setSize(resizedWidth, resizedHeight);
				this.camera.aspect = resizedWidth / resizedHeight;
				this.camera.updateProjectionMatrix();
			});

			// initialize Leap Motion
			initLeap();

			// Functions
			function createLandscape(landscape) {

				var dataTestSystem = {
					name : 'Neo4J'
				};
				var testSystem = createSystem(self.landscape, dataTestSystem);

				// create some test objects
				var dataTestPackageA = {
					name : 'org',
					instances : []
				};
				var dataTestPackageB = {
					name : 'neo4j',
					instances : [ {
						name : 'doE',
						numOfCalls : 8
					} ]
				};
				var dataTestPackageC = {
					name : 'graphdb',
					instances : [ {
						name : 'doE',
						numOfCalls : 3
					}, {
						name : 'doF',
						numOfCalls : 7
					} ]
				};
				var dataTestPackageD = {
					name : 'helpers',
					instances : [ {
						name : 'doG',
						numOfCalls : 3
					}, {
						name : 'doH',
						numOfCalls : 7
					} ]
				};

				var testPackageA = createPackage(testSystem, dataTestPackageA);
				var testPackageB = createPackage(testPackageA, dataTestPackageB);
				var testPackageC = createPackage(testPackageB, dataTestPackageC);
				var testPackageD = createPackage(testPackageB, dataTestPackageD);

				var testInstancesB = createInstance(testPackageB,
						dataTestPackageB);
				//				var testInstancesC = createInstance(testPackageC, dataTestPackageC);
				//				var testInstancesD = createInstance(testPackageD, dataTestPackageD);
			}

			function createTooltips() {
				self.tooltipCamera = new THREE.OrthographicCamera(
						-viewportWidth / 2, viewportWidth / 2,
						viewportHeight / 2, -viewportHeight / 2, 1, 10);
				self.tooltipCamera.position.z = 10;

				self.tooltipScene = new THREE.Scene();

				self.tooltipCanvas = document.createElement('canvas');
				self.tooltipContext = self.tooltipCanvas.getContext('2d');

				self.tooltipTexture = new THREE.Texture(self.tooltipCanvas);
				self.tooltipTexture.needsUpdate = true;
				self.tooltipTexture.minFilter = THREE.LinearFilter;

				self.tooltipMaterial = new THREE.SpriteMaterial({
					map : self.tooltipTexture
				});

				self.tooltipSprite = new THREE.Sprite(self.tooltipMaterial);
				self.tooltipSprite.scale.set(200, 200, 1);

				self.tooltipScene.add(self.tooltipSprite);
			}

			// initializes the LEAP Motion library for gesture control
			function initLeap() {
				Leap.loop();

				Leap.loopController.use('transform', {
					vr : true,
					effectiveParent : self.camera
				});

				Leap.loopController.use('boneHand', {
					scene : self.scene,
					arm : true
				});

				self.vrControls = new THREE.VRControls(self.camera);
				self.vrEffect = new THREE.VREffect(self.renderer);

				// handler if necessary
				var onkey = function(event) {
					if (event.key === 'z' || event.keyCode === 122) {
						self.vrControls.zeroSensor();
					}
					if (event.key === 'f' || event.keyCode === 102) {
						console.log('f');
						return self.vrEffect.setFullScreen(true);
					}
				};
			}

			// Resets the camera/model towards an predefined position (45 degree)
			function resetCamera() {
				var rotationX = 0.57;
				var rotationY = -0.76;
				var cameraPositionZ = 20;

				self.landscape.rotation.x = rotationX;
				self.landscape.rotation.y = rotationY;
				this.camera.position.z = cameraPositionZ;
			}

			// TODO WIP
			// updates the layout of the parent after a new package has been added
			function updateLayout(parent, newPackage) {

				var debug = false;

				var numOfPackages = parent.userData.numOfPackages;

				if (numOfPackages > 1) {
					parent.traverse(function(child) {
						if (child instanceof THREE.Mesh) {
							if (child.userData.type == 'package') {
								// resize children (packages)
								{
								}
							}
						}
						if (debug) {
							console.log('type: ' + parent.userData.type);
							console.log('name: ' + parent.name);
							console.log('packages: '
									+ parent.userData.numOfPackages);
							console.log('instances: '
									+ parent.userData.numOfInstances);
						}
					});
				}
			}

			// TODO real centering of text label, sometimes there is a little offset
			function createLabel(parentObject) {
				var dynamicTexture = new $wnd.THREEx.DynamicTexture(512, 512);
				dynamicTexture.texture.needsUpdate = true;
				dynamicTexture.context.font = "bolder 90px Verdana";
				dynamicTexture.texture.anisotropy = self.renderer
						.getMaxAnisotropy()
				dynamicTexture.clear();

				// at size (3,3) the Neo4J label is clipped, why?
				var geometry = new THREE.PlaneGeometry(1, 1);
				var material = new THREE.MeshBasicMaterial({
					map : dynamicTexture.texture,
					transparent : true
				})

				var textMesh = new THREE.Mesh(geometry, material);

				// calculate boundingbox for (centered) positioning
				parentObject.geometry.computeBoundingBox();
				var bboxParent = parentObject.geometry.boundingBox;

				textMesh.position.x = parentObject.position.x;
				textMesh.position.y = parentObject.position.y;
				textMesh.position.z = bboxParent.max.z + 0.05;

				textMesh.rotation.x = -(Math.PI / 2);
				textMesh.translateY(0.45);
				textMesh.translateZ(0.30);

				// font color depending on parent object
				var textColor = 'black';

				if (parentObject.userData.type == 'system') {
					textColor = 'black';
				} else if (parentObject.userData.type == 'package') {
					textColor = 'white';
				}
				// instance rotated text - colored white
				else {
				}

				dynamicTexture.drawText(parentObject.name, undefined, 256,
						textColor);
				dynamicTexture.texture.needsUpdate = true;

				// internal user-definded type
				textMesh.userData = {
					type : 'label'
				};
				parentObject.add(textMesh);
				return textMesh;
			}

			function createSystem(parentObject, systemDefintion) {
				var systemName = systemDefintion.name ? systemDefintion.name
						: '<unnamed system>';
				var systemSize = systemDefintion.size ? systemDefintion.size
						: 15;
				var geometry = new THREE.Geometry();
				var size = new THREE.Vector3(systemSize, levelHeight,
						systemSize);
				var position = new THREE.Vector3(0, 0, 0);

				var mesh = createBox(size, position);
				geometry.merge(mesh.geometry, mesh.matrix);

				// color system
				var material = new THREE.MeshLambertMaterial();
				material.side = THREE.DoubleSide;
				material.color = createColor('system');

				var newSystem = new THREE.Mesh(geometry, material);
				newSystem.name = systemName;

				// internal user-definded type
				newSystem.userData = {
					type : 'system',
					numOfPackages : 0
				};

				createLabel(newSystem);
				parentObject.add(newSystem);
				return newSystem;
			}

			// adds a package to the parent object
			function createPackage(parentObject, packageDefintion) {
				var packageName = packageDefintion.name ? packageDefintion.name
						: '<unnamed package>';

				// calculate boundingbox for layout based on parentObject
				parentObject.geometry.computeBoundingBox();
				var bboxParent = parentObject.geometry.boundingBox;
				var parentHeight = (bboxParent.max.z - bboxParent.min.z);
				var parentWidth = (bboxParent.max.x - bboxParent.min.x);
				var parentDepth = (bboxParent.max.y - bboxParent.min.y);

				parentObject.userData.numOfPackages++;

				var geometry = new THREE.Geometry();
				var size = new THREE.Vector3(parentWidth - 1, levelHeight,
						parentHeight - 1.5);

				var material = new THREE.MeshLambertMaterial();
				material.side = THREE.DoubleSide;
				material.color = createColor('black');

				var position = new THREE.Vector3(0, 0, 0);
				var mesh = createBox(size, position);
				geometry.merge(mesh.geometry, mesh.matrix);

				var newPackage = new THREE.Mesh(geometry, material);
				newPackage.name = packageName;

				newPackage.userData = {
					type : 'package',
					numOfPackages : 0,
					numOfInstances : 0
				};

				createLabel(newPackage);

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
				newPackage.translateY(levelHeight);
				newPackage.translateZ(-levelHeight);

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
				var size = new THREE.Vector3((sizeFactor * levelHeight),
						((sizeFactor * levelHeight) * instanceNumOfCalls),
						(sizeFactor * levelHeight));

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
				newInstance.translateY(levelHeight);

				parentObject.add(newInstance);
				parentObject.userData.numOfInstances += 1;

				//				// TODO fix iteration for rearrangement
				//				// Rearrange instances, if necessary
				//				parentObject.traverse(function(child) {
				//					if ((child instanceof THREE.Mesh)
				//							&& (child.userData.type == 'instance')) {
				//						console.log(child);
				//					}
				//				});

				return newInstance;
			}

			// creates and positiones a parametric box
			RenderingObject.prototype.createBox = function(sizeVector,
					positionVector) {
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

			return {}
		};
		$wnd.renderingObj.applicationDrawer();
	}-*/;

	public static native void initInteractionHandler() /*-{

		RenderingObject.prototype.interactionHandler = function() {
			var self = this;

			var THREE = this.THREE;
			var canvas = this.canvas;
			var camera = this.camera;

			var scene = self.scene;
			var landscape = self.landscape;

			var mouse = new THREE.Vector2(0, 0);
			mouse.DownLeft = false;
			mouse.DownRight = false;
			mouse.wheelPressed = false;
			mouse.leftClicked = false;

			// low value => high speed
			var movementSpeed = 100;

			var cameraTranslateX = 0, cameraTranslateY = 0;

			// Raycasting
			var raycaster = new THREE.Raycaster();
			var INTERSECTED;
			var oldColor = new THREE.Color();

			// get offset from parent element (navbar) : {top, left}
			var canvasOffset = $wnd.jQuery(this.canvas).offset();

			function onMouseMove(evt) {
				if (!mouse.downLeft && !mouse.downRight) {
					return;
				}

				evt.preventDefault();

				// reset possible left click -> no raycasting when mouse moves
				mouse.leftClicked = false;

				// rotate around center of mesh group
				if (mouse.downRight) {
					var deltaX = evt.clientX - mouse.x, deltaY = evt.clientY
							- mouse.y;
					mouse.x = evt.clientX;
					mouse.y = evt.clientY;

					rotateScene(deltaX, deltaY);
				}
				// translate
				else if (mouse.downLeft) {
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
					mouse.downLeft = false;
					mouse.downRight = true;
					mouse.x = evt.clientX;
					mouse.y = evt.clientY;
					mouse.leftClicked = false;
				}

				// translation
				// !right && left
				else if (btnCode == 1) {
					mouse.downLeft = true;
					mouse.downRight = false;
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

				mouse.leftClicked = false;
				mouse.downLeft = false;
				mouse.downRight = false;
			}

			function onMouseWheelPressed(evt) {
				var delta = Math.max(-1, Math.min(1,
						(evt.wheelDelta || -evt.detail)));

				mouse.wheelPressed = true;
				zoomCamera(delta);
				mouse.wheelPressed = false;
			}

			function raycasting() {
				// TODO
				// ray has a little offset, needs to be fixed
				// Maybe still an offset problem with the canvas?

				// update the picking ray with the camera and mouse position
				raycaster.setFromCamera(mouse, self.camera);
				// calculate objects intersecting the picking ray (true => recursive)
				var intersections = raycaster.intersectObjects(scene.children,
						true);

				if (intersections.length > 0 && mouse.leftClicked == true) {
					var obj = intersections[0].object;

					if (INTERSECTED != obj) {
						if (INTERSECTED != undefined) {
							INTERSECTED.material.color.set(oldColor);
						}

						// select parent if label is selected
						if (obj.userData.type == 'label') {
							obj = obj.parent;
						}

						// select only if not system
						if (obj.userData.type != 'system') {
							// update tooltip, if object has name
							if (obj.name) {
								updateTooltip(obj.name, true);
							}

							INTERSECTED = obj;
							oldColor.copy(obj.material.color);
							obj.material.color.setRGB(1, 0, 0);
						}

					} else {
						updateTooltip("", false);
						obj.material.color.set(oldColor);
						INTERSECTED = null;
					}
				}
			}

			function updateTooltip(message, showing) {
				self.tooltipContext.clearRect(0, 0, self.tooltipCanvas.width,
						self.tooltipCanvas.height);

				if (showing) {
					//var message = intersects[0].object.name;
					//var metrics = self.tooltipContext.measureText(message);
					//var width = metrics.width;

					// draw background
					self.tooltipContext.beginPath();
					self.tooltipContext.fillStyle = "white";
					self.tooltipContext.fillRect(20, 20, 150, 50);
					self.tooltipContext.fill();

					// draw string
					self.tooltipContext.beginPath();
					self.tooltipContext.font = "Bold 20px Arial";
					self.tooltipContext.textAlign = "center";
					self.tooltipContext.textBaseline = "middle";
					self.tooltipContext.fillStyle = "black";
					self.tooltipContext.fillText(message, 80, 40);
					self.tooltipContext.fill();

					self.tooltipTexture.needsUpdate = true;

					var viewportWidth = @explorviz.visualization.engine.main.WebGLStart::viewportWidth;
					var viewportHeight = @explorviz.visualization.engine.main.WebGLStart::viewportHeight;
					var canvasOffset = $wnd.jQuery(self.canvas).offset();

					var x = cameraTranslateX - viewportWidth / 2;
					var y = -((cameraTranslateY - canvasOffset.top) - viewportHeight / 2);

					// set(0,0,1) = center due to ortho
					self.tooltipSprite.position.set(x, y, 1);
				} else {
					self.tooltipTexture.needsUpdate = true;
				}

			}

			function rotateScene(deltaX, deltaY) {
				landscape.rotation.y += deltaX / movementSpeed;
				landscape.rotation.x += deltaY / movementSpeed;
				//			textMesh.rotation.y += deltaX / movementSpeed;
				//			textMesh.rotation.x += deltaY / movementSpeed;
			}

			function translateCamera(deltaX, deltaY) {
				//camera.position.x -= deltaX / movementSpeed;
				//camera.position.y += deltaY / movementSpeed;
				camera.position.x -= deltaX / 3.0;
				camera.position.y += deltaY / 3.0;
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
				// Alex: could be weird for VR-Mode
			}

			canvas.addEventListener('mousemove', onMouseMove, false);
			canvas.addEventListener('mouseup', onMouseUp, false);
			canvas.addEventListener('mousedown', onMouseDown, false);
			canvas.addEventListener('mousewheel', onMouseWheelPressed, false)

			return {}

		};
		$wnd.renderingObj.interactionHandler();
	}-*/;

	public static void init() {
		createRenderingObject();
		initApplicationDrawer();
		initInteractionHandler();
	}

	/*
	 * (Helper-) Functions
	 */

	public static native void render() /*-{

		var context = $wnd.renderingObj;

		if ($doc.getElementById("webglcanvas") != null)
			$doc.getElementById("webglcanvas").remove();

		context.vrControls.update();
		context.renderer.clear();
		context.vrEffect.render(context.scene, context.camera);
		context.renderer.clearDepth();
		context.vrEffect.render(context.tooltipScene, context.tooltipCamera);

	}-*/;

	public static native void resetCamera() /*-{

		var context = $wnd.renderingObj;
		var THREE = context.THREE;

		var rotationX = 45 * Math.PI / 180;
		var rotationY = 45 * Math.PI / 180;

		context.landscape.rotation.x = rotationX;
		context.landscape.rotation.y = rotationY;

	}-*/;

	public static native void deleteMeshes() /*-{

		var context = $wnd.renderingObj;
		var length = context.landscape.children.length;

		for (var i = length - 1; i >= 0; i--) {
			var child = context.landscape.children[i];
			context.landscape.remove(child);
		}
	}-*/;

	/*
	 * Create methods (called from ThreeJSWrapper.xtend)
	 */

	public static native void createBoxes(Box box, String name) /*-{

		var context = $wnd.renderingObj;
		var THREE = context.THREE;

		var center = box.@explorviz.visualization.engine.primitives.Box::getCenter()();
		var extension = box.@explorviz.visualization.engine.primitives.Box::getExtensions()();
		var color = box.@explorviz.visualization.engine.primitives.Box::getColor()();

		var centerPoint = new THREE.Vector3(center.x, center.y, center.z);

		var geometry = new THREE.Geometry();
		var size = new THREE.Vector3(extension.x, extension.y, extension.z);

		var material = new THREE.MeshLambertMaterial();
		material.side = THREE.DoubleSide;
		material.color = new THREE.Color(color.x, color.y, color.z);

		var mesh = context.createBox(size, centerPoint);
		geometry.merge(mesh.geometry, mesh.matrix);

		var newPackage = new THREE.Mesh(geometry, material);
		newPackage.name = name;

		newPackage.userData = {
			type : 'package',
			numOfPackages : 0,
			numOfInstances : 0
		};
		context.landscape.add(newPackage);
	}-*/;
}
