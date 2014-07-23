package explorviz.shared.model.helper

class Bounds {
	@Property transient float width
	@Property transient float height
	@Property transient float depth

	@Property transient float positionX
	@Property transient float positionY
	@Property transient float positionZ
	
	new(float positionX, float positionZ, float width, float depth) {
		this.width = width
		this.depth = depth
		this.positionX = positionX
		this.positionZ = positionZ
	}
	
	new(float width, float depth) {
		this.width = width
		this.depth = depth
	}
}