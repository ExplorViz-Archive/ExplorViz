package explorviz.visualization.engine.shaders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface Resources extends ClientBundle {
	public static Resources	INSTANCE	= GWT.create(Resources.class);
	
	@Source(value = { "vertex-shader.txt" })
	TextResource vertexShader();
	
	@Source(value = { "fragment-shader.txt" })
	TextResource fragmentShader();
}