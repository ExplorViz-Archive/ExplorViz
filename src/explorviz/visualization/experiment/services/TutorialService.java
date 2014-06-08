package explorviz.visualization.experiment.services;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.experiment.Step;

@RemoteServiceRelativePath("tutorialservice")
public interface TutorialService extends RemoteService {
	public String getText(int number) throws IOException;

	public String getLanguage();

	public String[] getLanugages();

	Step[] getSteps();
}
