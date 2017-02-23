package explorviz.visualization.experiment.services;

import java.util.ArrayList;
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
			AsyncCallback<ArrayList<Prequestion>> callback);

	void getQuestionnaireQuestionsForUser(String filename, String userName,
			AsyncCallback<Question[]> callback);

	void getQuestionnairePostquestionsForUser(String filename, String userName,
			AsyncCallback<ArrayList<Postquestion>> callback);

	void uploadExperiment(String jsonExperimentFile, AsyncCallback<Boolean> callback);

	void isExperimentReadyToStart(String filename, AsyncCallback<String> callback);

	void isUserInCurrentExperiment(String username, AsyncCallback<Boolean> callback);

	void uploadLandscape(String data, AsyncCallback<Boolean> callback);

	void getExperimentTitle(String filename, AsyncCallback<String> callback);

	void setExperimentTimeAttr(String filename, boolean isLastStarted,
			AsyncCallback<Void> callback);

	void getQuestionnairePreAndPostquestions(final String filename, final String userName,
			final String questionnaireID, AsyncCallback<Boolean> callback);

	void getQuestionnaireEyeTracking(final String filename, final String userName,
			final String questionnaireID, AsyncCallback<Boolean> callback);

	void getQuestionnaireRecordScreen(final String filename, final String userName,
			final String questionnaireID, AsyncCallback<Boolean> callback);

	void setQuestionnairePreAndPostquestions(final String filename, final String questionnaireID,
			boolean preAndPostquestions, AsyncCallback<Void> callback);

	void setQuestionnaireEyeTracking(final String filename, final String questionnaireID,
			boolean eyeTracking, AsyncCallback<Void> callback);

	void setQuestionnaireRecordScreen(final String filename, final String questionnaireID,
			boolean recordScreen, AsyncCallback<Void> callback);

	void uploadEyeTrackingData(final String experimentName, final String userID,
			final String eyeTrackingData, AsyncCallback<Boolean> callback);

	void getEyeTrackingData(final String filename, final String questionnaireID,
			AsyncCallback<String> callback);

	void getScreenRecordData(final String experimentName, final String questionnaireID,
			AsyncCallback<String> callback);

	void getQuestionnairePrefix(final String username, AsyncCallback<String> callback);

	void downloadDataOfUser(final String experimentFilename, final String userID,
			AsyncCallback<String> callback);

	void removeLocalVideoData(AsyncCallback<Void> callback);

}
