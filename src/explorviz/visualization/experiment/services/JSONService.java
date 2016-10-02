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

	public String getExperiment(String name);

	public void removeExperiment(String name);

	public String getExperimentDetails(String title);

	public void duplicateExperiment(String json) throws IOException;

	public String downloadExperimentData(String filename) throws IOException;

	public List<String> getExperimentTitles();

	public String getExperimentTitlesAndFilenames();

	public String getQuestionnaireDetails(String filename);

	public void removeQuestionnaire(String data);

	public String getQuestionnaire(String data);

	public void saveQuestionnaireServer(String data) throws IOException;

	public String createUsersForQuestionnaire(int count, String prefix);

	public String getExperimentAndUsers(String data);

	public String removeQuestionnaireUser(String username);

	public Question[] getQuestionnaireQuestionsForUser(String filename, String userName);

	public boolean uploadExperiment(String jsonExperimentFile) throws IOException;

	public String isExperimentReadyToStart(String filename);

	public Boolean isUserInCurrentExperiment(String username);

	public boolean uploadLandscape(String data) throws IOException;

	public String getExperimentTitle(String filename);

	public void setExperimentTimeAttr(String filename, boolean isLastStarted);

}
