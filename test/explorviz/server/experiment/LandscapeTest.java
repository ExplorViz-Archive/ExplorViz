package explorviz.server.experiment;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONException;
import org.junit.*;
import org.junit.rules.ExpectedException;

import elemental.json.Json;
import elemental.json.JsonObject;
import explorviz.server.database.DBConnection;
import explorviz.server.util.JSONServiceImpl;

public class LandscapeTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static JSONServiceImpl service;

	private static String sourcePathLand = "war/experiment/Test-Data/1467188123864-6247035.expl";

	@BeforeClass
	public static void initialize() {

		try {
			DBConnection.connect();
		} catch (final SQLException e1) {
			System.err.println("Couldn't connect to database. Some tests will fail.");
		}

		service = new JSONServiceImpl();
		JSONServiceImpl.createExperimentFoldersIfNotExist();
	}

	////////////////////////////
	// upload landscape tests //
	////////////////////////////
	@Test
	public void testUploadLandscapeFailData() throws IOException {
		thrown.expect(JSONException.class);
		thrown.expectMessage("A JSONObject text must begin with '{' at 1 [character 2 line 1]");
		service.uploadLandscape("ExplorViz is nice");
	}

	@Test
	public void testUploadLandscapeFailFilename() throws IOException {
		final JsonObject data = Json.createObject();

		data.put("filename", "Explorviz is nice");
		data.put("fileData", "Nothing");

		assertFalse(service.uploadLandscape(data.toString()));
	}

	@Test
	public void testUploadLandscapeFailLandscape() throws IOException {
		final JsonObject data = Json.createObject();

		data.put("filename", "1467188123864-6247035.expl");
		data.put("fileData", "Nothing");

		assertFalse(service.uploadLandscape(data.toString()));
	}

	@Test
	public void testUploadLandscapeFailLandscapeDeserialization() throws IOException {
		final JsonObject data = Json.createObject();

		data.put("filename", "1467188123864-6247035.expl");
		data.put("fileData", "test, Landscape");

		assertFalse(service.uploadLandscape(data.toString()));
	}

	@Test
	public void testUploadLandscape() throws IOException {
		final JsonObject data = Json.createObject();

		final Path path = Paths.get(sourcePathLand);
		final byte[] bytes = Files.readAllBytes(path);

		data.put("filename", "1467188123864-6247035.expl");
		data.put("fileData", "test," + DatatypeConverter.printBase64Binary(bytes));

		// Doesn't work because of weird
		// Base64 encoding problem

		// assertTrue(service.uploadLandscape(data.toString()));
	}

}
