package explorviz.visualization.renderer

import explorviz.visualization.engine.math.Vector4f

class ColorDefinitions {
	public val static pipeColor = new Vector4f(1f,0.596078f,0.11372549f,1f)
	
	public val static nodeGroupPlusColor = new Vector4f(0.08235f, 0.6f, 0.16470588f, 1f)
	public val static nodeGroupBackgroundColor = new Vector4f(0.08235f, 0.6f, 0.16470588f, 1f)
	
	public val static nodeForegroundColor = new Vector4f(1f, 1f, 1f, 1f)
	public val static nodeBackgroundColor = new Vector4f(0f, 0.7568627f, 0.247058f, 1f)
	
	public val static applicationForegroundColor = new Vector4f(1f, 1f, 1f, 1f)
	public val static applicationBackgroundColor = new Vector4f(0.2745098f, 0.090196f, 0.705882f, 1f)
	// vorschlag1: new Vector4f(0.145098039f, 0.4470588f, 0.9215686274f, 1f)
	// vorschlag2: new Vector4f(0.2745098f, 0.090196f, 0.705882f, 1f)
}