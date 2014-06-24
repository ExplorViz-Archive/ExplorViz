package explorviz.shared.experiment

import com.google.gwt.user.client.rpc.IsSerializable

class PersonalInformation implements IsSerializable {
	var userID = ""
	var gender = ""
	var experience = ""
	var age = 0
	
	new(String information){
		//parse information
	}
}