package explorviz.visualization.renderer;

import explorviz.shared.model.helper.Draw3DNodeEntity;
import explorviz.visualization.engine.math.Vector3f;
import explorviz.visualization.engine.primitives.Box;
import explorviz.visualization.engine.primitives.Pipe;

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
			this.Hammer = $wnd.Hammer;
		};

		$wnd.renderingObj = new RenderingObject();

	}-*/;

	public static native void initApplicationDrawer() /*-{

		RenderingObject.prototype.applicationDrawer = function() {

			var self = this;

			var THREE = self.THREE;

			var loader = new THREE.FontLoader();

			loader.load('js/threeJS/fonts/helvetiker_regular.typeface.json',
					function(font) {
						self.font = font;
					});

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
				canvas : self.canvas,
				antialias : true,
				alpha : true
			});

			self.renderer.setSize(viewportWidth, viewportHeight);

			// set background color to white
			self.renderer.setClearColor(0xffffff);

			// To allow render tooltip-overlay on top
			// DOESN'T WORK WITH VR ???
			self.renderer.autoClear = true;

			//self.renderer.shadowMap.enabled = true;
			// soften the shadows
			//self.renderer.shadowMapSoft = true;

			// Define the spotlight for the scene
			// TODO
			// needs a little "color" tuning to be like the original ExplorViz 3D visualization
			// basic hex colors are identical to ExplorViz
			var spotLight = new THREE.SpotLight(0xffffff, 0.5, 1000, 1.56, 0, 0);
			spotLight.position.set(100, 100, 100);
			spotLight.castShadow = false;
			//		spotLight.shadow.camera.near = 6;
			//		spotLight.shadow.camera.far = 13;
			self.scene.add(spotLight);
			var light = new THREE.AmbientLight(
					new THREE.Color(0.65, 0.65, 0.65));
			self.scene.add(light);

			// allows to debug the spotlight
			//		var spotLightHelper = new THREE.SpotLightHelper(spotLight);
			//		scene.add(spotLightHelper);

			// define height of packages and system
			var levelHeight = 0.5;

			// container for all landscape related objects
			//self.landscape = new THREE.Group();
			// ONLY FOR VR ATM !!
			self.landscape = new THREE.Object3D();

			// ONLY FOR VR ATM !!
			self.landscape.position.z = -100;

			//

			self.scene.add(self.landscape);

			//			createLandscape(self.landscape);

			// create tooltip
			createTooltip();

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
				self.camera.aspect = resizedWidth / resizedHeight;
				self.camera.updateProjectionMatrix();

				self.tooltipCamera.left = -resizedWidth / 2;
				self.tooltipCamera.right = resizedWidth / 2;
				self.tooltipCamera.top = resizedHeight / 2;
				self.tooltipCamera.bottom = -resizedHeight / 2;
				self.tooltipCamera.aspect = resizedWidth / resizedHeight;
				self.tooltipCamera.updateProjectionMatrix();

			});

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

			function createTooltip() {
				self.tooltipCanvas = document.createElement('canvas');

				self.tooltipCanvas.id = "tooltipCanvas";

				self.tooltipContext = self.tooltipCanvas.getContext('2d');
				self.tooltipContext.font = "Bold 20px Arial";

				var width = self.renderer.domElement.clientWidth;
				var height = self.renderer.domElement.clientHeight;

				self.tooltipCamera = new THREE.OrthographicCamera(-width / 2,
						width / 2, height / 2, -height / 2, 1, 10);
				self.tooltipCamera.position.z = 10;
				self.tooltipScene = new THREE.Scene();

				self.tooltipTexture = new THREE.Texture(self.tooltipCanvas);
				self.tooltipTexture.needsUpdate = true;

				self.tooltipMaterial = new THREE.SpriteMaterial({
					map : self.tooltipTexture
				});

				self.tooltipMaterial.map.minFilter = THREE.LinearFilter;

				self.tooltipSprite = new THREE.Sprite(self.tooltipMaterial);
				self.tooltipSprite.scale.set(200, 200, 1);

				self.tooltipScene.add(self.tooltipSprite);
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

			// TODO Label Size based on object size
			RenderingObject.prototype.createLabel = function(parentObject) {

				var minFontSize = 0.8;
				var maxFontSize = 2;

				var labelString = parentObject.name;

				var maxLengthOfLine = 10;
				var numberOfLines = labelString.length / maxLengthOfLine;

				// create every line
				for (var i = 0; i < numberOfLines; i++) {

					var partialString = labelString.split(i * maxLengthOfLine,
							(i + 1) * maxLengthOfLine);

					console.log(labelString + ", " + partialString[i]);

					var textGeo = new THREE.TextGeometry(partialString[i], {

						font : self.font,

						size : maxFontSize,
						height : 0.1,
						curveSegments : 1

					});

					var textMaterial = new THREE.MeshBasicMaterial({
						color : 0xff0000
					});

					var mesh = new THREE.Mesh(textGeo, textMaterial);

					textGeo.computeBoundingSphere();

					var centerX = textGeo.boundingSphere.center.x;

					// calculate boundingbox for (centered) positioning
					parentObject.geometry.computeBoundingBox();
					var bboxParent = parentObject.geometry.boundingBox;

					// rotate label depending on open status
					if (parentObject.userData.opened) {
						mesh.position.x = bboxParent.min.x + 2;
						mesh.position.y = bboxParent.max.y;
						mesh.position.z = 0 - Math.abs(centerX) / 2;

						//					mesh.position.x = 0;
						//					mesh.position.y = 10;
						//					mesh.position.z = 0;

						mesh.rotation.x = -(Math.PI / 2);
						mesh.rotation.z = -(Math.PI / 2);
					} else {
						mesh.position.x = 0 - Math.abs(centerX) / 2;
						mesh.position.y = bboxParent.max.y;
						mesh.position.z = 0 - Math.abs(centerX) / 2;

						//					mesh.position.x = 0;
						//					mesh.position.y = 10;
						//					mesh.position.z = 0;

						mesh.rotation.x = -(Math.PI / 2);
						mesh.rotation.z = -(Math.PI / 4);
					}

					//				if (parentObject.name == "CategorySqlMapDao")
					//					if (parentObject.name == "graphdb")
					//					console.log(parentObject.extensions.z - Math.abs(centerX)
					//							/ 2);

					// font color depending on parent object
					var textColor = new THREE.Color(0, 0, 0);

					if (parentObject.userData.type == 'system') {
						textColor = new THREE.Color(0, 0, 0);
					} else if (parentObject.userData.type == 'package') {
						textColor = new THREE.Color(1, 1, 1);

						if (parentObject.userData.foundation) {
							textColor = new THREE.Color(0, 0, 0);
						}
					}
					// instance rotated text - colored white
					else {
						textColor = new THREE.Color(1, 1, 1);
					}

					textMaterial.color = textColor;

					// internal user-definded type
					mesh.userData = {
						type : 'label'
					};

					parentObject.add(mesh);

					//return textMesh;	
				}
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
					positionVector, materialObj) {
				var material;

				if (materialObj == null) {
					material = new THREE.MeshBasicMaterial();
					material.color = createColor('black');
				} else {
					material = materialObj;
				}

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

			// init vr

			self.vrControls = new THREE.VRControls(self.camera);
			self.vrControls.standing = true;
			self.vrEffect = new THREE.VREffect(self.renderer);
			self.vrEffect.setSize(self.renderer.domElement.clientWidth,
					self.renderer.domElement.clientHeight);

			return {}

		};
		$wnd.renderingObj.applicationDrawer();
	}-*/;

	public static native void initInteractionHandler() /*-{

		RenderingObject.prototype.interactionHandler = function() {
			var self = this;

			var THREE = self.THREE;
			var Hammer = self.Hammer;
			var canvas = self.canvas;
			var camera = self.camera;

			var scene = self.scene;
			var landscape = self.landscape;

			// low value => high speed
			var movementSpeed = 3;

			var cameraTranslateX = 0, cameraTranslateY = 0;

			var hammer = new Hammer.Manager(canvas, {});

			var singleTap = new Hammer.Tap({
				event : 'singletap',
				interval : 250
			});

			var doubleTap = new Hammer.Tap({
				event : 'doubletap',
				taps : 2,
				interval : 250
			});

			var pan = new Hammer.Pan({
				event : 'pan'
			});

			// TODO: Pinch & Rotation

			hammer.add([ doubleTap, singleTap, pan ]);

			doubleTap.recognizeWith(singleTap);
			singleTap.requireFailure(doubleTap);

			hammer.on('panstart', function(evt) {
				cameraTranslateX = evt.pointers[0].clientX
				cameraTranslateY = evt.pointers[0].clientY;
			});

			hammer
					.on(
							'panmove',
							function(evt) {

								var deltaX = evt.pointers[0].clientX
										- cameraTranslateX;
								var deltaY = evt.pointers[0].clientY
										- cameraTranslateY;

								var distanceXInPercent = (deltaX / parseFloat(self.renderer.domElement.clientWidth)) * 100.0
								var distanceYInPercent = (deltaY / parseFloat(self.renderer.domElement.clientHeight)) * 100.0

								var xVal = camera.position.x
										+ distanceXInPercent * 6.0 * 0.015
										* -(Math.abs(camera.position.z) / 4.0);
								var yVal = camera.position.y
										+ distanceYInPercent * 4.0 * 0.01
										* (Math.abs(camera.position.z) / 4.0);

								translateCamera(xVal, yVal);

								cameraTranslateX = evt.pointers[0].clientX;
								cameraTranslateY = evt.pointers[0].clientY;
							});

			hammer.on('panend', function(evt) {
				cameraTranslateX = 0;
				cameraTranslateY = 0;
			});

			hammer
					.on(
							'singletap',
							function(evt) {

								var clicked = {};
								clicked.x = evt.pointers[0].clientX;
								clicked.y = evt.pointers[0].clientY - 60;

								var mouse = {};

								mouse.x = (clicked.x / self.renderer.domElement.clientWidth) * 2 - 1;
								mouse.y = -(clicked.y / self.renderer.domElement.clientHeight) * 2 + 1;

								var intersectedObj = $wnd.renderingObj
										.raycasting(null, mouse, true);

								var showTooltip = false;

								if (intersectedObj == null) {
									@explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(null,null);
									updateTooltip(intersectedObj, clicked,
											showTooltip);
									return;
								}

								if (intersectedObj.userData.type == 'package') {
									showTooltip = @explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,intersectedObj.userData.explorVizObj);
								} else if (intersectedObj.userData.type == 'class') {
									showTooltip = @explorviz.visualization.engine.threejs.ThreeJSWrapper::highlight(Lexplorviz/shared/model/helper/Draw3DNodeEntity;Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizDrawEntity,null);
								}

								updateTooltip(intersectedObj, clicked,
										showTooltip);

							});

			hammer
					.on(
							'doubletap',
							function(evt) {

								var mouse = {};

								mouse.x = ((evt.pointers[0].clientX) / self.renderer.domElement.clientWidth) * 2 - 1;
								mouse.y = -((evt.pointers[0].clientY - 60) / self.renderer.domElement.clientHeight) * 2 + 1;

								var intersectedObj = $wnd.renderingObj
										.raycasting(null, mouse, true);

								if (intersectedObj.userData.type == 'package')
									@explorviz.visualization.engine.threejs.ThreeJSWrapper::toggleOpenStatus(Lexplorviz/visualization/engine/primitives/Box;)(intersectedObj.userData.explorVizObj)

							});

			// get offset from parent element (navbar) : {top, left}
			var canvasOffset = $wnd.jQuery(this.canvas).offset();

			var mouse = new THREE.Vector2(0, 0);
			mouse.DownRight = false;

			function onMouseMove(evt) {
				if (!mouse.downRight) {
					return;
				}

				evt.preventDefault();

				// rotate around center of mesh group
				if (mouse.downRight) {
					var deltaX = evt.clientX - mouse.x, deltaY = evt.clientY
							- mouse.y;
					mouse.x = evt.clientX;
					mouse.y = evt.clientY;

					rotateScene(deltaX, deltaY);
				}
			}

			function onMouseDown(evt) {
				var btnCode = evt.which;
				evt.preventDefault();

				// rotation
				// right && !left
				if (btnCode == 3) {
					mouse.downRight = true;
					mouse.x = evt.clientX;
					mouse.y = evt.clientY;
					mouse.leftClicked = false;
				}
			}

			function onMouseUp(evt) {
				evt.preventDefault();
				mouse.downRight = false;
			}

			function onMouseWheelPressed(evt) {
				var delta = Math.max(-1, Math.min(1,
						(evt.wheelDelta || -evt.detail)));

				zoomCamera(delta);

			}

			// Raycasting
			var raycaster = new THREE.Raycaster();

			RenderingObject.prototype.raycasting = function raycasting(origin,
					direction, fromCamera) {

				var counter = 0;

				if (fromCamera) {
					// direction = mouse
					raycaster.setFromCamera(direction, self.camera);
				} else if (origin) {
					raycaster.set(origin, direction);
				}

				// calculate objects intersecting the picking ray (true => recursive)
				var intersections = raycaster.intersectObjects(scene.children,
						true);

				if (intersections.length > 0) {

					var result = intersections
							.filter(function(obj) {
								return (obj.object.userData.type == 'package' || obj.object.userData.type == 'class');
							});

					if (result.length <= 0)
						return;

					return result[0].object;

				}
			}

			var INTERSECTED = null;
			var oldColor = new THREE.Color();

			function updateTooltip(obj, mouse, showTooltip) {

				if (obj == null || !showTooltip) {
					drawTooltip("", mouse, false);

					if (INTERSECTED != null) {
						INTERSECTED.material.color.set(oldColor);
					}

					INTERSECTED = null;
				}

				else if (INTERSECTED == null) {

					if (obj.name) {
						drawTooltip(obj.name, mouse, true);
					}

					INTERSECTED = obj;
					oldColor.copy(obj.material.color);
				}

				else if (INTERSECTED.name == obj.name) {

					drawTooltip("", mouse, false);
					INTERSECTED = null;
				}

				else {
					INTERSECTED.material.color.set(oldColor);
					drawTooltip(obj.name, mouse, true);
					INTERSECTED = obj;
					oldColor.copy(obj.material.color);
				}
			}

			function drawTooltip(message, mouse, showing) {

				self.tooltipContext.clearRect(0, 0, self.tooltipCanvas.width,
						self.tooltipCanvas.height);

				if (showing) {

					var viewportWidth = self.renderer.domElement.clientWidth;
					var viewportHeight = self.renderer.domElement.clientHeight;

					var x = mouse.x - viewportWidth / 2;
					var y = -(mouse.y + 60 - viewportHeight / 2);

					var metrics = self.tooltipContext.measureText(message);
					var width = metrics.width;

					self.tooltipSprite.position.set(x, y, 1);

					// draw black border
					self.tooltipContext.fillStyle = "rgba(0,0,0,0.95)";
					self.tooltipContext.fillRect(0, 0, width + 8, 20 + 8);

					// draw white background
					self.tooltipContext.fillStyle = "rgba(255,255,255,0.95)";
					self.tooltipContext.fillRect(2, 2, width + 4, 20 + 4);

					// draw string
					self.tooltipContext.fillStyle = "rgba(0,0,0,1)";
					self.tooltipContext.fillText(message, 4, 20);

				} else {

				}
				self.tooltipTexture.needsUpdate = true;
			}

			function rotateScene(deltaX, deltaY) {
				landscape.rotation.y += deltaX / 100;
				landscape.rotation.x += deltaY / 100;
			}

			function translateCamera(x, y) {
				camera.position.x = x;
				camera.position.y = y;
			}

			function zoomCamera(delta) {
				// zoom in
				if (delta > 0) {
					camera.position.z -= delta * 5.0;
				}
				// zoom out
				else {
					camera.position.z -= delta * 5.0;
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

		context.renderer.clear();
		context.renderer.render(context.scene, context.camera);
		// context.renderer.clearDepth();
		//context.renderer.render(context.tooltipScene, context.tooltipCamera);

	}-*/;

	public static native void resetCamera() /*-{

		var context = $wnd.renderingObj;
		var THREE = context.THREE;

		var rotationX = 45 * Math.PI / 180;
		var rotationY = 45 * Math.PI / 180;

		//context.landscape.rotation.x = rotationX;
		//context.landscape.rotation.y = rotationY;

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

	public static native void createBox(Box box, Draw3DNodeEntity explorVizEntity, String name,
			boolean isClass, boolean isOpened, boolean isFoundation) /*-{

		var context = $wnd.renderingObj;
		var THREE = context.THREE;

		var center = box.@explorviz.visualization.engine.primitives.Box::getCenter()();
		var extension = box.@explorviz.visualization.engine.primitives.Box::getExtensions()();
		var color = box.@explorviz.visualization.engine.primitives.Box::getColor()();

		var centerPoint = new THREE.Vector3(center.x, center.y, center.z);

		var size = new THREE.Vector3(extension.x, extension.y, extension.z);

		//centerPoint.multiplyScalar(0.3);
		//size.multiplyScalar(0.3);

		var material = new THREE.MeshLambertMaterial();
		material.color = new THREE.Color(color.x, color.y, color.z);

		var mesh = context.createBox(size, centerPoint, material);
		mesh.extensions = size;
		mesh.name = name;

		if (isClass) {
			mesh.userData = {
				type : 'class',
				numOfInstances : 0,
				explorVizObj : box,
				explorVizDrawEntity : explorVizEntity
			};
		} else {
			mesh.userData = {
				type : 'package',
				numOfPackages : 0,
				numOfInstances : 0,
				opened : isOpened,
				foundation : isFoundation,
				explorVizObj : box,
				explorVizDrawEntity : explorVizEntity
			};
		}

		context.createLabel(mesh);
		context.landscape.add(mesh);

	}-*/;

	public static native void createPipe(Pipe commu, Vector3f start,
			Vector3f end) /*-{

		var context = $wnd.renderingObj;
		var THREE = context.THREE;

		var startObj = start.@explorviz.visualization.engine.math.Vector3f::getVector()();
		var endObj = end.@explorviz.visualization.engine.math.Vector3f::getVector()();

		var thickness = commu.@explorviz.visualization.engine.primitives.Pipe::getLineThickness()();
		var color = commu.@explorviz.visualization.engine.primitives.Pipe::getColor()();

		//thickness *= 4;

		// Three docs: Due to limitations in the ANGLE layer, with the WebGL renderer on Windows platforms 
		// linewidth will always be 1 regardless of the set value.
		// => Pipes instead of lines?

		var opacityValue = color.w * 7.0;
		var transparentValue = false;

		if (opacityValue < 1.0)
			transparentValue = true;

		var material = new THREE.MeshBasicMaterial({
			color : new THREE.Color(color.x, color.y, color.z),
			opacity : opacityValue,
			transparent : transparentValue
		});

		var start = new THREE.Vector3(startObj.x, startObj.y, startObj.z);
		var end = new THREE.Vector3(endObj.x, endObj.y, endObj.z);

		//start.multiplyScalar(0.3);
		//end.multiplyScalar(0.3);
		thickness *= 0.2;

		var cylinder = cylinderMesh(start, end, material)

		function cylinderMesh(pointX, pointY, material) {
			var direction = new THREE.Vector3().subVectors(pointY, pointX);
			var orientation = new THREE.Matrix4();
			orientation.lookAt(pointX, pointY, new THREE.Object3D().up);
			orientation.multiply(new THREE.Matrix4().set(1, 0, 0, 0, 0, 0, 1,
					0, 0, -1, 0, 0, 0, 0, 0, 1));
			var edgeGeometry = new THREE.CylinderGeometry(thickness, thickness,
					direction.length(), 20, 1);
			var edge = new THREE.Mesh(edgeGeometry, material);
			edge.applyMatrix(orientation);

			edge.position.x = (pointY.x + pointX.x) / 2;
			//edge.position.y = (pointY.y + pointX.y) / 2 + 5.0;
			edge.position.y = (pointY.y + pointX.y) / 2;
			edge.position.z = (pointY.z + pointX.z) / 2;
			return edge;
		}

		context.landscape.add(cylinder);

	}-*/;

	/*
	 * Interaction
	 */

}
