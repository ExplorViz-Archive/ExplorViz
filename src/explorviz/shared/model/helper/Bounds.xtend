package explorviz.shared.model.helper

class Bounds {
	@Property transient float width
	@Property transient float height
	@Property transient float depth

	@Property transient float positionX
	@Property transient float positionY
	@Property transient float positionZ
	
	new(float width, float height, float positionX, float positionY) {
		this.width = width
		this.height = height
		this.positionX = positionX
		this.positionY = positionY
	}
	
	new(float width, float height) {
		this.width = width
		this.height = height
	}
}