package explorviz.visualization.engine.primitives

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f
import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors
import elemental.html.WebGLTexture
import explorviz.shared.model.Component
import explorviz.visualization.renderer.ColorDefinitions

class Box extends PrimitiveObject {
	@Accessors val quads = new ArrayList<Quad>(6)

	public var Vector3f center
	public var Vector3f extensionInEachDirection

	public var Vector4f color
	public var WebGLTexture texture

	@Accessors public Component comp

	var boolean highlighted = false

	new(Vector3f center, Vector3f extensionInEachDirection, WebGLTexture texture) {
		createBoxGeneric(center, extensionInEachDirection, texture, null)
	}

	new(Vector3f center, Component comp) {
		this.comp = comp
		var Vector4f compColor = comp.color
		
		if(comp.highlighted)
			compColor = ColorDefinitions::highlightColor
			
		createBoxGeneric(center, comp.extension, null, compColor)
	}

	new(Vector3f center, Vector3f extensionInEachDirection, Vector4f color) {
		createBoxGeneric(center, extensionInEachDirection, null, color)
	}

	new(Vector3f center, Vector3f extensionInEachDirection) {
		createBoxGeneric(center, extensionInEachDirection, null, null)
	}

	def void createBoxGeneric(Vector3f centerParam, Vector3f extensionInEachDirection, WebGLTexture texture,
		Vector4f color) {
		this.center = centerParam
		this.extensionInEachDirection = extensionInEachDirection
		if (color != null) {
			this.color = color

		} else {
			this.texture = texture
		}

		// from the viewpoint of the front!
		val pointFrontBottomLeft = new Vector3f(center.x - extensionInEachDirection.x,
			center.y - extensionInEachDirection.y, center.z + extensionInEachDirection.z)
		val pointFrontBottomRight = new Vector3f(center.x + extensionInEachDirection.x,
			center.y - extensionInEachDirection.y, center.z + extensionInEachDirection.z)
		val pointFrontTopRight = new Vector3f(center.x + extensionInEachDirection.x,
			center.y + extensionInEachDirection.y, center.z + extensionInEachDirection.z)
		val pointFrontTopLeft = new Vector3f(center.x - extensionInEachDirection.x,
			center.y + extensionInEachDirection.y, center.z + extensionInEachDirection.z)

		// from the viewpoint of the back!
		val pointBackBottomLeft = new Vector3f(center.x + extensionInEachDirection.x,
			center.y - extensionInEachDirection.y, center.z - extensionInEachDirection.z)
		val pointBackBottomRight = new Vector3f(center.x - extensionInEachDirection.x,
			center.y - extensionInEachDirection.y, center.z - extensionInEachDirection.z)
		val pointBackTopRight = new Vector3f(center.x - extensionInEachDirection.x,
			center.y + extensionInEachDirection.y, center.z - extensionInEachDirection.z)
		val pointBackTopLeft = new Vector3f(center.x + extensionInEachDirection.x,
			center.y + extensionInEachDirection.y, center.z - extensionInEachDirection.z)

		if (color != null) {
			val quadFront = new Quad(pointFrontBottomLeft, pointFrontBottomRight, pointFrontTopRight, pointFrontTopLeft,
				color)
			quads.add(quadFront)

			val quadUpper = new Quad(pointFrontTopLeft, pointFrontTopRight, pointBackTopLeft, pointBackTopRight, color)
			quads.add(quadUpper)

			val quadLeft = new Quad(pointBackBottomRight, pointFrontBottomLeft, pointFrontTopLeft, pointBackTopRight,
				color)
			quads.add(quadLeft)

			val quadBack = new Quad(pointBackBottomLeft, pointBackBottomRight, pointBackTopRight, pointBackTopLeft,
				color)
			quads.add(quadBack)

			val quadBottom = new Quad(pointFrontBottomRight, pointFrontBottomLeft, pointBackBottomRight,
				pointBackBottomLeft, color)
			quads.add(quadBottom)

			val quadRight = new Quad(pointFrontBottomRight, pointBackBottomLeft, pointBackTopLeft, pointFrontTopRight,
				color)
			quads.add(quadRight)
		} else {
			val quadFront = new Quad(pointFrontBottomLeft, pointFrontBottomRight, pointFrontTopRight, pointFrontTopLeft,
				texture)
			quads.add(quadFront)

			val quadUpper = new Quad(pointFrontTopLeft, pointFrontTopRight, pointBackTopLeft, pointBackTopRight,
				texture)
			quads.add(quadUpper)

			val quadLeft = new Quad(pointBackBottomRight, pointFrontBottomLeft, pointFrontTopLeft, pointBackTopRight,
				texture)
			quads.add(quadLeft)

			val quadBack = new Quad(pointBackBottomLeft, pointBackBottomRight, pointBackTopRight, pointBackTopLeft,
				texture)
			quads.add(quadBack)

			val quadBottom = new Quad(pointFrontBottomRight, pointFrontBottomLeft, pointBackBottomRight,
				pointBackBottomLeft, texture)
			quads.add(quadBottom)

			val quadRight = new Quad(pointFrontBottomRight, pointBackBottomLeft, pointBackTopLeft, pointFrontTopRight,
				texture)
			quads.add(quadRight)
		}
	}

	override void draw() {
		for (quad : quads) {
			quad.draw()
		}
	}

	override getVertices() {
		quads.get(0).vertices
	}

	override highlight(Vector4f color) {
		highlighted = true

		this.color = color

		for (quad : quads) {
			quad.highlight(color)
		}
	}

	override unhighlight() {
		highlighted = false

		for (quad : quads) {
			quad.unhighlight()
		}
	}

	override moveByVector(Vector3f vector) {
		for (quad : quads)
			quad.moveByVector(vector)
	}

	override isHighlighted() {
		highlighted
	}

	def getCenter() {
		return BoxNative::getCenter(this)
	}

	def getExtensions() {
		return BoxNative::getExtensions(this)
	}

	def getColor() {
		return BoxNative::getColor(this)
	}

}
