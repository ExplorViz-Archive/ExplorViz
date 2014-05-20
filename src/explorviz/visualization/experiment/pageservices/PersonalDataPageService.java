package explorviz.visualization.experiment.pageservices;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("personaldatapage")
public interface PersonalDataPageService extends RemoteService {
	public String getPage();
}