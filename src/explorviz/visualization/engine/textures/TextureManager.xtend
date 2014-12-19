package explorviz.visualization.engine.textures

import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.user.client.ui.Image
import com.google.gwt.user.client.ui.RootPanel
import elemental.client.Browser
import elemental.html.CanvasElement
import elemental.html.CanvasRenderingContext2D
import elemental.html.WebGLRenderingContext
import elemental.html.WebGLTexture
import explorviz.visualization.engine.main.WebGLStart
import explorviz.visualization.engine.math.Vector4f
import java.util.concurrent.ConcurrentHashMap

class TextureManager {
	val static imgHandlers = new ConcurrentHashMap<Image, HandlerRegistration>()
	var static CanvasElement canvasElement

	public val static fontSize = 64
	public val static letterStartCode = 32
	public val static lettersPerSide = 16

	def static init() {
		canvasElement = Browser::getDocument().createCanvasElement()
	}

	def static WebGLTexture createLetterTexture(String colorCapitalized) {
		createTextureFromImagePath("font/font" + colorCapitalized + ".png")
	}
	
	def static deleteTextureIfExisting(WebGLTexture texture) {
//		if (texture != null) {
//			WebGLStart::glContext.deleteTexture(texture)
//		}
	}

	def static WebGLTexture createTextureFromImagePath(String relativeImagePath) {
		val img = new Image()
		val texture = WebGLStart::glContext.createTexture()
		val imgHandler = img.addLoadHandler(
			[
				applyTextureFiltering(texture, img, null)
				val imgHandler = imgHandlers.get(img)
				imgHandlers.remove(img)
				if (imgHandler != null) imgHandler.removeHandler
				RootPanel.get().remove(img);
			])

		imgHandlers.put(img, imgHandler)

		img.setUrl("images/" + relativeImagePath)
		img.setVisible(false);
		RootPanel.get().add(img);
		texture
	}

	private def static void applyTextureFiltering(WebGLTexture texture, Image img, CanvasElement canvas) {
		WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, texture)
		if (img != null) {
			WebGLStart::glContext.texImage2D(WebGLRenderingContext::TEXTURE_2D, 0, WebGLRenderingContext::RGBA,
				WebGLRenderingContext::RGBA, WebGLRenderingContext::UNSIGNED_BYTE, NativeImageCreator.createImage(img))
		} else {
			WebGLStart::glContext.texImage2D(WebGLRenderingContext::TEXTURE_2D, 0, WebGLRenderingContext::RGBA,
				WebGLRenderingContext::RGBA, WebGLRenderingContext::UNSIGNED_BYTE, canvas)
		}

		WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D,
			WebGLRenderingContext::TEXTURE_MIN_FILTER, WebGLRenderingContext::LINEAR_MIPMAP_LINEAR)
		WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D,
			WebGLRenderingContext::TEXTURE_MAG_FILTER, WebGLRenderingContext::LINEAR)

		WebGLStart::glContext.generateMipmap(WebGLRenderingContext::TEXTURE_2D)
		WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, null)
	}

	def static WebGLTexture createGradientTexture(Vector4f firstColor, Vector4f secondColor, int textureWidth,
		int textureHeight) {
		canvasElement.setWidth(textureWidth)
		canvasElement.setHeight(textureHeight)
		val CanvasRenderingContext2D context = canvasElement.getContext("2d") as CanvasRenderingContext2D

		context.rect(0, 0, textureWidth, textureHeight);
		val gradient = context.createLinearGradient(0, 0, textureWidth, textureHeight)
		gradient.addColorStop(0, convertToRGBA(firstColor))
		gradient.addColorStop(1, convertToRGBA(secondColor))
		context.fillStyle = gradient;
		context.fill();

		val texture = WebGLStart::glContext.createTexture()
		applyTextureFiltering(texture, null, context.canvas)
		texture
	}

	private def static convertToRGBA(Vector4f color) {
		"rgba(" + Math.round(color.x * 255) + ", " + Math.round(color.y * 255) + ", " + Math.round(color.z * 255) + ", " +
			Math.round(color.w) * 255 + ")"
	}
}
