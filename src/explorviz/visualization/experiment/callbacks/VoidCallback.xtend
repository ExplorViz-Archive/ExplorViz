package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback

class VoidCallback implements AsyncCallback<Void> {
	
	override onFailure(Throwable caught) {
		//Does nothing
	}
	
	override onSuccess(Void result) {
		//Does nothing
	}
	
}