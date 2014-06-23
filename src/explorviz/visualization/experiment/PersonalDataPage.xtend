package explorviz.visualization.experiment

import explorviz.visualization.view.IPage
import explorviz.visualization.main.PageControl
import explorviz.visualization.engine.navigation.Navigation

class PersonalDataPage implements IPage {
	
	override render(PageControl pageControl) {
		var htmlResult = '''<form id="questionForm" role="form" style="width:90%">
			  <div class="form-group">
			  
			    <label for="ageForm">Gender</label>
			    <div class="input-group" id="ageForm">
					<input type="text" class="form-control" placeholder="Age">
					<span class="input-group-addon">Years</span>
			    </div>
			
				<div class="form-group">
					<label for="genderForm">Gender</label>
					<select class="form-control" id="genderForm">
					  <option>Male</option>
					  <option>Female</option>
					</select>
			    </div>
			
				<div class="form-group">
				    <label for="experienceForm">Experience with ExplorViz</label>
					<select class="form-control" id="experienceForm">
						<option>none</option>
						<option>seen it before</option>
						<option>used it a few times</option>
						<option>know how it works</option>
					</select>
			    </div>
			    
			  </div>  
			  <button type="submit" class="btn btn-default">Submit</button>  
			</form>'''.toString()
		
		pageControl.setView(htmlResult)
		
		Navigation::deregisterWebGLKeys()
	}
	
}