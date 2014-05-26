package explorviz.shared.experiment

class Step{
	String source
	String dest
	boolean opened
	boolean connection
	
	/**
	 * @source name of the component or the name of the component the connection starts at
	 * @dest name of the component the connection ends at
	 * @opened which state should the component have to complete the step
	 * @connection is it a connection or a component
	 */
	new(String source, String dest, boolean opened, boolean connection){
		this.source = source
		this.dest = dest
		this.opened = opened
		this.connection = connection
	}
	
	def getSource(){
		source
	}
	
	def isOpened(){
		opened
	}
	
	def getDest(){
		dest
	}
	
	def isConnection(){
		connection
	}
}