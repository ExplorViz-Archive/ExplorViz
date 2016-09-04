package explorviz.visualization.experiment.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.experiment.Question;

public interface JSONServiceAsync {

	void saveJSONOnServer(String json, AsyncCallback<Void> callback);

	void getExperimentFilenames(AsyncCallback<List<String>> callback);

	void getExperiment(String name, AsyncCallback<String> callback);

	void removeExperiment(String name, AsyncCallback<Void> callback);

	void getQuestionsOfExp(String name, AsyncCallback<Question[]> callback);

	void getExperimentDetails(String title, AsyncCallback<String> callback);

	void duplicateExperiment(String json, AsyncCallback<Void> callback);

	void downloadExperimentData(String filename, AsyncCallback<String> callback);

	void getExperimentTitles(AsyncCallback<List<String>> callback);

	void getExperimentTitlesAndFilenames(AsyncCallback<String> callback);

	void getQuestionnaireDetails(String filename, AsyncCallback<String> callback);

	void removeQuestionnaire(String data, AsyncCallback<Void> callback);

}
