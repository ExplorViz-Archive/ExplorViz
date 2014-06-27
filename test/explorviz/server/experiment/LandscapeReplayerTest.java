package explorviz.server.experiment;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import explorviz.shared.model.Landscape;

public class LandscapeReplayerTest {

	@Test
	public void testGetCurrentLandscape() {
		final LandscapeReplayer replayer = new LandscapeReplayer();
		LandscapeReplayer.FULL_FOLDER = "test" + File.separator + "replay";

		Landscape landscape = replayer.getCurrentLandscape();
		assertNull(landscape);

		replayer.setMaxTimestamp(1403854844524L);
		landscape = replayer.getCurrentLandscape();
		assertEquals(1, landscape.getSystems().size());
		assertEquals(904763910857L, landscape.getHash());

		// same step
		landscape = replayer.getCurrentLandscape();
		assertEquals(1, landscape.getSystems().size());
		assertEquals(904763910857L, landscape.getHash());

		// next step
		replayer.setMaxTimestamp(1403854859525L);
		landscape = replayer.getCurrentLandscape();
		assertEquals(1, landscape.getSystems().size());
		assertEquals(908685040960L, landscape.getHash());

		// two steps
		replayer.setMaxTimestamp(1403854889526L);
		landscape = replayer.getCurrentLandscape();
		assertEquals(1, landscape.getSystems().size());
		assertEquals(923685357888L, landscape.getHash());
		landscape = replayer.getCurrentLandscape();
		assertEquals(1, landscape.getSystems().size());
		assertEquals(952749284962L, landscape.getHash());
		landscape = replayer.getCurrentLandscape();
		assertEquals(1, landscape.getSystems().size());
		assertEquals(952749284962L, landscape.getHash());

		// definitely over the available max
		replayer.setMaxTimestamp(1403854934526L);
		for (int i = 0; i < 10; i++) {
			landscape = replayer.getCurrentLandscape();
		}
		assertEquals(1, landscape.getSystems().size());
		assertEquals(983696717209L, landscape.getHash());
	}
}
