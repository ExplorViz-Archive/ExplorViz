package explorviz.server.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import org.json.JSONObject;
import org.json.XML;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.main.FileSystemHelper;
import explorviz.visualization.experiment.services.JSONService;

public class JSONServiceImpl extends RemoteServiceServlet implements JSONService {

	private static final long serialVersionUID = 6576514774419481521L;

	private static String FULL_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ "experiment";

	@Override
	public String getJSON() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendJSON(final String json) throws IOException {
		final JSONObject jsonObj = new JSONObject(json);
		final String xml = XML.toString(jsonObj);
		// TODO pretty output
		final Path experimentFolder = Paths.get(FULL_FOLDER + File.separator + "experiment.xml");
		final byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
		Files.write(experimentFolder, bytes, StandardOpenOption.CREATE);
	}

}
