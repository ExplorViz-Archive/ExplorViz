package explorviz.visualization.experiment.pageservices;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("questionpage")
public interface QuestionPageService extends RemoteService {
	public String getPage();
}