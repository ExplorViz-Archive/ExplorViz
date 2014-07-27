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
	val static BLACK = new Vector4f(0f, 0f, 0f, 1f)
	var static CanvasElement canvasElement

	public val static fontSize = 64
	public val static letterStartCode = 32
	public val static lettersPerSide = 16

	def static init() {
		canvasElement = Browser::getDocument().createCanvasElement()
	}

	def static createTextureFromTextAndImagePath(String text, String relativeImagePath, int textureWidth,
		int textureHeight, int textSize, Vector4f fgcolor, Vector4f bgcolor, Vector4f bgcolorRight) {
		val img = new Image()
		val texture = WebGLStart::glContext.createTexture()
		val backgroundColor = if (bgcolor == null) BLACK else bgcolor
		val foregroundColor = if (fgcolor == null) BLACK else fgcolor
		val backgroundRightColor = if (bgcolorRight == null) BLACK else bgcolorRight
		val imgHandler = img.addLoadHandler(
			[
				val CanvasRenderingContext2D context = create2DContext(textureWidth, textureHeight)
				context.rect(0, 0, textureWidth, textureHeight);
				val gradient = context.createLinearGradient(0, 0, textureWidth, textureHeight)
				gradient.addColorStop(0,
					"rgba(" + Math.round(backgroundColor.x * 255) + ", " + Math.round(backgroundColor.y * 255) + ", " +
						Math.round(backgroundColor.z * 255) + ", " + Math.round(backgroundColor.w) * 255 + ")")
				gradient.addColorStop(1,
					"rgba(" + Math.round(backgroundRightColor.x * 255) + ", " + Math.round(backgroundRightColor.y * 255) +
						", " + Math.round(backgroundRightColor.z * 255) + ", " +
						Math.round(backgroundRightColor.w) * 255 + ")")
				context.fillStyle = gradient;
				context.fill();
				//				context.fillStyle = "rgba(" + Math.round(backgroundColor.x * 255) + ", " + Math.round(backgroundColor.y * 255) + ", " +
				//					Math.round(backgroundColor.z * 255) + ", " + Math.round(backgroundColor.w) * 255 + ")"
				//				context.fillRect(0, 0, textureWidth, textureHeight)
				context.font = 'bold ' + textSize + 'px Arial'
				context.lineWidth = 8
				context.textAlign = 'center'
				context.textBaseline = 'middle'
				context.fillStyle = "rgba(" + Math.round(foregroundColor.x * 255) + ", " +
					Math.round(foregroundColor.y * 255) + ", " + Math.round(foregroundColor.z * 255) + ", " +
					Math.round(foregroundColor.w * 255) + ")"
				context.fillText(text, 350 / 2 + 32, 128)
				context.drawImage(NativeImageCreator.createImage(img), 350, 64, 128, 100)
				createFromCanvas(context.canvas, texture)
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

	def static createLetterTexture(String colorCapitalized) {
		createTextureFromImagePath("font/font" + colorCapitalized + ".png")
	}

	def static createTextureFromTextWithBgColor(String text, int textureWidth, int textureHeight,
		Vector4f backgroundColor) {
		createTextureFromText(text, textureWidth, textureHeight, 0, 0, 0, 'normal 36px Arial', backgroundColor)
	}

	def static createTextureFromTextWithTextSizeWithFgColorWithBgColor(String text, int textureWidth, int textureHeight,
		int textSize, Vector4f foregroundColor, Vector4f backgroundColor) {
		createTextureFromText(text, textureWidth, textureHeight, Math.round(foregroundColor.x * 255),
			Math.round(foregroundColor.y * 255), Math.round(foregroundColor.z * 255), 'bold ' + textSize + 'px Arial',
			backgroundColor)
	}

	def static createTextureFromText(String text, int textureWidth, int textureHeight, int r, int g, int b,
		String fontString, Vector4f backgroundColor) {
		val CanvasRenderingContext2D context = create2DContext(textureWidth, textureHeight)

		if (backgroundColor.w > 0.01f) {
			context.fillStyle = "rgba(" + Math.round(backgroundColor.x * 255) + ", " +
				Math.round(backgroundColor.y * 255) + ", " + Math.round(backgroundColor.z * 255) + ", " +
				Math.round(backgroundColor.w) * 255 + ")"
			context.fillRect(0, 0, textureWidth, textureHeight)
		} else {
			context.clearRect(0, 0, textureWidth, textureHeight)
		}

		context.font = fontString
		context.lineWidth = 8
		context.textAlign = 'center'
		context.textBaseline = 'middle'

		context.fillStyle = "rgba(" + r + "," + g + "," + b + ", 255)"
		context.fillText(text, textureWidth / 2, textureHeight / 2)

		createTextureFromCanvas(context.canvas)
	}

	def static createTextureFromImagePath(String relativeImagePath, int offsetX, int offsetY, int width, int height,
		int textureWidth, int textureHeight) {
		val img = new Image()
		val texture = WebGLStart::glContext.createTexture()
		val imgHandler = img.addLoadHandler(
			[
				val CanvasRenderingContext2D context = create2DContext(textureWidth, textureHeight)
				context.clearRect(0, 0, textureWidth, textureHeight)
				context.drawImage(NativeImageCreator.createImage(img), offsetX, offsetY, width, height)
				createFromCanvas(context.canvas, texture)
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

	def static create2DContext(int textureWidth, int textureHeight) {
		canvasElement.setWidth(textureWidth)
		canvasElement.setHeight(textureHeight)
		canvasElement.getContext("2d") as CanvasRenderingContext2D
	}

	def static createTextureFromImagePath(String relativeImagePath) {
		val img = new Image()
		val texture = WebGLStart::glContext.createTexture()
		val imgHandler = img.addLoadHandler(
			[
				WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, texture)
				WebGLStart::glContext.texImage2D(WebGLRenderingContext::TEXTURE_2D, 0, WebGLRenderingContext::RGBA,
					WebGLRenderingContext::RGBA, WebGLRenderingContext::UNSIGNED_BYTE,
					NativeImageCreator.createImage(img))
				WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D,
					WebGLRenderingContext::TEXTURE_MIN_FILTER, WebGLRenderingContext::LINEAR_MIPMAP_LINEAR)
				WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D,
					WebGLRenderingContext::TEXTURE_MAG_FILTER, WebGLRenderingContext::LINEAR)
				WebGLStart::glContext.generateMipmap(WebGLRenderingContext::TEXTURE_2D)
				WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, null)
				val imgHandler = imgHandlers.get(img)
				imgHandlers.remove(img)
				if (imgHandler != null) imgHandler.removeHandler
				RootPanel.get().remove(img);
			])

		// TODO WebGLStart::glContext.deleteTexture()
		imgHandlers.put(img, imgHandler)

		img.setUrl("images/" + relativeImagePath)
		img.setVisible(false);
		RootPanel.get().add(img);
		texture
	}

	def static createTextureFromCanvas(CanvasElement canvas) {
		createFromCanvas(canvas, WebGLStart::glContext.createTexture())
	}

	def static createFromCanvas(CanvasElement canvas, WebGLTexture texture) {
		WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, texture)

		WebGLStart::glContext.texImage2D(WebGLRenderingContext::TEXTURE_2D, 0, WebGLRenderingContext::RGBA,
			WebGLRenderingContext::RGBA, WebGLRenderingContext::UNSIGNED_BYTE, canvas)

		WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D,
			WebGLRenderingContext::TEXTURE_MIN_FILTER, WebGLRenderingContext::LINEAR)

		// LINEAR_MIPMAP_NEAREST
		WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D, WebGLRenderingContext::TEXTURE_MAG_FILTER,
			WebGLRenderingContext::LINEAR)

		//		WebGLStart::glContext.generateMipmap(WebGLRenderingContext::TEXTURE_2D)
		WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, null)
		texture
	}
}
