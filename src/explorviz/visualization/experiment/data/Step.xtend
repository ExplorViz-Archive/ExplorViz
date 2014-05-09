package explorviz.visualization.experiment.data

class Step{
	int number
	String name
	String status
	
	new(int number, String name, String status){
		this.number = number
		this.name = name
		this.status = status
	}
	
	def getNumber(){
		number
	}
	
	def getName(){
		name
	}
	
	def getStatus(){
		status
	}
}