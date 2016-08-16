package explorviz.visualization.experiment.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JSONServiceAsync {

	void getJSON(AsyncCallback<String> callback);

	void sendJSON(String json, AsyncCallback<Void> callback);

	void getExperimentNames(AsyncCallback<List<String>> callback);

}
