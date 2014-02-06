package explorviz.visualization.renderer

import explorviz.visualization.model.ApplicationClientSide
import explorviz.visualization.engine.primitives.PrimitiveObject
import java.util.List
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.primitives.Pipe
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.model.ComponentClientSide
import explorviz.visualization.model.ClazzClientSide
import java.util.ArrayList
import explorviz.visualization.model.helper.Draw3DNodeEntity
import explorviz.visualization.model.CommunicationClazzClientSide

class ApplicationRenderer {
	static var Vector3f centerPoint
	static val communicationColor = new Vector4f(0.708f, 0.681f, 0.332f, 1f)

	def static drawApplication(ApplicationClientSide application, List<PrimitiveObject> polygons) {
		application.clearAllPrimitiveObjects()

		if (centerPoint == null) {
			centerPoint = getCenterPoint(application)
		}

		application.components.forEach [
			drawOpenedComponent(it, polygons, 0)
		]

		drawCommunications(application.communications, polygons)
	}

	def private static drawCommunications(List<CommunicationClazzClientSide> communications,
		List<PrimitiveObject> polygons) {
		val commuList = new ArrayList<CommunicationAccumulator>

		communications.forEach [
			val source = if (it.source.parent.opened) it.source else findFirstOpenComponent(it.source.parent)
			val target = if (it.target.parent.opened) it.target else findFirstOpenComponent(it.target.parent)
			if (source != null && target != null) {
				var found = false
				for (commu : commuList) {
					if (found == false) {
						found = ((commu.source == source) && (commu.target == target))

						if (found) {
							commu.requestCount = commu.requestCount + it.requestsPerSecond
						}
					}
				}

				if (found == false) {
					val newCommu = new CommunicationAccumulator()
					newCommu.source = source
					newCommu.target = target
					newCommu.requestCount = it.requestsPerSecond
					commuList.add(newCommu)
				}
			}
		]

		commuList.forEach [
			drawCommunication(it.source, it.target, it.requestCount, polygons)
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
		List<PrimitiveObject> polygons) {
		val pipe = createPipe(communicationColor,
			new Vector3f(source.positionX - centerPoint.x + source.width / 2f, source.positionY - centerPoint.y + 0.8f,
				source.positionZ - centerPoint.z + source.width / 2f),
			new Vector3f(target.positionX - centerPoint.x + target.width / 2f, target.positionY - centerPoint.y + 0.8f,
				target.positionZ - centerPoint.z + target.width / 2f),
			getCategoryForCommuincation(requestsPerSecond) * 0.1f + 0.02f)

		//commu.primitiveObjects.add(pipe) TODO
		polygons.add(pipe)
	}

	def private static int getCategoryForCommuincation(int requestsPerSecond) {
		if (requestsPerSecond == 0) {
			return 0
		} else if ((0 < requestsPerSecond) && (requestsPerSecond <= 15)) { // TODO quantile
			return 1
		} else if ((15 < requestsPerSecond) && (requestsPerSecond <= 30)) {
			return 2
		} else if ((30 < requestsPerSecond) && (requestsPerSecond <= 50)) {
			return 3
		} else if ((50 < requestsPerSecond) && (requestsPerSecond <= 80)) {
			return 4
		} else if ((80 < requestsPerSecond) && (requestsPerSecond <= 150)) {
			return 5
		} else {
			return 6
		}
	}

	def private static createPipe(Vector4f communicationColor, Vector3f start, Vector3f end, float lineThickness) {
		val communicationPipe = new Pipe()
		communicationPipe.setColor(communicationColor)
		communicationPipe.setLineThickness(lineThickness)
		communicationPipe.begin
		communicationPipe.addPoint(start)
		communicationPipe.addPoint(end)
		communicationPipe.end
		communicationPipe
	}

	def private static void drawOpenedComponent(ComponentClientSide component, List<PrimitiveObject> polygons, int index) {
		val box = component.createBox(centerPoint, component.color)
		var hackFactor = 0f
		if (index < 3) {
			hackFactor = index * 3f
		}
		val labelCenterPoint = new Vector3f(component.centerPoint.x - centerPoint.x + component.width / 4f - hackFactor,
			component.centerPoint.y - centerPoint.y,
			component.centerPoint.z - centerPoint.z + component.width / 2f + component.extension.z / 2f)
		val labelExtension = new Vector3f(component.extension.x / 2f, component.extension.y / 2f,
			component.extension.z / 2f)
		val label = createLabel(labelCenterPoint, labelExtension, component.name, false)

		component.primitiveObjects.add(box)

		polygons.add(box)
		polygons.add(label)

		component.clazzes.forEach [
			if (component.opened)
				drawClazzes(it, polygons, index + 1)
		]

		component.children.forEach [
			if (it.opened) {
				drawOpenedComponent(it, polygons, index + 1)
			} else {
				if (component.opened)
					drawClosedComponents(it, polygons, index + 1)
			}
		]
	}

	def private static void drawClosedComponents(ComponentClientSide component, List<PrimitiveObject> polygons,
		int index) {
		val box = component.createBox(centerPoint, component.color)
		val label = createLabel(component.centerPoint.sub(centerPoint), component.extension, component.name, true)

		component.primitiveObjects.add(box)

		polygons.add(box)
		polygons.add(label)
	}

	def private static void drawClazzes(ClazzClientSide clazz, List<PrimitiveObject> polygons, int index) {
		val box = clazz.createBox(centerPoint, clazz.color)
		val label = createLabel(
			new Vector3f(clazz.positionX - centerPoint.x + clazz.width / 2f,
				clazz.positionY - centerPoint.y + clazz.height / 2f,
				clazz.positionZ - centerPoint.z + clazz.width / 2f),
			new Vector3f(clazz.width / 2f, clazz.height / 2f, clazz.width / 2f),
			clazz.name,
			true
		)

		clazz.primitiveObjects.add(box)

		polygons.add(box)
		polygons.add(label)
	}

	def private static createLabel(Vector3f center, Vector3f itsExtension, String label, boolean white) {
		val texture = if (white == false) TextureManager::createTextureFromText(label, 512, 512) else TextureManager::
				createTextureFromTextWithWhite(label, 512, 512)

		new Quad(
			new Vector3f(center.x - itsExtension.x * 1.41f, center.y + itsExtension.y + 0.01f, center.z),
			new Vector3f(center.x, center.y + itsExtension.y + 0.01f, center.z + itsExtension.z * 1.41f),
			new Vector3f(center.x + itsExtension.x * 1.41f, center.y + itsExtension.y + 0.01f, center.z),
			new Vector3f(center.x, center.y + itsExtension.y + 0.01f, center.z - itsExtension.z * 1.41f),
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
