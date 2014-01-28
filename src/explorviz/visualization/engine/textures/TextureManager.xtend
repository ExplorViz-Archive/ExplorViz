package explorviz.visualization.engine.textures

import elemental.client.Browser

import elemental.html.WebGLRenderingContext
import elemental.html.CanvasElement
import elemental.html.WebGLTexture
import elemental.html.CanvasRenderingContext2D

import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.main.WebGLStart
import com.google.gwt.user.client.Window

class TextureManager {
    def static createTextureFromTextAndImagePath(String text, String relativeImagePath, int textureWidth, int textureHeight) {
        val imgElement = createImageElement(relativeImagePath)
        val texture = WebGLStart::glContext.createTexture()

		imgElement.addEventListener('load', [
        	Window::alert("xxx")
        	
            val CanvasRenderingContext2D context = create2DContext(textureWidth,textureHeight)
            
            context.clearRect(0,0,textureWidth,textureHeight)
        
            context.font = 'normal ' + 60 + 'px Arial'
            context.lineWidth = 8
            context.textAlign = 'center'
            context.textBaseline = 'middle'
        
            context.fillStyle = "rgba(" + 0 + ", " + 0 + ", " + 0 + ", 255)"
            context.fillText(text, 350 / 2 + 32, 128)
            
            context.drawImage(imgElement,350,64,128,100)
            
            createFromCanvas(context.canvas, texture)
        ], false)
        
        texture
    }
    
    def static createTextureFromText(String text, int textureWidth, int textureHeight) {
        createTextureFromText(text,textureWidth,textureHeight,0,0,0,'normal 90px Arial', new Vector3f(1f,1f,1f))
    }
    
    def static createTextureFromTextWithWhite(String text, int textureWidth, int textureHeight) {
        createTextureFromText(text,textureWidth,textureHeight,255,255,255,'normal 90px Arial', new Vector3f(1f,1f,1f))
    }
    
    def static createTextureFromText(String text, int textureWidth, int textureHeight, int r, int g, int b, String fontString, Vector3f background) {
        val CanvasRenderingContext2D context = create2DContext(textureWidth,textureHeight)
        
        context.clearRect(0,0,textureWidth,textureHeight)
        
        context.font = fontString
        context.lineWidth = 8
        context.textAlign = 'center'
        context.textBaseline = 'middle'
        
        context.fillStyle = "rgba("+ r +"," + g +"," + b + ", 255)"
        context.fillText(text, textureWidth / 2, textureHeight / 2)
        
        createTextureFromCanvas(context.canvas)
    }
    
    def static createTextureFromImagePath(String relativeImagePath, int offsetX, int offsetY, int width, int height, int textureWidth, int textureHeight) {
        val imgElement = createImageElement(relativeImagePath)
        val texture = WebGLStart::glContext.createTexture()

        imgElement.setOnload( [
            val CanvasRenderingContext2D context = create2DContext(textureWidth,textureHeight)
            
            context.clearRect(0,0,textureWidth,textureHeight)
            context.drawImage(imgElement,offsetX,offsetY,width,height)
            
            createFromCanvas(context.canvas, texture)
        ])
        texture
    }
    
    def static create2DContext(int textureWidth, int textureHeight) {
        val canvasElement =  Browser::getDocument().createCanvasElement()
        canvasElement.setWidth(textureWidth)
        canvasElement.setHeight(textureHeight)
        canvasElement.getContext("2d") as CanvasRenderingContext2D
    }
    
    def static createTextureFromImagePath(String relativeImagePath) {
        val imgElement = createImageElement(relativeImagePath)
        val texture = WebGLStart::glContext.createTexture()

        imgElement.setOnload( [
            bindTexture(texture)
            
            WebGLStart::glContext.texImage2D(WebGLRenderingContext::TEXTURE_2D, 0, WebGLRenderingContext::RGBA, WebGLRenderingContext::RGBA, WebGLRenderingContext::UNSIGNED_BYTE, imgElement)
            WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, null)
        ])
        texture
    }
    
    private def static createImageElement(String relativeImagePath) {
        val imgElement = Browser::getDocument().createImageElement()
        imgElement.setSrc("images/" + relativeImagePath)
        imgElement
    }

    def static bindTexture(WebGLTexture texture) {
        WebGLStart::glContext.activeTexture(WebGLRenderingContext::TEXTURE0)
        
        WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, texture)
        
        WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D, WebGLRenderingContext::TEXTURE_MIN_FILTER, WebGLRenderingContext::LINEAR)
        WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D, WebGLRenderingContext::TEXTURE_MAG_FILTER, WebGLRenderingContext::LINEAR)
        WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D, WebGLRenderingContext::TEXTURE_WRAP_S, WebGLRenderingContext::CLAMP_TO_EDGE)
        WebGLStart::glContext.texParameteri(WebGLRenderingContext::TEXTURE_2D, WebGLRenderingContext::TEXTURE_WRAP_T, WebGLRenderingContext::CLAMP_TO_EDGE)
    }
    
    def static createTextureFromCanvas(CanvasElement canvas) {
        createFromCanvas(canvas, WebGLStart::glContext.createTexture())
    }

    def static createFromCanvas(CanvasElement canvas, WebGLTexture texture) {
        bindTexture(texture)
        
        WebGLStart::glContext.texImage2D(WebGLRenderingContext::TEXTURE_2D, 0, WebGLRenderingContext::RGBA, WebGLRenderingContext::RGBA, WebGLRenderingContext::UNSIGNED_BYTE, canvas)
        WebGLStart::glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, null)
        texture
    }
}