package explorviz.visualization.experiment.services;

import java.io.IOException;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("jsonservice")
public interface JSONService extends RemoteService {

	public String getJSON() throws IOException;

	public void sendJSON(String json) throws IOException;

	public List<String> getExperimentNames();

}
