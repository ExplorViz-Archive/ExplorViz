package explorviz.server.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

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
		final String title = jsonObj.getString("title");
		final Path experimentFolder = Paths.get(FULL_FOLDER + File.separator + title + ".json");
		final byte[] bytes = jsonObj.toString(4).getBytes(StandardCharsets.UTF_8);

		try {
			Files.write(experimentFolder, bytes, StandardOpenOption.CREATE_NEW);
		} catch (final java.nio.file.FileAlreadyExistsException e) {
			Files.write(experimentFolder, bytes, StandardOpenOption.TRUNCATE_EXISTING);
		}

	}

	@Override
	public List<String> getExperimentNames() {
		final List<String> names = new ArrayList<String>();

		final File directory = new File(FULL_FOLDER);

		final File[] fList = directory.listFiles();

		for (final File f : fList) {
			names.add(f.getName());
		}

		return names;
	}

	@Override
	public String getExperimentByName(final String name) {

		byte[] jsonBytes = null;
		try {
			jsonBytes = Files
					.readAllBytes(Paths.get(FULL_FOLDER + File.separator + name + ".json"));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);

		return jsonString;
	}

	@Override
	public void removeExperiment(final String name) {
		// delete object
	}

}
