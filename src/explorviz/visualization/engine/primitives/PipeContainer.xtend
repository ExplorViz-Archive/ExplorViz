package explorviz.visualization.engine.primitives

import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.renderer.ApplicationRenderer
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.List
import explorviz.visualization.highlighting.NodeHighlighter
import explorviz.shared.model.Clazz
import explorviz.shared.model.helper.Draw3DNodeEntity
import explorviz.shared.model.Component
import org.eclipse.xtend.lib.annotations.Accessors

class PipeContainer {
	val static List<PipeContainer.RememberedPipe> rememberedPipes = new ArrayList<PipeContainer.RememberedPipe>()
	val static List<PipeContainer.RememberedTriangle> rememberedTriangles = new ArrayList<PipeContainer.RememberedTriangle>()

	var static int pipeTransparentCount = 0
	var static int pipeTransparentOffsetInBuffer = 0

	var static int pipeCount = 0
	var static int pipeOffsetInBuffer = 0

	var static int extraTrianglesCount = 0

	def static init() {
		clear()
	}

	def static clear() {
		pipeTransparentCount = 0
		pipeTransparentOffsetInBuffer = 0

		pipeCount = 0
		pipeOffsetInBuffer = 0

		extraTrianglesCount = 0
	}

	/**
	 * ATTENTION: all boxes must be created in batch! call doBoxCreation when finished
	 */
	def static void createPipe(CommunicationAppAccumulator entity, Vector3f viewCenterPoint, float lineThickness) {
		if (entity.state == EdgeState.HIDDEN) {
			return
		}

		val rememberedPipe = new PipeContainer.RememberedPipe()
		rememberedPipe.entity = entity
		rememberedPipe.viewCenterPoint = viewCenterPoint
		rememberedPipe.lineThickness = lineThickness

		rememberedPipes.add(rememberedPipe)
	}

	def static void doPipeCreation() {
		rememberedPipes.sortInplaceBy[entity.state != EdgeState.TRANSPARENT]
		rememberedPipes.sortInplaceBy[entity.state == EdgeState.REPLAY_HIGHLIGHT]

		for (rememberedPipe : rememberedPipes) {
			val entity = rememberedPipe.entity
			val transparent = entity.state == EdgeState.TRANSPARENT

			val color = if (transparent)
					ColorDefinitions::pipeColorTrans
				else if (entity.state == EdgeState.REPLAY_HIGHLIGHT)
					ColorDefinitions::pipeHighlightColor
				else
					ColorDefinitions::pipeColor

			val pipe = new Pipe(transparent, true, color)

			pipe.setLineThickness(rememberedPipe.lineThickness)

			val possibleStart = rememberedPipe.entity.points.get(0).sub(rememberedPipe.viewCenterPoint)
			val possibleEnd = rememberedPipe.entity.points.get(1).sub(rememberedPipe.viewCenterPoint)

			val start = possibleStart
			pipe.addPoint(start)

			val end = possibleEnd
			pipe.addPoint(end)

			entity.primitiveObjects.add(pipe)

			if (transparent) {
				if (pipeTransparentCount == 0) {
					pipeTransparentOffsetInBuffer = pipe.quads.get(0).offsetStart
				}
				pipeTransparentCount++
			} else {
				if (pipeCount == 0) {
					pipeOffsetInBuffer = pipe.quads.get(0).offsetStart
				}
				pipeCount++
			}

			if (NodeHighlighter::highlightedNode != null) {
				var incoming = determineIncoming(entity)
				var outgoing = determineOutgoing(entity)

				if (incoming && outgoing) {
					var switchedEntity = new CommunicationAppAccumulator()
					switchedEntity.source = entity.target
					switchedEntity.target = entity.source

					val target = rememberedPipe.entity.target
					val shouldBeSwitched = if (NodeHighlighter::highlightedNode != null) {
							(target != null &&
								target.fullQualifiedName == NodeHighlighter::highlightedNode.fullQualifiedName)
						} else
							false

					if (shouldBeSwitched) {
						prepareDirectionTriangle(start, end, true, false, entity)
						prepareDirectionTriangle(end, start, false, true, switchedEntity)
					} else {
						prepareDirectionTriangle(end, start, true, false, switchedEntity)
						prepareDirectionTriangle(start, end, false, true, entity)
					}
				} else if (outgoing) {
					prepareDirectionTriangle(start, end, false, true, entity)
				} else if (incoming) {
					prepareDirectionTriangle(start, end, true, false, entity)
				}
			}
		}
		rememberedPipes.clear()

		rememberedTriangles.sortInplaceBy[outgoing]
		for (rememberedTriangle : rememberedTriangles) {
			val color = if (rememberedTriangle.outgoing)
					ColorDefinitions::communicationOutColor
				else
					ColorDefinitions::communicationInColor

			new Triangle(null, color, false, true, rememberedTriangle.p1, rememberedTriangle.p2,
				rememberedTriangle.p3, 0f, 1f, 1f, 1f, 1f, 0f)

			//			rememberedTriangle.entity.primitiveObjects.add(triangle)
			extraTrianglesCount++
		}
		rememberedTriangles.clear()
	}

	public def static boolean determineOutgoing(CommunicationAppAccumulator commuApp) {
		for (commu : commuApp.aggregatedCommunications) {
			if (isClazzChildOf(commu.source, NodeHighlighter::highlightedNode)) {
				return true
			}
		}

		false
	}

	private def static isClazzChildOf(Clazz clazz, Draw3DNodeEntity entity) {
		if (entity instanceof Clazz) {
			return clazz.fullQualifiedName == entity.fullQualifiedName
		}

		isClazzChildOfHelper(clazz.parent, entity)
	}

	private def static boolean isClazzChildOfHelper(Component component, Draw3DNodeEntity entity) {
		if (component == null) {
			return false
		}

		if (component.fullQualifiedName == entity.fullQualifiedName) {
			return true
		}

		isClazzChildOfHelper(component.parentComponent, entity)
	}

	public def static boolean determineIncoming(CommunicationAppAccumulator commuApp) {
		for (commu : commuApp.aggregatedCommunications) {
			if (isClazzChildOf(commu.target, NodeHighlighter::highlightedNode)) {
				return true
			}
		}

		false
	}

	def static private void prepareDirectionTriangle(Vector3f start, Vector3f end, boolean incoming, boolean outgoing,
		CommunicationAppAccumulator commu) {
		if (incoming) {
			prepareDirectionTriangleHelper(start, end, outgoing, commu)
		} else if (outgoing) {
			prepareDirectionTriangleHelper(end, start, outgoing, commu)
		}
	}

	def static private void prepareDirectionTriangleHelper(Vector3f start, Vector3f end, boolean outgoing,
		CommunicationAppAccumulator commu) {
		val triangleWidth = 2.5f
		val triangleStartWidth = 2f
		val direction = start.sub(end)

		var t = 1f
		var t1 = 1f
		var t2 = 1f

		if (!outgoing) {
			val q = commu.target.position.sub(ApplicationRenderer::viewCenterPoint)
			t1 = Math.abs(getTValueForSecondVector(end, start, q, new Vector3f(q.x + commu.target.width, 0f, q.z)))
			t2 = Math.abs(getTValueForSecondVector(end, start, q, new Vector3f(q.x, 0f, q.z + commu.target.depth)))
		} else {
			val q = commu.source.position.sub(ApplicationRenderer::viewCenterPoint)
			t1 = Math.abs(getTValueForSecondVector(end, start, q, new Vector3f(q.x + commu.source.width, 0f, q.z)))
			t2 = Math.abs(getTValueForSecondVector(end, start, q, new Vector3f(q.x, 0f, q.z + commu.source.depth)))
		}

		t = t1

		if (t < -100f || 100f < t) {

			// t1 is infinity
			t = t2
		} else if (t1 > 0 && t2 > 0) {
			t = Math.min(t1, t2)
		}

		val tempTLength = start.sub(end).mult(t)
		tempTLength.y = 0f

		val heightCorrection = if (outgoing) {
				commu.source.height
			} else {
				commu.target.height
			}
		var scale = tempTLength.length + triangleStartWidth + heightCorrection

		if (scale + triangleWidth > direction.length) {
			scale = direction.length / 2f
		}

		val triangleStart = scaleVectorToLength(direction, scale)

		val triangleTip = scaleVectorToLength(direction, triangleWidth / 2f)
		val normal = createLineWidthVector(direction, triangleWidth)
		val Y_Start = end.y + triangleStart.y

		val p1 = new Vector3f(end.x + triangleStart.x + normal.x, Y_Start, end.z + triangleStart.z + normal.z)
		var Vector3f p2 = null
		var Vector3f p3 = null
		if (!outgoing) {
			val Y_Tip = end.y + triangleStart.y - triangleTip.y
			p2 = new Vector3f(end.x + triangleStart.x - triangleTip.x, Y_Tip, end.z + triangleStart.z - triangleTip.z)
			p3 = new Vector3f(end.x + triangleStart.x - normal.x, Y_Start, end.z + triangleStart.z - normal.z)
		} else {
			val Y_Tip = end.y + triangleStart.y + triangleTip.y
			p2 = new Vector3f(end.x + triangleStart.x - normal.x, Y_Start, end.z + triangleStart.z - normal.z)
			p3 = new Vector3f(end.x + triangleStart.x + triangleTip.x, Y_Tip, end.z + triangleStart.z + triangleTip.z)
		}

		rememberedTriangles.add(new PipeContainer.RememberedTriangle(p1, p2, p3, outgoing, commu))
	}

	def static private float getTValueForSecondVector(Vector3f p, Vector3f p2, Vector3f q, Vector3f q2) {
		val r = p2.sub(p)
		val s = q2.sub(q)
		val qMinusP = q.sub(p)

		crossProduct(qMinusP, s) / crossProduct(r, s)
	}

	def static private float crossProduct(Vector3f a, Vector3f b) {
		a.x * b.z - a.z * b.x
	}

	def static private Vector3f createLineWidthVector(Vector3f v, float triangleWidth) {
		val n = new Vector3f(v.z, v.y, -1 * v.x)
		val L = triangleWidth / 2f

		scaleVectorToLength(n, L)
	}

	def static private Vector3f scaleVectorToLength(Vector3f v, float L) {
		new Vector3f(v.x * L / v.length(), v.y * L / v.length(), v.z * L / v.length())
	}

	def static void drawTransparentPipes() {
		if (pipeTransparentCount > 0)
			BufferManager::drawPipesAtOnce(pipeTransparentOffsetInBuffer, pipeTransparentCount, true, 0)
	}

	def static void drawPipes() {
		if (pipeCount > 0)
			BufferManager::drawPipesAtOnce(pipeOffsetInBuffer, pipeCount, false, extraTrianglesCount)
	}

	private static class RememberedPipe {
		@Accessors CommunicationAppAccumulator entity
		@Accessors Vector3f viewCenterPoint
		@Accessors float lineThickness
	}

	private static class RememberedTriangle {
		@Accessors Vector3f p1
		@Accessors Vector3f p2
		@Accessors Vector3f p3
		@Accessors boolean outgoing
		@Accessors CommunicationAppAccumulator entity

		new(Vector3f p1, Vector3f p2, Vector3f p3, boolean outgoing, CommunicationAppAccumulator accumulator) {
			this.p1 = p1
			this.p2 = p2
			this.p3 = p3
			this.outgoing = outgoing
			this.entity = accumulator
		}

	}
}
