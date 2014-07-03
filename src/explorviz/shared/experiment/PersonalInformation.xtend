package explorviz.shared.experiment

import com.google.gwt.user.client.rpc.IsSerializable

class PersonalInformation implements IsSerializable {
	var userID = ""
	var gender = ""
	var experience = ""
	var age = 0
	var degree = ""
	
	new(String information, String id){
		this.userID = id
		//parse answers: =age& =gender& =experience& =abschluss
	}
	
	def toCSV(){
		userID+","+age+","+gender+","+experience+","+degree+"\n"
	}
}