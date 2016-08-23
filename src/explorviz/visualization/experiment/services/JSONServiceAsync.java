package explorviz.visualization.experiment.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.experiment.Question;

public interface JSONServiceAsync {

	void getJSON(AsyncCallback<String> callback);

	void sendJSON(String json, AsyncCallback<Void> callback);

	void getExperimentNames(AsyncCallback<List<String>> callback);

	void getExperimentByName(String name, AsyncCallback<String> callback);

	void removeExperiment(String name, AsyncCallback<Void> callback);

	void getQuestionsOfExp(String name, AsyncCallback<Question[]> callback);

	void getExperimentDetails(String title, AsyncCallback<String> callback);

	void duplicateExperiment(String json, AsyncCallback<Void> callback);

	void downloadExperimentData(String filename, AsyncCallback<String> callback);
}
