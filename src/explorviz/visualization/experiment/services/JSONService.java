package explorviz.visualization.experiment.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.experiment.*;

@RemoteServiceRelativePath("jsonservice")
public interface JSONService extends RemoteService {

	public void saveJSONOnServer(String json) throws IOException;

	public List<String> getExperimentFilenames();

	public String getExperiment(String name) throws IOException;

	public void removeExperiment(String name) throws IOException;

	public String getExperimentDetails(String title) throws IOException;

	public void duplicateExperiment(String json) throws IOException;

	public String downloadExperimentData(String filename) throws IOException;

	public List<String> getExperimentTitles() throws IOException;

	public String getExperimentTitlesAndFilenames() throws IOException;

	public String getQuestionnaireDetails(String filename) throws IOException;

	public void removeQuestionnaire(String data) throws IOException;

	public String getQuestionnaire(String data) throws IOException;

	public void saveQuestionnaireServer(String data) throws IOException;

	public String createUsersForQuestionnaire(int count, String prefix, String filename);

	public String getExperimentAndUsers(String data) throws IOException;

	public String removeQuestionnaireUser(String username) throws IOException;

	public ArrayList<Prequestion> getQuestionnairePrequestionsForUser(String filename,
			String userName) throws IOException;

	public Question[] getQuestionnaireQuestionsForUser(String filename, String userName)
			throws IOException;

	public ArrayList<Postquestion> getQuestionnairePostquestionsForUser(String filename,
			String userName) throws IOException;

	public boolean uploadExperiment(String jsonExperimentFile) throws IOException;

	public String isExperimentReadyToStart(String filename) throws IOException;

	public Boolean isUserInCurrentExperiment(String username) throws IOException;

	public boolean uploadLandscape(String data) throws IOException;

	public String getExperimentTitle(String filename) throws IOException;

	public void setExperimentTimeAttr(String filename, boolean isLastStarted) throws IOException;

	public boolean getQuestionnairePreAndPostquestions(final String filename, final String userName,
			final String questionnaireID) throws IOException;

	public boolean getQuestionnaireEyeTracking(final String filename, final String userName,
			final String questionnaireID) throws IOException;

	public boolean getQuestionnaireRecordScreen(final String filename, final String userName,
			final String questionnaireID) throws IOException;

	public void setQuestionnairePreAndPostquestions(final String filename,
			final String questionnaireID, final boolean preAndPostquestions) throws IOException;

	public void setQuestionnaireEyeTracking(final String filename, final String questionnaireID,
			final boolean eyeTracking) throws IOException;

	public void setQuestionnaireRecordScreen(final String filename, final String questionnaireID,
			final boolean recordScreen) throws IOException;

	public boolean uploadEyeTrackingData(final String experimentName, final String userID,
			final String eyeTrackingData) throws IOException;

	public String getEyeTrackingData(final String filename, final String userID);

	public String getScreenRecordData(final String experimentName, final String questionnaireID);

	public String getQuestionnairePrefix(final String username);

	public String downloadDataOfUser(final String experimentFilename, final String userID)
			throws IOException;
	/*
	 * public boolean uploadScreenRecording(final String experimentName, final
	 * String userID, final String base64EncodedScreenRecordingData);
	 */

	public void removeLocalVideoData() throws IOException;

	public boolean existsFileInsideAnswerFolder(final String filename);

	public String existsFilesForAllUsers(final String questionnairePrefix, final String path);

}
