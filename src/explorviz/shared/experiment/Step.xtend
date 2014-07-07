package explorviz.shared.experiment

import com.google.gwt.user.client.rpc.IsSerializable

class Step implements IsSerializable {
	@Property String source = ""
	@Property String dest = ""
	@Property boolean doubleClick = false
	@Property boolean rightClick = false
	@Property boolean leftClick = false
	@Property boolean hover = false
	@Property boolean connection = false
	@Property boolean backToLandscape = false
	@Property boolean timeshift = false
	@Property boolean requiresButton = false
	
	/**
	 * @param source the name of the component the connection starts at
	 * @param dest name of the component the connection ends at
	 * @param rightClick to complete the step, rightClick the communication
	 * @param leftClick to complete the step, leftClick the communication
	 */
	new(String source, String dest, boolean rightClick, boolean leftClick){
		this.source = source
		this.dest = dest
		this.rightClick = rightClick
		this.leftClick = leftClick
		this.connection = true
	}
	
		/**
	 * @param source name of the component
	 * @param doubleClick to complete the step, doubleclick the component
	 * @param rightClick to complete the step, rightClick the component
	 * @param leftClick to complete the step, leftClick the component
	 * @param hover to complete the step, hover over the component
	 */
	new(String source, boolean doubleClick, boolean rightClick, boolean leftClick, boolean hover){
		this.source = source
		this.doubleClick = doubleClick
		this.rightClick = rightClick
		this.leftClick = leftClick
		this.hover = hover
	}
	
	/**
	 * creates a tutorial step that doesn't react to interaction but provides a "continue" button
	 * @param source name of the component the communication starts at
	 * @param dest name of the component the communication ends at
	 */
	new(String source, String dest){
		this.source = source
		this.dest = dest
		this.connection = true
		this.requiresButton = true
	}
	
	/**
	 * creates a tutorial step that doesn't react to interaction but provides a "continue" button
	 * @param source name of the component - empty string for no arrow
	 */	
	new(String source){
		this.source = source
		this.requiresButton = true
	}

	new(){
		backToLandscape = false
	}
	
	
	new(boolean back){
		backToLandscape = back
		timeshift = !back
	}
		
}