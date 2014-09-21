package explorviz.visualization.experiment.callbacks

import com.google.gwt.user.client.rpc.AsyncCallback

/**
 * Callback to be used for void methods. Doesn't do anything.
 * @author Santje Finke
 */
class VoidCallback implements AsyncCallback<Void> {
	
	override onFailure(Throwable caught) {
		//Does nothing
	}
	
	override onSuccess(Void result) {
		//Does nothing
	}
	
}