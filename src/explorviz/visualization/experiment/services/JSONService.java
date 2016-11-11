package explorviz.visualization.experiment.services;

import java.io.IOException;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.experiment.Question;

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

	public Question[] getQuestionnaireQuestionsForUser(String filename, String userName)
			throws IOException;

	public boolean uploadExperiment(String jsonExperimentFile) throws IOException;

	public String isExperimentReadyToStart(String filename) throws IOException;

	public Boolean isUserInCurrentExperiment(String username) throws IOException;

	public boolean uploadLandscape(String data) throws IOException;

	public String getExperimentTitle(String filename) throws IOException;

	public void setExperimentTimeAttr(String filename, boolean isLastStarted) throws IOException;

}
