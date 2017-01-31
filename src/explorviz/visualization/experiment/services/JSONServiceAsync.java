package explorviz.visualization.experiment.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.experiment.*;

public interface JSONServiceAsync {

	void saveJSONOnServer(String json, AsyncCallback<Void> callback);

	void getExperimentFilenames(AsyncCallback<List<String>> callback);

	void getExperiment(String name, AsyncCallback<String> callback);

	void removeExperiment(String name, AsyncCallback<Void> callback);

	void getExperimentDetails(String title, AsyncCallback<String> callback);

	void duplicateExperiment(String json, AsyncCallback<Void> callback);

	void downloadExperimentData(String filename, AsyncCallback<String> callback);

	void getExperimentTitles(AsyncCallback<List<String>> callback);

	void getExperimentTitlesAndFilenames(AsyncCallback<String> callback);

	void getQuestionnaireDetails(String filename, AsyncCallback<String> callback);

	void removeQuestionnaire(String data, AsyncCallback<Void> callback);

	void getQuestionnaire(String data, AsyncCallback<String> callback);

	void saveQuestionnaireServer(String data, AsyncCallback<Void> callback);

	void createUsersForQuestionnaire(int count, String prefix, String filename,
			AsyncCallback<String> callback);

	void getExperimentAndUsers(String data, AsyncCallback<String> callback);

	void removeQuestionnaireUser(String username, AsyncCallback<String> callback);

	void getQuestionnairePrequestionsForUser(String filename, String userName,
			AsyncCallback<Prequestion[]> callback);

	void getQuestionnaireQuestionsForUser(String filename, String userName,
			AsyncCallback<Question[]> callback);

	void getQuestionnairePostquestionsForUser(String filename, String userName,
			AsyncCallback<Postquestion[]> callback);

	void uploadExperiment(String jsonExperimentFile, AsyncCallback<Boolean> callback);

	void isExperimentReadyToStart(String filename, AsyncCallback<String> callback);

	void isUserInCurrentExperiment(String username, AsyncCallback<Boolean> callback);

	void uploadLandscape(String data, AsyncCallback<Boolean> callback);

	void getExperimentTitle(String filename, AsyncCallback<String> callback);

	void setExperimentTimeAttr(String filename, boolean isLastStarted,
			AsyncCallback<Void> callback);

}
