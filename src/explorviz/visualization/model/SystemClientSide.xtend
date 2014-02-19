package explorviz.visualization.model

import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.math.Vector4f
import explorviz.visualization.engine.primitives.Quad
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.model.helper.DrawNodeEntity
import explorviz.visualization.engine.primitives.Rectangle
import explorviz.visualization.renderer.ColorDefinitions

class SystemClientSide extends DrawNodeEntity {
	@Property val List<NodeGroupClientSide> nodeGroups = new ArrayList<NodeGroupClientSide>
	@Property LandscapeClientSide parent
	@Property String name

	static val Vector4f plusColor = ColorDefinitions::systemPlusColor
	static val Vector4f foregroundColor = ColorDefinitions::systemForegroundColor
	static val Vector4f backgroundColor = ColorDefinitions::systemBackgroundColor

	var Quad quad
	var boolean opened

	def boolean isOpened() {
		opened
	}

	def void setOpened(boolean openedParam) {
		if (openedParam) {
			nodeGroups.forEach [
				it.visible = true
				it.setOpened(false)
			]
		} else {
			nodeGroups.forEach [
				it.visible = false
				it.setAllChildrenVisibility(false)
			]
		}

		this.opened = openedParam
	}

	def Quad createSystemQuad(float z, Vector3f centerPoint) {
		quad = createQuad(z, centerPoint, backgroundColor)
		quad
	}

	def Quad createSystemOpenSymbol() {
		val extensionX = 0.1f
		val extensionY = 0.1f

		val TOP_RIGHT = quad.cornerPoints.get(2)

		var float centerX = TOP_RIGHT.x - extensionX * 1.5f
		var float centerY = TOP_RIGHT.y - extensionY * 1.5f

		var symbol = "\u2013"
		if (!opened) symbol = "+"

		val texture = TextureManager::createTextureFromText(symbol, 128, 128, Math.round(plusColor.x * 255),
			Math.round(plusColor.y * 255), Math.round(plusColor.z * 255), 'bold 256px Arial', backgroundColor)

		new Quad(
			new Vector3f(centerX, centerY, TOP_RIGHT.z + 0.01f),
			new Vector3f(extensionX, extensionY, 0.0f),
			texture,
			null,
			true
		)
	}

	def createSystemLabel(Quad node, String name) {
		val ORIG_TOP_LEFT = node.cornerPoints.get(3)
		val ORIG_TOP_RIGHT = node.cornerPoints.get(2)

		val labelWidth = 2.5f
		val labelHeight = 1f

		val labelOffsetTop = 0.1f

		val absolutLabelLeftStart = ORIG_TOP_LEFT.x + ((ORIG_TOP_RIGHT.x - ORIG_TOP_LEFT.x) / 2f) - (labelWidth / 2f)

		val BOTTOM_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_TOP_LEFT.y - labelOffsetTop - labelHeight, 0.05f)
		val BOTTOM_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth,
			ORIG_TOP_RIGHT.y - labelOffsetTop - labelHeight, 0.05f)
		val TOP_RIGHT = new Vector3f(absolutLabelLeftStart + labelWidth, ORIG_TOP_RIGHT.y - labelOffsetTop, 0.05f)
		val TOP_LEFT = new Vector3f(absolutLabelLeftStart, ORIG_TOP_LEFT.y - labelOffsetTop, 0.05f)

		new Quad(BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT,
			TextureManager::
				createTextureFromTextWithTextSizeWithFgColorWithBgColor(name, 1024, 512, 150, foregroundColor,
					backgroundColor))
	}

	override void destroy() {
		nodeGroups.forEach[it.destroy()]
		super.destroy()
	}

	def Rectangle createSystemQuadRectangle(float z, Vector3f vector3f) {
		new Rectangle(quad.cornerPoints.get(0), quad.cornerPoints.get(1), quad.cornerPoints.get(2),
			quad.cornerPoints.get(3), new Vector4f(0.85f, 0.85f, 0.85f, 1f), true, z)
	}
}
