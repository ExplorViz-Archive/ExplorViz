package explorviz.shared.experiment

import java.io.Serializable

class Step implements Serializable{
	String source
	String dest
	boolean doubleClick
	boolean rightClick
	boolean leftClick
	boolean connection
	boolean backToLandscape = false
	
	/**
	 * @param source the name of the component the connection starts at
	 * @param dest name of the component the connection ends at
	 * @param doubleClick to complete the step, doubleclick the component
	 * @param rightClick to complete the step, rightClick the component
	 * @param leftClick to complete the step, leftClick the component
	 */
	new(String source, String dest, boolean doubleClick, boolean rightClick, 
		boolean leftClick){
		this.source = source
		this.dest = dest
		this.doubleClick = doubleClick
		this.rightClick = rightClick
		this.leftClick = leftClick
		this.connection = true
	}
	
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
		this.connection = false
	}
	
	new(){
		backToLandscape = false
	}
	
	
	new(boolean back){
		backToLandscape = true
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
}