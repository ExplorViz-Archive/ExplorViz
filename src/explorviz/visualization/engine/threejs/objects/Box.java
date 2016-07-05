package explorviz.visualization.engine.threejs.objects;

import explorviz.visualization.engine.math.Vector3f;

public class Box {

	public Vector3f center;
	public Vector3f extensionInEachDirection;

	public Box(final Vector3f center, final Vector3f extensionInEachDirection) {
		this.center = center;
		this.extensionInEachDirection = extensionInEachDirection;
	}

	public native void nativeGetter() /*-{
		var that = this;

		$wnd.getBoxCenter = function() {
			var xval = that.@explorviz.visualization.engine.threejs.objects.Box::center.x;
			var yval = that.@explorviz.visualization.engine.threejs.objects.Box::center.y;
			var zval = that.@explorviz.visualization.engine.threejs.objects.Box::center.z;

			var test = that.@explorviz.visualization.engine.threejs.objects.Box::center;

			console.log(test.x);

			var center = {
				x : xval,
				y : yval,
				z : zval
			};
			return center;
		};

	}-*/;

}
