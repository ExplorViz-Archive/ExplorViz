package explorviz.visualization.renderer

import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.engine.primitives.PrimitiveObject
import java.util.List
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Pipe
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.ClazzClientSide
import java.util.ArrayList
import explorviz.visualization.model.helper.Draw3DNodeEntity
import explorviz.visualization.model.CommunicationClazzClientSide
import explorviz.visualization.engine.navigation.Camera
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.layout.application.ApplicationLayoutInterface

class ApplicationRenderer {
	static var Vector3f centerPoint
	static val List<PrimitiveObject> labels = new ArrayList<PrimitiveObject>(64)
//	static val String CURRENT_HIGHLIGHT = "EPrints.Plugin.Screen.Import"
//	static val String CURRENT_HIGHLIGHT = "EPrints.Plugin.Screen.Items"
//	static val String CURRENT_HIGHLIGHT = "EPrints.DataObj.User"
	
	static val List<ComponentClientSide> laterDrawComponent = new ArrayList<ComponentClientSide>(64)
	static val List<ClazzClientSide> laterDrawClazz = new ArrayList<ClazzClientSide>(64)

	static val Vector4f WHITE = new Vector4f(1f, 1f, 1f, 1f)
	static val Vector4f BLACK = new Vector4f(0f, 0f, 0f, 1f)
//	static val Vector4f BLUE = new Vector4f(193 / 255f, 0 / 255f, 79 / 255f, 1f)
//	static val Vector4f RED = new Vector4f(240 / 255f, 240 / 255f, 10 / 255f, 1f)

	def static drawApplication(ApplicationClientSide application, List<PrimitiveObject> polygons) {
		labels.clear()
		application.clearAllPrimitiveObjects()

		if (centerPoint == null) {
			centerPoint = getCenterPoint(application)
			Camera::vector.z = -100f
		}

		application.components.forEach [
			drawOpenedComponent(it, polygons, 0)
		]

		drawCommunications(application.communications, polygons)
		laterDrawComponent.forEach [
			drawClosedComponents(it, polygons)
		]
		laterDrawComponent.clear()
		
		laterDrawClazz.forEach [
			drawClazz(it, polygons)
		]
		laterDrawClazz.clear()
		
		polygons.addAll(labels)
	}

	def private static drawCommunications(List<CommunicationClazzClientSide> communications,
		List<PrimitiveObject> polygons) {
//		val sortedList = communications.sortBy[it.averageResponseTime * it.requestsPerSecond]
//
//		val badPerformanceStartIndex = Math.round((sortedList.size() / 100f) * 99)
//		val badPerformanceList = sortedList.subList(badPerformanceStartIndex, sortedList.size())

		val commuList = new ArrayList<CommunicationAccumulator>
		communications.forEach [
			val source = if (it.source.parent.opened) it.source else findFirstOpenComponent(it.source.parent)
			val target = if (it.target.parent.opened) it.target else findFirstOpenComponent(it.target.parent)
			if (source != null && target != null && source != target) {
				var found = false
				for (commu : commuList) {
					if (found == false) {
						found = ((commu.source == source) && (commu.target == target))

						if (found) {
							commu.requestCount = commu.requestCount + it.requestsPerSecond
							commu.averageResponseTime = Math.max(commu.averageResponseTime, it.averageResponseTime)
							commu.count = commu.count + 1
						}
					}
				}

				if (found == false) {
					val newCommu = new CommunicationAccumulator()
					newCommu.source = source
					newCommu.target = target
					newCommu.requestCount = it.requestsPerSecond
					newCommu.averageResponseTime = it.averageResponseTime
					newCommu.count = 1
					commuList.add(newCommu)
				}
			}
		]

		commuList.forEach [
			drawCommunication(it.source, it.target, it.requestCount, it.averageResponseTime, polygons)
		]
	}

	def private static ComponentClientSide findFirstOpenComponent(ComponentClientSide entity) {
		if (entity.parentComponent == null) {
			return null;
		}

		if (entity.parentComponent.opened) {
			return entity
		}

		return findFirstOpenComponent(entity.parentComponent)
	}

	def private static drawCommunication(Draw3DNodeEntity source, Draw3DNodeEntity target, int requestsPerSecond,
		float maxResponseTime, List<PrimitiveObject> polygons) {
		val start = new Vector3f(source.positionX - centerPoint.x + source.width / 2f,
			source.positionY - centerPoint.y + 0.8f, source.positionZ - centerPoint.z + source.depth / 2f)
		val end = new Vector3f(target.positionX - centerPoint.x + target.width / 2f,
			target.positionY - centerPoint.y + 0.8f, target.positionZ - centerPoint.z + target.depth / 2f)

//		val highlight = (source.fullQualifiedName == CURRENT_HIGHLIGHT ||
//			target.fullQualifiedName == CURRENT_HIGHLIGHT)
		var pipeSize = getCategoryForCommuincation(requestsPerSecond) * 0.14f + 0.04f

		val pipe = createPipe(start, end, pipeSize, false)

		//commu.primitiveObjects.add(pipe) TODO
		polygons.add(pipe)

//		if (highlight) {
//			var String millisecond = (Math.round((maxResponseTime / (1000 * 1000)) * 100.0) / 100.0).toString()
//			if (millisecond == "0") {
//				millisecond = "0.1";
//			}
//			val labelCenter = new Vector3f(start.x + ((end.x - start.x) / 2f), start.y + ((end.y - start.y) / 2f),
//				start.z + ((end.z - start.z) / 2f))
//			val label = createLabel(labelCenter, new Vector3f(7f, 0.2f, 7f), requestsPerSecond + " x " + 
//				millisecond + " ms", RED)
//
//			labels.add(label)
//		}
	}
	


	def private static int getCategoryForCommuincation(int requestsPerSecond) {
		if (requestsPerSecond == 0) {
			return 0
		} else if ((0 < requestsPerSecond) && (requestsPerSecond <= 3)) { // TODO quantile
			return 1
		} else if ((2 < requestsPerSecond) && (requestsPerSecond <= 7)) {
			return 2
		} else if ((8 < requestsPerSecond) && (requestsPerSecond <= 10)) {
			return 3
		} else if ((10 < requestsPerSecond) && (requestsPerSecond <= 80)) {
			return 4
		} else if ((80 < requestsPerSecond) && (requestsPerSecond <= 150)) {
			return 5
		} else {
			return 6
		}
	}

	def private static createPipe(Vector3f start, Vector3f end, float lineThickness, boolean transparent) {
		val communicationPipe = new Pipe()
		if (transparent) {
			communicationPipe.setTransparent(true)
			communicationPipe.setColor(ColorDefinitions::pipeColorTrans)
		} else {
			communicationPipe.setColor(ColorDefinitions::pipeColor)
		}
		communicationPipe.setLineThickness(lineThickness)
		communicationPipe.begin
		communicationPipe.addPoint(start)
		communicationPipe.addPoint(end)
		communicationPipe.end
		communicationPipe
	}

	def private static void drawOpenedComponent(ComponentClientSide component, List<PrimitiveObject> polygons, int index) {
		val box = component.createBox(centerPoint, component.color)

		val labelCenterPoint = new Vector3f(component.centerPoint.x - centerPoint.x - component.width / 2.0f + ApplicationLayoutInterface::labelInsetSpace / 2.0f + ApplicationLayoutInterface::insetSpace / 2f,
			component.centerPoint.y - centerPoint.y,
			component.centerPoint.z - centerPoint.z)
		val labelExtension = new Vector3f(component.extension.x, component.extension.y / 2f,
			component.extension.z / 2f)
		val label = createLabelOpenPackages(labelCenterPoint, labelExtension, component.name, if (index == 0) BLACK else WHITE)

		component.primitiveObjects.add(box)

		polygons.add(box)
		labels.add(label)

		component.clazzes.forEach [
			if (component.opened) {
				//drawClazzes(it, polygons)
				laterDrawClazz.add(it)
			}
		]

		component.children.forEach [
			if (it.opened) {
				drawOpenedComponent(it, polygons, index + 1)
			} else {
				if (component.opened) {
//					drawClosedComponents(it, polygons)
					laterDrawComponent.add(it)
				}
			}
		]
	}

	def private static void drawClosedComponents(ComponentClientSide component, List<PrimitiveObject> polygons) {
		val box = component.createBox(centerPoint, component.color)
		val label = createLabel(component.centerPoint.sub(centerPoint), component.extension, component.name, WHITE)

		component.primitiveObjects.add(box)

		polygons.add(box)
		labels.add(label)
	}

	def private static void drawClazz(ClazzClientSide clazz, List<PrimitiveObject> polygons) {
		val box = clazz.createBox(centerPoint, clazz.color)
		val label = createLabel(
			new Vector3f(clazz.positionX - centerPoint.x + clazz.width / 2f,
				clazz.positionY - centerPoint.y + clazz.height / 2f,
				clazz.positionZ - centerPoint.z + clazz.depth / 2f),
			new Vector3f(clazz.width / 2f, clazz.height / 2f, clazz.depth / 2f),
			clazz.name,
			WHITE
		)

		clazz.primitiveObjects.add(box)

		polygons.add(box)
		labels.add(label)
	}

	def private static createLabel(Vector3f center, Vector3f itsExtension, String label, Vector4f color) {
		val texture = TextureManager::createTextureFromTextWithColor(label, 1024, 1024, color)

		val normalY = center.y + itsExtension.y + 0.02f
		val heigheredY = center.y + itsExtension.y + 0.02f

		val xExtension = Math::max(itsExtension.x * 5f, 13f)
		val zExtension = xExtension

		new Quad(
			new Vector3f(center.x - xExtension, normalY, center.z),
			new Vector3f(center.x, normalY, center.z + zExtension),
			new Vector3f(center.x + xExtension, heigheredY, center.z),
			new Vector3f(center.x, heigheredY, center.z - zExtension),
			texture,
			true
		)
	}
	
	def private static createLabelOpenPackages(Vector3f center, Vector3f itsExtension, String label, Vector4f color) {
		val texture = TextureManager::createTextureFromTextWithColor(label, 1024, 1024, color)

		val yValue = center.y + 0.02f

		val xExtension = itsExtension.x
		val zExtension = Math::max(itsExtension.z * 5f, 13f)

		new Quad(
			new Vector3f(center.x - xExtension, yValue, center.z - zExtension),
			new Vector3f(center.x - xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z + zExtension),
			new Vector3f(center.x + xExtension, yValue, center.z - zExtension),
			texture,
			true
		)
	}

	def private static getCenterPoint(ApplicationClientSide application) {
		val rect = new ArrayList<Float>
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)
		rect.add(Float::MAX_VALUE)
		rect.add(-Float::MAX_VALUE)

		val MIN_X = 0
		val MAX_X = 1
		val MIN_Y = 2
		val MAX_Y = 3
		val MIN_Z = 4
		val MAX_Z = 5

		application.components.forEach [
			getMinMaxFromQuad(it, rect, MIN_X, MAX_X, MAX_Y, MIN_Y, MAX_Z, MIN_Z)
		]

		new Vector3f(rect.get(MIN_X) + ((rect.get(MAX_X) - rect.get(MIN_X)) / 2f),
			rect.get(MIN_Y) + ((rect.get(MAX_Y) - rect.get(MIN_Y)) / 2f),
			rect.get(MIN_Z) + ((rect.get(MAX_Z) - rect.get(MIN_Z)) / 2f))
	}

	def private static getMinMaxFromQuad(Draw3DNodeEntity entity, ArrayList<Float> rect, int MIN_X, int MAX_X, int MAX_Y,
		int MIN_Y, int MAX_Z, int MIN_Z) {
		val curX = entity.positionX
		val curY = entity.positionY
		val curZ = entity.positionZ

		if (curX < rect.get(MIN_X)) {
			rect.set(MIN_X, curX)
		}
		if (rect.get(MAX_X) < curX + (entity.width)) {
			rect.set(MAX_X, curX + (entity.width))
		}

		if (curY > rect.get(MAX_Y)) {
			rect.set(MAX_Y, curY)
		}
		if (rect.get(MIN_Y) > curY - (entity.height)) {
			rect.set(MIN_Y, curY - (entity.height))
		}

		if (curZ < rect.get(MIN_Z)) {
			rect.set(MIN_Z, curZ)
		}
		if (rect.get(MAX_Z) < curZ + (entity.width)) {
			rect.set(MAX_Z, curZ + (entity.width))
		}
	}
}
