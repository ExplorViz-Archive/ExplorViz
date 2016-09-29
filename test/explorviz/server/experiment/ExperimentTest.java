package explorviz.server.experiment;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import org.junit.*;

import explorviz.server.util.JSONServiceImpl;

public class ExperimentTest {

	private JSONServiceImpl service;
	private boolean deleteLandscapeFile = false;

	@Before
	public void initialize() {
		service = new JSONServiceImpl();
		JSONServiceImpl.createExperimentFoldersIfNotExist();
		if (!copyLandscape()) {
			fail("Couldn't copy Landscape");
		}
	}

	@After
	public void cleanup() {
		if (deleteLandscapeFile) {
			removeLandscape();
		}
	}

	// Tests

	@Test
	public void testGetExperimentFilenames() {

		// check if at least one landscape exists in folder
		final List<String> filenames = service.getExperimentFilenames();

		assertTrue("Filename list should be greater or equal zero", filenames.size() > 0);
	}

	// Helper

	private boolean copyLandscape() {
		deleteLandscapeFile = false;
		// copy one experiment from workspace to replay folder
		final Path relativePath = Paths.get("war/replay/1467188123864-6247035.expl");
		byte[] byteLandscape = null;
		try {
			byteLandscape = Files.readAllBytes(relativePath);
		} catch (final IOException e) {
			System.err.println("Couldn't read landscape from workspace. Exception: " + e);
			return false;
		}

		final Path experimentFolder = Paths.get(
				JSONServiceImpl.LANDSCAPE_FOLDER + File.separator + "1467188123864-6247035.expl");

		if (byteLandscape == null) {
			return false;
		}

		try {
			Files.write(experimentFolder, byteLandscape, StandardOpenOption.CREATE_NEW);
		} catch (final FileAlreadyExistsException e) {
			deleteLandscapeFile = false;
			return true;
		} catch (final IOException e) {
			System.err.println("Couldn't write landscape to replay folder. Exception: " + e);
			return false;
		}
		deleteLandscapeFile = true;
		return true;
	}

	private void removeLandscape() {

		try {
			final Path landscapePath = Paths.get(JSONServiceImpl.LANDSCAPE_FOLDER + File.separator
					+ "1467188123864-6247035.expl");

			Files.delete(landscapePath);

		} catch (final IOException e) {
			System.err.println(
					"Couldn't delete file 1467188123864-6247035.expl in war/replay folder. Exception: "
							+ e);
		}
	}

}
