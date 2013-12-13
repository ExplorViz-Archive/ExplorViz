package explorviz.visualization.engine.shaders;

import elemental.html.*;

public class ShaderInitializer {
	private static WebGLProgram				shaderProgram;
	private static WebGLRenderingContext	glContext;
	
	public static WebGLProgram getShaderProgram() {
		return shaderProgram;
	}
	
	public static void setShaderProgram(WebGLProgram shaderProgram) {
		ShaderInitializer.shaderProgram = shaderProgram;
	};
	
	public static ShaderObject initShaders(WebGLRenderingContext glContext) {
		ShaderInitializer.glContext = glContext;
		
		final WebGLShader fragmentShader = getShader(WebGLRenderingContext.FRAGMENT_SHADER, Resources.INSTANCE
				.fragmentShader().getText());
		final WebGLShader vertexShader = getShader(WebGLRenderingContext.VERTEX_SHADER, Resources.INSTANCE
				.vertexShader().getText());
		
		setShaderProgram(glContext.createProgram());
		glContext.attachShader(getShaderProgram(), vertexShader);
		glContext.attachShader(getShaderProgram(), fragmentShader);
		glContext.linkProgram(getShaderProgram());
		
		if (!isLinkShaderOkay(glContext, shaderProgram)) {
			throw new RuntimeException(glContext.getProgramInfoLog(shaderProgram));
		}
		
		glContext.useProgram(getShaderProgram());
		
		final int vertexPositionAttribute = glContext.getAttribLocation(getShaderProgram(), "vertexPosition");
		glContext.enableVertexAttribArray(vertexPositionAttribute);
		
		final int textureCoordAttribute = glContext.getAttribLocation(getShaderProgram(), "texPosition");
		glContext.enableVertexAttribArray(textureCoordAttribute);
		
		final int vertexColorAttribute = glContext.getAttribLocation(getShaderProgram(), "vertexColor");
		glContext.enableVertexAttribArray(vertexColorAttribute);
		
		final int vertexNormalAttribute = glContext.getAttribLocation(getShaderProgram(), "vertexNormal");
		glContext.enableVertexAttribArray(vertexNormalAttribute);
		
		final int newVertexPositionAttribute = glContext.getAttribLocation(getShaderProgram(), "newVertexPosition");
		glContext.enableVertexAttribArray(newVertexPositionAttribute);
		
		final WebGLUniformLocation timePassedInPercentUniform = glContext.getUniformLocation(getShaderProgram(),
				"timePassedInPercent");
		
		final WebGLUniformLocation useLightingUniform = glContext.getUniformLocation(getShaderProgram(), "useLighting");
		glContext.uniform1f(useLightingUniform, 0);
		
		final WebGLUniformLocation textureUniform = glContext.getUniformLocation(getShaderProgram(), "tex");
		
		final WebGLUniformLocation useTextureUniform = glContext.getUniformLocation(getShaderProgram(), "uUseTexture");
		
		return new ShaderObject(vertexPositionAttribute, textureCoordAttribute, vertexColorAttribute,
				vertexNormalAttribute, newVertexPositionAttribute, timePassedInPercentUniform, useLightingUniform,
				textureUniform, useTextureUniform);
	}
	
	private native static boolean isLinkShaderOkay(WebGLRenderingContext glContext, WebGLProgram shaderProgram) /*-{
		return glContext.getProgramParameter(shaderProgram,
				glContext.LINK_STATUS);
	}-*/;
	
	private static WebGLShader getShader(int type, String source) {
		final WebGLShader shader = glContext.createShader(type);
		
		glContext.shaderSource(shader, source);
		glContext.compileShader(shader);
		
		if (!isCompileShaderOkay(glContext, shader)) {
			throw new RuntimeException(glContext.getShaderInfoLog(shader));
		}
		
		return shader;
	}
	
	private native static boolean isCompileShaderOkay(WebGLRenderingContext glContext, WebGLShader shader) /*-{
		return glContext.getShaderParameter(shader, glContext.COMPILE_STATUS);
	}-*/;
}
