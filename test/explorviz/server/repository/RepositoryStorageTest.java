package explorviz.server.repository;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Test;

import explorviz.shared.model.Landscape;

public class RepositoryStorageTest {

	@Test
	public void testWriteToFile() throws Exception {
		RepositoryStorage.clearRepository();

		final Landscape landscape = new Landscape();
		landscape.setHash(100000);
		final long firstTime = java.lang.System.currentTimeMillis();
		RepositoryStorage.writeToFile(landscape, firstTime);

		Thread.sleep(100);

		assertEquals(1, RepositoryStorage.getAvailableModelsForTimeshift().size());

		landscape.setHash(200000);
		final long secondTime = java.lang.System.currentTimeMillis();
		RepositoryStorage.writeToFile(landscape, secondTime);

		assertEquals(2, RepositoryStorage.getAvailableModelsForTimeshift().size());

		assertEquals(100000, RepositoryStorage.readFromFile(firstTime + 5).getHash());
		assertEquals(200000, RepositoryStorage.readFromFile(secondTime + 5).getHash());

		RepositoryStorage.clearRepository();
	}

	@Test(expected = FileNotFoundException.class)
	public void testReadFromEmptyRepository() throws Exception {
		RepositoryStorage.clearRepository();
		RepositoryStorage.readFromFile(0).getHash();
	}
}
