package explorviz.visualization.engine.usertracking;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.visualization.main.ErrorDialog;

/**
 * @author Maria Kosche
 *
 */
public class UsertrackingRecordCallback<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(final Throwable caught) {
		ErrorDialog.showError(caught);
	}

	@Override
	public void onSuccess(final T result) {
	}

}
