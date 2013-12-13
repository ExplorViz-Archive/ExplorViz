package explorviz.visualization.engine;

import elemental.html.Float32Array;

public class FloatArray {
	private native static Float32Array createFloat32Array(int length, float... nums) /*-{
		return new Float32Array(nums, 0, length);
	}-*/;
	
	public static Float32Array create(int length, float... nums) {
		return createFloat32Array(length, nums);
	}
	
	public static Float32Array create(float... nums) {
		return create(nums.length, nums);
	}
}
