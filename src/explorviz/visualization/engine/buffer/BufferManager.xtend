package explorviz.visualization.engine.buffer

import elemental.html.Float32Array
import elemental.html.WebGLBuffer
import elemental.html.WebGLRenderingContext
import elemental.html.WebGLTexture
import explorviz.visualization.engine.FloatArray
import explorviz.visualization.engine.shaders.ShaderObject

import static extension explorviz.visualization.main.ArrayExtensions.*

class BufferManager {
	private static WebGLRenderingContext glContext
	private static ShaderObject shaderObject
	private static WebGLBuffer buffer

	private static val int DEFAULT_BUFFER_POINT_LENGTH = 65536 * 2

	private static val int VERTICES_DIM = 3
	private static val int TEXTURECOORDS_DIM = 2
	private static val int COLORS_DIM = 4
	private static val int NORMALS_DIM = 3

	private static Float32Array vertices
	private static Float32Array textureCoords
	private static Float32Array colors
	private static Float32Array normals
	private static Float32Array newVertices
	private static int currentBufferItemCount = 0

	private static boolean wasLastTransparent = false

	private new() {
	}

	def static init(WebGLRenderingContext glContextParam, ShaderObject attribsParam) {
		glContext = glContextParam
		shaderObject = attribsParam

		buffer = glContext.createBuffer()
		glContext.bindBuffer(WebGLRenderingContext::ARRAY_BUFFER, buffer)
		glContext.activeTexture(WebGLRenderingContext::TEXTURE0)

		glContext.disable(WebGLRenderingContext::BLEND)

		glContext.enable(WebGLRenderingContext::DEPTH_TEST)
		glContext.depthFunc(WebGLRenderingContext::LESS)

		clear()
	}

	def static void clear() {
		vertices = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * VERTICES_DIM)
		textureCoords = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * TEXTURECOORDS_DIM)
		colors = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * COLORS_DIM)
		normals = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * NORMALS_DIM)
		newVertices = FloatArray::create(DEFAULT_BUFFER_POINT_LENGTH * VERTICES_DIM)

		currentBufferItemCount = 0
	}

	def static begin() {
		clear()
	}

	def static int addTriangle(float[] verticesToAdd, float[] textureCoordsToAdd, float[] colorToAdd,
		float[] normalToAdd) {
		val startOffset = currentBufferItemCount

		vertices = mergeArray(verticesToAdd, vertices, currentBufferItemCount * VERTICES_DIM)
		textureCoords = mergeArray(textureCoordsToAdd, textureCoords, currentBufferItemCount * TEXTURECOORDS_DIM)

		colors = mergeArray(colorToAdd, colors, currentBufferItemCount * COLORS_DIM)
		colors = mergeArray(colorToAdd, colors, (currentBufferItemCount + 1) * COLORS_DIM)
		colors = mergeArray(colorToAdd, colors, (currentBufferItemCount + 2) * COLORS_DIM)

		normals = mergeArray(normalToAdd, normals, currentBufferItemCount * NORMALS_DIM)
		normals = mergeArray(normalToAdd, normals, (currentBufferItemCount + 1) * NORMALS_DIM)
		normals = mergeArray(normalToAdd, normals, (currentBufferItemCount + 2) * NORMALS_DIM)

		newVertices = mergeArray(verticesToAdd, newVertices, currentBufferItemCount * VERTICES_DIM)

		currentBufferItemCount = currentBufferItemCount + 3
		startOffset
	}

	def private static mergeArray(float[] valuesToAdd, Float32Array targetArray, int startOffset) {
		val currentMaxBufferSize = targetArray.getLength

		var currentTargetArray = targetArray

		if (currentMaxBufferSize <= startOffset + valuesToAdd.length) {

			// increase buffer
			// System::arraycopy cannot cope with such large puffer :(
			currentTargetArray = FloatArray::create(currentMaxBufferSize * 2)
			FloatArray::set(currentTargetArray, targetArray, 0)
		}

		FloatArray::set(currentTargetArray, valuesToAdd, startOffset)
		currentTargetArray
	}

	def static end() {
		fillBuffer()
	}

	def static void fillBuffer() {
		val texCoordsOffset = vertices.getByteLength()
		val colorsOffset = texCoordsOffset + textureCoords.getByteLength()
		val normalsOffset = colorsOffset + colors.getByteLength()
		val newVerticesOffset = normalsOffset + normals.getByteLength()

		glContext.bufferData(WebGLRenderingContext::ARRAY_BUFFER,
			vertices.getByteLength() + textureCoords.getByteLength() + colors.getByteLength() +
				normals.getByteLength() + newVertices.getByteLength(), WebGLRenderingContext::STATIC_DRAW)

		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, 0, vertices)
		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, texCoordsOffset, textureCoords)
		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, colorsOffset, colors)
		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, normalsOffset, normals)
		glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, newVerticesOffset, newVertices)

		glContext.vertexAttribPointer(shaderObject.vertexPositionAttribute, VERTICES_DIM,
			WebGLRenderingContext::FLOAT, false, 0, 0)
		glContext.vertexAttribPointer(shaderObject.textureCoordAttribute, TEXTURECOORDS_DIM,
			WebGLRenderingContext::FLOAT, false, 0, texCoordsOffset)
		glContext.vertexAttribPointer(shaderObject.vertexColorAttribute, COLORS_DIM, WebGLRenderingContext::FLOAT,
			false, 0, colorsOffset)
		glContext.vertexAttribPointer(shaderObject.vertexNormalAttribute, NORMALS_DIM, WebGLRenderingContext::FLOAT,
			false, 0, normalsOffset)
		glContext.vertexAttribPointer(shaderObject.newVertexPositionAttribute, VERTICES_DIM,
			WebGLRenderingContext::FLOAT, false, 0, newVerticesOffset)
	}

	def static void refillColors() {
		val colorsOffset = vertices.getByteLength() + textureCoords.getByteLength()

		if (glContext != null) {
			glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, colorsOffset, colors)
		}
	}

	def static final void drawTriangle(int offsetInBuffer, WebGLTexture texture, boolean transparent) {
		if (transparent != wasLastTransparent) {
			if (transparent) {
				glContext.disable(WebGLRenderingContext::DEPTH_TEST)

				glContext.enable(WebGLRenderingContext::BLEND)
				glContext.blendFunc(WebGLRenderingContext::SRC_ALPHA, WebGLRenderingContext::ONE_MINUS_SRC_ALPHA)
			} else {
				glContext.disable(WebGLRenderingContext::BLEND)

				glContext.enable(WebGLRenderingContext::DEPTH_TEST)
				glContext.depthFunc(WebGLRenderingContext::LESS)
			}

			wasLastTransparent = transparent
		}

		if (texture != null) {
			glContext.uniform1f(shaderObject.useTextureUniform, 1)

			glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, texture) // TODO do not rebind every drawing
		} else {
			glContext.uniform1f(shaderObject.useTextureUniform, 0)
		}

		// TODO draw all triangles in the scene at once!
		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, 3)
	}

	def static overrideColor(int offsetInBuffer, float[] newColor) {
		val ithComponent = offsetInBuffer

		val localColorsOffset = ithComponent * COLORS_DIM
		for (var i = 0; i < 3 * COLORS_DIM; i++) {
			FloatArray::set(colors, newColor.get(i % COLORS_DIM), localColorsOffset + i)
		}

		refillColors()
	}

	def static setNewVerticesPosition(int offsetInBuffer, float[] newPositions) {
		val ithComponent = offsetInBuffer

		val localNewVerticesOffset = ithComponent * VERTICES_DIM
		for (var i = 0; i < 3 * VERTICES_DIM; i++) {
			FloatArray::set(newVertices, newPositions.get(i), localNewVerticesOffset + i)
		}

	//        fillBuffer() // Dont fill buffer for each new vertices position
	}
}
