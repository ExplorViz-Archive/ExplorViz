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

	public Question[] getQuestionsOfExp(String name);

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

}
