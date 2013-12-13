package explorviz.visualization.engine.shaders;

import elemental.html.WebGLUniformLocation;

public class ShaderObject {
	private final int					vertexPositionAttribute;
	private final int					textureCoordAttribute;
	private final int					vertexColorAttribute;
	private final int					vertexNormalAttribute;
	private final int					newVertexPositionAttribute;
	
	private final WebGLUniformLocation	timePassedInPercentUniform;
	private final WebGLUniformLocation	useLightingUniform;
	private final WebGLUniformLocation	textureUniform;
	private final WebGLUniformLocation	useTextureUniform;
	
	public ShaderObject(int vertexPositionAttribute, int textureCoordAttribute, int vertexColorAttribute,
			int vertexNormalAttribute, int newVertexPositionAttribute, WebGLUniformLocation timePassedInPercentUniform,
			WebGLUniformLocation useLightingUniform, WebGLUniformLocation textureUniform,
			WebGLUniformLocation useTextureUniform) {
		this.vertexPositionAttribute = vertexPositionAttribute;
		this.textureCoordAttribute = textureCoordAttribute;
		this.vertexColorAttribute = vertexColorAttribute;
		this.vertexNormalAttribute = vertexNormalAttribute;
		this.newVertexPositionAttribute = newVertexPositionAttribute;
		
		this.timePassedInPercentUniform = timePassedInPercentUniform;
		this.useLightingUniform = useLightingUniform;
		this.textureUniform = textureUniform;
		this.useTextureUniform = useTextureUniform;
	}
	
	public int getVertexPositionAttribute() {
		return vertexPositionAttribute;
	}
	
	public int getTextureCoordAttribute() {
		return textureCoordAttribute;
	}
	
	public int getVertexColorAttribute() {
		return vertexColorAttribute;
	}
	
	public int getVertexNormalAttribute() {
		return vertexNormalAttribute;
	}
	
	public int getNewVertexPositionAttribute() {
		return newVertexPositionAttribute;
	}
	
	public WebGLUniformLocation getTextureUniform() {
		return textureUniform;
	}
	
	public WebGLUniformLocation getUseTextureUniform() {
		return useTextureUniform;
	}
	
	public WebGLUniformLocation getTimePassedInPercentUniform() {
		return timePassedInPercentUniform;
	}
	
	public WebGLUniformLocation getUseLightingUniform() {
		return useLightingUniform;
	}
}
