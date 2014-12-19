package explorviz.visualization.engine.picking

import org.eclipse.xtend.lib.annotations.Accessors

class ClickEvent {
	@Accessors float positionX
	@Accessors float positionY
	
    @Accessors int originalClickX
    @Accessors int originalClickY
	
	@Accessors EventObserver object
}