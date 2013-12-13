package explorviz.visualization.engine.picking

class ClickEvent {
	@Property float positionX
	@Property float positionY
	
    @Property int originalClickX
    @Property int originalClickY
	
	@Property EventObserver object
}