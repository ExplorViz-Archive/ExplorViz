package explorviz.visualization.layout.datastructures.hypergraph

import explorviz.visualization.engine.math.Vector3f

class Vector3fNode extends Vector3f {
	
	new (float x, float y, float z) {
		super(x,y,z)
	}
	
	new(Vector3f vector) {
		super(vector.x, vector.y, vector.z)
	}
	
	override boolean equals(Object other) {
		if (other instanceof Vector3f) {
			var Vector3f otherVector = other as Vector3f;
			return (checkFloatEquals(x, otherVector.x) && checkFloatEquals(z, otherVector.z));
		}
		return false;
	}

	def private boolean checkFloatEquals(float first,float second) {
		return Math.abs(first - second) < 0.0001f;
	}

	override int hashCode() {
		val int prime = 31;
		var int result = 1;
		result = (prime * result) + Float.floatToIntBits(x) + Float.floatToIntBits(z);
		return result;
	}
}