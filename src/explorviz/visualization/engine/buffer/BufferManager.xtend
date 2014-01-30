package explorviz.visualization.engine.buffer

import elemental.html.WebGLRenderingContext
import elemental.html.WebGLBuffer
import elemental.html.WebGLTexture

import explorviz.visualization.engine.shaders.ShaderObject
import explorviz.visualization.engine.FloatArray

import static extension explorviz.visualization.main.ArrayExtensions.*

class BufferManager {
	static WebGLRenderingContext glContext
	static ShaderObject shaderObject
	static WebGLBuffer buffer

	static val int DEFAULT_BUFFER_POINT_LENGTH = 65536 * 8
	
	static val int VERTICES_DIM = 3
	static val int TEXTURECOORDS_DIM = 2
	static val int COLORS_DIM = 4
	static val int NORMALS_DIM = 3
	
	static float[] vertices
	static float[] textureCoords
	static float[] colors
	static float[] normals
	static float[] newVertices
	static int currentBufferItemCount = 0

	private new() {
	}

	def static init(WebGLRenderingContext glContextParam, ShaderObject attribsParam) {
		glContext = glContextParam
		shaderObject = attribsParam

		if (glContext != null) {
			buffer = glContext.createBuffer()
			glContext.bindBuffer(WebGLRenderingContext::ARRAY_BUFFER, buffer)
		}

		clear()
	}
	
    def static clear() {
        vertices = createFloatArray(DEFAULT_BUFFER_POINT_LENGTH * VERTICES_DIM)
        textureCoords = createFloatArray(DEFAULT_BUFFER_POINT_LENGTH * TEXTURECOORDS_DIM)
        colors = createFloatArray(DEFAULT_BUFFER_POINT_LENGTH * COLORS_DIM)
        normals = createFloatArray(DEFAULT_BUFFER_POINT_LENGTH * NORMALS_DIM)
        newVertices = createFloatArray(DEFAULT_BUFFER_POINT_LENGTH * VERTICES_DIM)
        
        currentBufferItemCount = 0
    }

	def static begin() {
	    clear()
	}

	def static int addTriangle(float[] verticesToAdd, float[] textureCoordsToAdd, float[] colorToAdd, float[] normalToAdd) {
	    val startOffset = currentBufferItemCount
	    
		vertices = mergeArray(verticesToAdd, vertices, currentBufferItemCount * VERTICES_DIM)
		textureCoords = mergeArray(textureCoordsToAdd, textureCoords, currentBufferItemCount * TEXTURECOORDS_DIM)
		
		colors = mergeArray(colorToAdd, colors, currentBufferItemCount * COLORS_DIM)
		colors = mergeArray(colorToAdd, colors, (currentBufferItemCount+1) * COLORS_DIM)
		colors = mergeArray(colorToAdd, colors, (currentBufferItemCount+2) * COLORS_DIM)
		
		normals = mergeArray(normalToAdd, normals, currentBufferItemCount * NORMALS_DIM)
		normals = mergeArray(normalToAdd, normals, (currentBufferItemCount+1) * NORMALS_DIM)
		normals = mergeArray(normalToAdd, normals, (currentBufferItemCount+2) * NORMALS_DIM)

		newVertices = mergeArray(verticesToAdd, newVertices, currentBufferItemCount * VERTICES_DIM)
		
        currentBufferItemCount = currentBufferItemCount + 3
        startOffset
	}

	def private static mergeArray(float[] valuesToAdd, float[] targetArray, int startOffset) {
		val lengthToAdd = valuesToAdd.length
		val currentMaxBufferSize = targetArray.length
		
		var currentTargetArray = targetArray
		
		if (currentMaxBufferSize <= startOffset + lengthToAdd) {
		    // increase buffer
		    // System::arraycopy cannot cope with such large puffer :(
		    setElement(currentTargetArray, currentMaxBufferSize * 2, 0f)
		}

		System::arraycopy(valuesToAdd, 0, currentTargetArray, startOffset, lengthToAdd)
		currentTargetArray
	}

	def static end() {
        fillBuffer()
	}
	
	def static fillBuffer() {
        val verticesJS = FloatArray::create(currentBufferItemCount * VERTICES_DIM, vertices)
        val texCoordsJS = FloatArray::create(currentBufferItemCount * TEXTURECOORDS_DIM, textureCoords)
        val colorsJS = FloatArray::create(currentBufferItemCount * COLORS_DIM, colors)
        val normalsJS = FloatArray::create(currentBufferItemCount * NORMALS_DIM, normals)
        val newVerticesJS = FloatArray::create(currentBufferItemCount * VERTICES_DIM, newVertices)

        val texCoordsOffset = verticesJS.getByteLength()
        val colorsOffset = texCoordsOffset + texCoordsJS.getByteLength()
        val normalsOffset = colorsOffset + colorsJS.getByteLength()
        val newVerticesOffset = normalsOffset + normalsJS.getByteLength()

        if (glContext != null) {
            glContext.bindBuffer(WebGLRenderingContext::ARRAY_BUFFER, buffer)
            
            glContext.bufferData(WebGLRenderingContext::ARRAY_BUFFER,
                verticesJS.getByteLength() + texCoordsJS.getByteLength() + colorsJS.getByteLength() + normalsJS.getByteLength() + newVerticesJS.getByteLength(), WebGLRenderingContext::STATIC_DRAW)
                
            glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, 0, verticesJS)
            glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, texCoordsOffset, texCoordsJS)
            glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, colorsOffset, colorsJS)
            glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, normalsOffset, normalsJS)
            glContext.bufferSubData(WebGLRenderingContext::ARRAY_BUFFER, newVerticesOffset, newVerticesJS)

            glContext.vertexAttribPointer(shaderObject.vertexPositionAttribute, VERTICES_DIM, WebGLRenderingContext::FLOAT,
                false, 0, 0)
            glContext.vertexAttribPointer(shaderObject.textureCoordAttribute, TEXTURECOORDS_DIM, WebGLRenderingContext::FLOAT, false,
                0, texCoordsOffset)
            glContext.vertexAttribPointer(shaderObject.vertexColorAttribute, COLORS_DIM, WebGLRenderingContext::FLOAT, false,
                0, colorsOffset)
            glContext.vertexAttribPointer(shaderObject.vertexNormalAttribute, NORMALS_DIM, WebGLRenderingContext::FLOAT, false,
                0, normalsOffset)
            glContext.vertexAttribPointer(shaderObject.newVertexPositionAttribute, VERTICES_DIM, WebGLRenderingContext::FLOAT, false,
                0, newVerticesOffset)
        }
	}

	def static void drawTriangle(int offsetInBuffer, WebGLTexture texture, boolean transparent) {
		if (glContext != null) {
			if (transparent) {
				glContext.disable(WebGLRenderingContext::DEPTH_TEST)
		
				glContext.enable(WebGLRenderingContext::BLEND)
				glContext.blendFunc(WebGLRenderingContext::SRC_ALPHA, WebGLRenderingContext::ONE_MINUS_SRC_ALPHA)
			} else {
				glContext.disable(WebGLRenderingContext::BLEND)
				
				glContext.enable(WebGLRenderingContext::DEPTH_TEST)
				glContext.depthFunc(WebGLRenderingContext::LEQUAL)
			}
			
		    if (texture != null) {
			  glContext.uniform1f(shaderObject.useTextureUniform, 1)
			  glContext.activeTexture(WebGLRenderingContext::TEXTURE0)

			  glContext.bindTexture(WebGLRenderingContext::TEXTURE_2D, texture) // TODO do not rebind every drawing
			} else {
			  glContext.uniform1f(shaderObject.useTextureUniform, 0)
			}
			// TODO draw all triangles in the scene at once!
    		glContext.drawArrays(WebGLRenderingContext::TRIANGLES, offsetInBuffer, 3)
		}
	}
	
	def static void drawAllTriangles() {
        if (glContext != null) {
            glContext.uniform1f(shaderObject.useTextureUniform, 0)
            glContext.drawArrays(WebGLRenderingContext::TRIANGLES, 0, currentBufferItemCount)
        }  
	}
	
	def static overrideColor(int offsetInBuffer, float[] newColor) {
		val ithComponent = offsetInBuffer
		
		val localColorsOffset = ithComponent * COLORS_DIM
		var i = 0
		while (i < 3 * COLORS_DIM) {
		  colors.set(localColorsOffset + i,newColor.get(i % COLORS_DIM))
		  i = i + 1
		}
		fillBuffer()
	}
	
    def static setNewVerticesPosition(int offsetInBuffer, float[] newPositions) {
        val ithComponent = offsetInBuffer
        
        val localNewVerticesOffset = ithComponent * VERTICES_DIM
        var i = 0
        while (i < 3 * VERTICES_DIM) {
          newVertices.set(localNewVerticesOffset + i,newPositions.get(i))
          i = i + 1
        }
        //fillBuffer() // Dont fill buffer for each new vertices position
    }
}
