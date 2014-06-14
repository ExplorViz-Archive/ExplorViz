package explorviz.shared.experiment

import com.google.gwt.user.client.rpc.IsSerializable

class Step implements IsSerializable{
	String source
	String dest
	boolean doubleClick 
	boolean rightClick
	boolean leftClick
	boolean connection
	boolean backToLandscape
	boolean timeshift
	boolean requiresButton
	
	/**
	 * @param source the name of the component the connection starts at
	 * @param dest name of the component the connection ends at
	 * @param doubleClick to complete the step, doubleclick the component
	 * @param rightClick to complete the step, rightClick the component
	 * @param leftClick to complete the step, leftClick the component
	 */
//	new(String source, String dest, boolean doubleClick, boolean rightClick, 
//		boolean leftClick){
//		this.source = source
//		this.dest = dest
//		this.doubleClick = doubleClick
//		this.rightClick = rightClick
//		this.leftClick = leftClick
//		this.connection = true
//	}
	
	/**
	 * @param source name of the component
	 * @param doubleClick to complete the step, doubleclick the component
	 * @param rightClick to complete the step, rightClick the component
	 * @param leftClick to complete the step, leftClick the component
	 */
	new(String source, boolean doubleClick, boolean rightClick, boolean leftClick){
		this.source = source
		this.dest = dest
		this.doubleClick = doubleClick
		this.rightClick = rightClick
		this.leftClick = leftClick
	}
	
	/**
	 * creates a tutorial step that doesn't react to interaction but provides a "continue" button
	 * @param source name of the component the communication starts at
	 * @param dest name of the component the communication ends at
	 */
//	new(String source, String dest){
//		this.source = source
//		this.dest = dest
//		this.connection = true
//		this.requiresButton = true
//	}
	/**
	 * creates a tutorial step that doesn't react to interaction but provides a "continue" button
	 * @param source name of the component - empty string for no arrow
	 */	
	new(String source){
		this.source = source
		this.requiresButton = true
	}
	
	new(){
		//empty contructor for serialization
	}
	
	
	new(boolean back){
		backToLandscape = back
		timeshift = !back
	}
	
	def getSource(){
		source
	}
	
	def isLeftClick(){
		leftClick
	}
	
	def isRightClick(){
		rightClick
	}
	
	def isDoubleClick(){
		doubleClick
	}
	
	def getDest(){
		dest
	}
	
	def isConnection(){
		connection
	}
	
	def isBackToLandscape(){
		backToLandscape
	}
	
	def isRequiresButton(){
		requiresButton
	}
	
	def isTimeshift(){
		timeshift
	}
	
}