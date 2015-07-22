package explorviz.visualization.engine.buffer

import elemental.html.Float32Array
import elemental.html.WebGLBuffer
import elemental.html.WebGLRenderingContext
import elemental.html.WebGLTexture
import explorviz.visualization.engine.FloatArray
import explorviz.visualization.engine.shaders.ShaderObject

import static extension explorviz.visualization.main.ArrayExtensions.*
import explorviz.visualization.engine.primitives.Pipe

class BufferManager {
	private static WebGLRenderingContext glContext
	private static ShaderObject shaderObject
	private static WebGLBuffer buffer

	private static val int DEFAULT_BUFFER_POINT_LENGTH = 65536

	private static val int VERTICES_DIM = 3
	private static val int TEXTURECOORDS_DIM = 2
	private static val int COLORS_DIM = 4
	private static val int NORMALS_DIM = 3

	private static Float32Array vertices
	private static Float32Array textureCoords
	private static Float32Array colors
	private static Float32Array normals

	private static int currentBufferItemCount = 0

	private new() {
	}

	def static init(WebGLRenderingContext glContextParam, ShaderObject attribsParam) {
		glContext = glContextParam
		shaderObject = attribsParam

		buffer = glContext.createBuffer()
		glContext.bindBuffer(WebGLRenderingContext::ARRAY_BUFFER, buffer)
		glContext.activeTexture(WebGLRenderingContext::TEXTURE0)

		glContext.blendFunc(WebGLRenderingContext::SRC_ALPHA, WebGLRenderingContext::ONE_MINUS_SRC_ALPHA)
		glContext.disable(WebGLRenderingContext::BLEND)
		
		glContext.enable(WebGLRenderingContext::DEPTH_TEST)
		glContext.depthFunc(WebGLRenderingContext::LEQUAL)

		clear()
	}

	private def static void clear() {
		vertices = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * VERTICES_DIM)
		textureCoords = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * TEXTURECOORDS_DIM)
		colors = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * COLORS_DIM)
		normals = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * NORMALS_DIM)
		currentBufferItemCount = 0
	}

	def static begin() {
		currentBufferItemCount = 0
	}

	def static int addTriangle(float[] verticesToAdd, float[] textureCoordsToAdd, float[] colorToAdd,
		float[] normalToAdd) {
		val startOffset = currentBufferItemCount

		vertices = mergeArray(verticesToAdd, vertices, currentBufferItemCount * VERTICES_DIM)
		textureCoords = mergeArray(textureCoordsToAdd, textureCoords, currentBufferItemCount * TEXTURECOORDS_DIM)
		colors = mergeArray(colorToAdd, colors, currentBufferItemCount * COLORS_DIM)
		normals = mergeArray(normalToAdd, normals, currentBufferItemCount * NORMALS_DIM)

		currentBufferItemCount = currentBufferItemCount + 3
		startOffset
	}

	def private static mergeArray(float[] valuesToAdd, Float32Array targetArray, int startOffset) {
		val currentMaxBufferSize = targetArray.getLength

		var currentTargetArray = targetArray

		if (currentMaxBufferSize <= startOffset + valuesToAdd.length) {
			currentTargetArray = FloatArray::create(currentMaxBufferSize * 2)
			FloatArray::set(currentTargetArray, targetArray, 0)
		}

		FloatArray::set(currentTargetArray, valuesToAdd, startOffset)
		currentTargetArray
	}

	def static int addQuad(float[] verticesToAdd, float[] textureCoordsToAdd, float[] colorToAdd, float[] normalToAdd) {
		val startOffset = currentBufferItemCount

		vertices = mergeArray(verticesToAdd, vertices, currentBufferItemCount * VERTICES_DIM)
		textureCoords = mergeArray(textureCoordsToAdd, textureCoords, currentBufferItemCount * TEXTURECOORDS_DIM)
		colors = mergeArray(colorToAdd, colors, currentBufferItemCount * COLORS_DIM)
		normals = mergeArray(normalToAdd, normals, currentBufferItemCount * NORMALS_DIM)

		currentBufferItemCount = currentBufferItemCount + 6
		startOffset
	}

	def static end() {
		fillBuffer()
	}

	def static void fillBuffer() {
		val texCoordsOffset = vertices.getByteLength()
		val colorsOffset = texCoordsOffset + textureCoords.getByteLength()
		val normalsOffset = colorsOffset + colors.getByteLength()

		glContext.bufferData(WebGLRenderingContext::ARRAY_BUFFER, normalsOffset + normals.getByteLength(),
			WebGLRenderingContext::STATIC_DRAW)

		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, 0, vertices)
		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, texCoordsOffset, textureCoords)
		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, colorsOffset, colors)
		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, normalsOffset, normals)

		glContext.vertexAttribPointer(shaderObject.vertexPositionAttribute, VERTICES_DIM,
			WebGLRenderingContext::FLOAT, false, 0, 0)
		glContext.vertexAttribPointer(shaderObject.textureCoordAttribute, TEXTURECOORDS_DIM,
			WebGLRenderingContext::FLOAT, false, 0, texCoordsOffset)
		glContext.vertexAttribPointer(shaderObject.vertexColorAttribute, COLORS_DIM, WebGLRenderingContext::FLOAT,
			false, 0, colorsOffset)
		glContext.vertexAttribPointer(shaderObject.vertexNormalAttribute, NORMALS_DIM, WebGLRenderingContext::FLOAT,
			false, 0, normalsOffset)
	}

	def static void refillVertices() {
		val verticesOffset = 0

		if (glContext != null) {
			glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, verticesOffset, vertices)
		}
	}
	
	def static void refillColors() {
		val colorsOffset = vertices.getByteLength() + textureCoords.getByteLength()

		if (glContext != null) {
			glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, colorsOffset, colors)
		}
	}
	
	def static void refillNormals() {
		val normalsOffset = vertices.getByteLength() + textureCoords.getByteLength() + colors.getByteLength()

		if (glContext != null) {
			glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, normalsOffset, normals)
		}
	}

	def static final void drawTriangle(int offsetInBuffer, WebGLTexture texture, boolean transparent, boolean drawWithoutDepthTest) {
		drawAbstractGeo(transparent, drawWithoutDepthTest, texture)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, 3)
	}

	def private static drawAbstractGeo(boolean transparent, boolean drawWithoutDepthTest, WebGLTexture texture) {
		if (drawWithoutDepthTest) {
			glContext.disable(WebGLRenderingContext::DEPTH_TEST)
		} else {
			glContext.enable(WebGLRenderingContext::DEPTH_TEST)
			}
		
		if (transparent) {
			glContext.enable(WebGLRenderingContext::BLEND)
		} else {
			glContext.disable(WebGLRenderingContext::BLEND)
		}

		if (texture != null) {
			glContext.uniform1f(shaderObject.useTextureUniform, 1)
			glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, texture)
		} else {
			glContext.uniform1f(shaderObject.useTextureUniform, 0)
		}
	}

	def static final void drawQuad(int offsetInBuffer, WebGLTexture texture, boolean transparent, boolean drawWithoutDepthTest) {
		drawAbstractGeo(transparent, drawWithoutDepthTest, texture)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, 6)
	}
	
	def static final void drawLabelsAtOnce(int offsetInBuffer, WebGLTexture texture, int letterCount) {
		drawAbstractGeo(true, true, texture)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, letterCount * 6)
	}
	
	def static final void drawLineAtOnce(int offsetInBuffer, int lineQuadsCount, int lineTrianglesCount) {
		drawAbstractGeo(false, false, null)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, lineQuadsCount * 2 * 3 + lineTrianglesCount * 3)
	}
	
	def static final void drawLineTrianglesAtOnce(int offsetInBuffer, int lineTrianglesCount) {
		drawAbstractGeo(false, false, null)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, lineTrianglesCount * 3)
	}
	
	def static final void drawBoxesAtOnce(int offsetInBuffer, int boxCount) {
		drawAbstractGeo(false, false, null)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, boxCount * 6 * 6)
	}
	
	def static final void drawQuadsAtOnce(int offsetInBuffer, int quadCount) {
		drawAbstractGeo(false, false, null)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, quadCount * 2 * 3)
	}
	
	def static final void drawQuadsWithAppTextureAtOnce(int offsetInBuffer, int quadCount, WebGLTexture texture) {
		drawAbstractGeo(false, false, texture)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, quadCount * 2 * 3)
	}
	
	def static final void drawPipesAtOnce(int offsetInBuffer, int pipeCount, boolean transparent, int extraTrianglesCount) {
		drawAbstractGeo(transparent, true, null)

		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, pipeCount * 6 * Pipe::smoothnessQuadsCount + extraTrianglesCount * 3)
	}

	def static overrideColor(int offsetInBuffer, float[] newColor) {
		val ithComponent = offsetInBuffer

		val localColorsOffset = ithComponent * COLORS_DIM
		for (var i = 0; i < newColor.length; i++) {
			FloatArray::set(colors, newColor.get(i), localColorsOffset + i)
		}

		refillColors()
	}

	def static setNewVerticesPosition(int offsetInBuffer, float[] newPositions) {
		//		val ithComponent = offsetInBuffer
		//
		//		val localNewVerticesOffset = ithComponent * VERTICES_DIM
		//		for (var i = 0; i < 3 * VERTICES_DIM; i++) {
		//			FloatArray::set(newVertices, newPositions.get(i), localNewVerticesOffset + i)
		//		}
		//        fillBuffer() // Dont fill buffer for each new vertices position
	}
}
