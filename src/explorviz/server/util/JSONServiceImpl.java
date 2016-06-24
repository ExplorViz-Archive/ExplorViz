package explorviz.server.util;

import java.io.IOException;

import org.json.JSONObject;
import org.json.XML;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.services.JSONService;

public class JSONServiceImpl extends RemoteServiceServlet implements JSONService {

	private static final long serialVersionUID = 6576514774419481521L;

	@Override
	public String getJSON() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendJSON(final String json) throws IOException {
		Logging.log(json);
		final JSONObject jsonObj = new JSONObject(json);
		Logging.log(jsonObj.toString());
		final String xml = XML.toString(jsonObj);
		Logging.log(xml);
	}

}
