package explorviz.visualization.engine.primitives

import explorviz.shared.model.helper.CommunicationAppAccumulator
import explorviz.shared.model.helper.EdgeState
import explorviz.visualization.engine.buffer.BufferManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.renderer.ApplicationRenderer
import explorviz.visualization.renderer.ColorDefinitions
import java.util.ArrayList
import java.util.List

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
	def static void createPipe(CommunicationAppAccumulator entity, Vector3f viewCenterPoint, float lineThickness,
		Vector3f start, Vector3f end) {
		if (entity.state == EdgeState.HIDDEN) {
			return
		}

		val rememberedPipe = new PipeContainer.RememberedPipe()
		rememberedPipe.entity = entity
		rememberedPipe.viewCenterPoint = viewCenterPoint
		rememberedPipe.lineThickness = lineThickness
		rememberedPipe.start = start
		rememberedPipe.end = end

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

			val start = rememberedPipe.start.sub(rememberedPipe.viewCenterPoint)
			pipe.addPoint(start)

			val end = rememberedPipe.end.sub(rememberedPipe.viewCenterPoint)
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

			if (entity.state == EdgeState.SHOW_DIRECTION_IN) {
				prepareDirectionTriangle(start, end, true, false, entity)
			} else if (entity.state == EdgeState.SHOW_DIRECTION_OUT || entity.state == EdgeState.REPLAY_HIGHLIGHT) {
				prepareDirectionTriangle(start, end, false, true, entity)
			} else if (entity.state == EdgeState.SHOW_DIRECTION_IN_AND_OUT) {
				prepareDirectionTriangle(start, end, true, false, entity)
				prepareDirectionTriangle(end, start, false, true, entity)
				
			}
		}
		rememberedPipes.clear()

		rememberedTriangles.sortInplaceBy[outgoing]
		for (rememberedTriangle : rememberedTriangles) {
			val color = if (rememberedTriangle.outgoing)
					ColorDefinitions::communicationOutColor
				else
					ColorDefinitions::communicationInColor

			val triangle = new Triangle(null, color, false, true, rememberedTriangle.p1, rememberedTriangle.p2,
				rememberedTriangle.p3, 0f, 1f, 1f, 1f, 1f, 0f)

			rememberedTriangle.entity.primitiveObjects.add(triangle)

			extraTrianglesCount++
		}
		rememberedTriangles.clear()
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

		val heightCorrection = if (outgoing) {commu.source.height} else {commu.target.height}
		var scale = tempTLength.length + triangleStartWidth + heightCorrection
		
		if (scale + triangleWidth > direction.length) {
			scale = direction.length /2f
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
		@Property CommunicationAppAccumulator entity
		@Property Vector3f viewCenterPoint
		@Property float lineThickness
		@Property Vector3f start
		@Property Vector3f end
	}

	private static class RememberedTriangle {
		@Property Vector3f p1
		@Property Vector3f p2
		@Property Vector3f p3
		@Property boolean outgoing
		@Property CommunicationAppAccumulator entity

		new(Vector3f p1, Vector3f p2, Vector3f p3, boolean outgoing, CommunicationAppAccumulator accumulator) {
			this.p1 = p1
			this.p2 = p2
			this.p3 = p3
			this.outgoing = outgoing
			this.entity = accumulator
		}

	}
}
