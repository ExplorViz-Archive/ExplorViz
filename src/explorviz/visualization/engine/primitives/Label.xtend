package explorviz.visualization.engine.primitives

import elemental.html.WebGLTexture
import java.util.ArrayList
import java.util.List
import explorviz.visualization.engine.textures.TextureManager
import explorviz.visualization.engine.math.Vector3f
import explorviz.visualization.engine.math.Vector4f

class Label extends PrimitiveObject{
	var static WebGLTexture letterTexture
	
	val List<Quad> letters = new ArrayList<Quad>() 
	
	def static init() {
		letterTexture = TextureManager::createLetterTexture()
	}
	
	new(String text) {
		letters.add(new Quad(new Vector3f(0,0,20), new Vector3f(10,10,10), letterTexture, null, true, true))
	}
	
	override getVertices() {
		// not used
		letters.get(0).vertices
	}
	
	override draw() {
		letters.forEach [
//			it.draw()
		]
	}
	
	override isHighlighted() {
		false
	}
	
	override highlight(Vector4f color) {
		// dont
	}
	
	override unhighlight() {
		// dont
	}
	
	override moveByVector(Vector3f vector) {
		// dont
	}
	
}