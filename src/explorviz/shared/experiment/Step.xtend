package explorviz.shared.experiment

class Step{
	String source
	String dest
	boolean doubleClick
	boolean rightClick
	boolean leftClick
	boolean connection
	boolean backToLandscape = false
	
	/**
	 * @source name of the component or the name of the component the connection starts at
	 * @dest name of the component the connection ends at
	 * @doubleClick to complete the step, doubleclick the component
	 * @rightClick to complete the step, rightClick the component
	 * @leftClick to complete the step, leftClick the component
	 * @connection is it a connection or a component
	 */
	new(String source, String dest, boolean doubleClick, boolean rightClick, 
		boolean leftClick, boolean connection
	){
		this.source = source
		this.dest = dest
		this.doubleClick = doubleClick
		this.rightClick = rightClick
		this.leftClick = leftClick
		this.connection = connection
	}
	
	new(){
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