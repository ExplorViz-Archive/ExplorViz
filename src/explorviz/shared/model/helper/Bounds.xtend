package explorviz.shared.model.helper

import com.google.gwt.user.client.rpc.IsSerializable

class Bounds implements IsSerializable {
	@Property float width
	@Property float height
	@Property float depth
	@Property float positionX
	@Property float positionY
	@Property float positionZ

	new () {
		
	}
	
	new (float positionX, float positionZ, float width, float depth) {
		this.width = width
		this.depth = depth
		this.positionX = positionX
		this.positionZ = positionZ
		this.positionY = 0f
		this.height = 0f
	}
	
	new (float positionX, float positionY, float positionZ, float width, float height, float depth) {
		this.width = width
		this.depth = depth
		this.positionX = positionX
		this.positionZ = positionZ
		this.positionY = positionY
		this.height = height
	}
	
	new (float width, float depth) {
		this.width = width
		this.depth = depth
	}
	
	def boolean overlaps(Bounds childBounds) {
		return !(childBounds.positionX+childBounds.width < this.positionX || childBounds.positionX > this.positionX+this.width
		|| childBounds.positionZ+childBounds.depth < this.positionZ || childBounds.positionZ > this.positionZ+this.depth)
	}
}