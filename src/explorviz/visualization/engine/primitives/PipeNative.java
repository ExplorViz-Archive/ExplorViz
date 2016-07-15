package explorviz.visualization.engine.primitives;

import com.google.gwt.core.client.JavaScriptObject;

public class PipeNative {

	public static native JavaScriptObject getLineThickness(Pipe pipe)
	/*-{
		var thickness = pipe.@explorviz.visualization.engine.primitives.Pipe::lineThickness;

		return thickness;
	}-*/;

	public static native JavaScriptObject getColor(Pipe pipe) /*-{
		var colorVector = pipe.@explorviz.visualization.engine.primitives.Pipe::color;
		var color = colorVector.@explorviz.visualization.engine.math.Vector4f::getVector()();

		return color;
	}-*/;

}
