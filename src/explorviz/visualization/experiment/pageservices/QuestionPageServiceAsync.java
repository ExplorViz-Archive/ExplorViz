package explorviz.visualization.experiment.pageservices;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface QuestionPageServiceAsync {

	void getPage(AsyncCallback<String> callback);

}
