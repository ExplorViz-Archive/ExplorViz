package explorviz.visualization.engine.usertracking;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UsertrackingRecordCallback<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(final Throwable caught) {
		// Window.alert("User tracking failure: " + caught.getMessage()
		// + " - Please contact administrator");

		// TODO test for eval
	}

	@Override
	public void onSuccess(final T result) {
	}

}
