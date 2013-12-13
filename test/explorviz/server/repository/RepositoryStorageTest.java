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
		RepositoryStorage.writeToFile(landscape, 100000);

		assertEquals(1, RepositoryStorage.getAvailableModels().size());
		// assertEquals((long) 0, (long)
		// RepositoryStorage.getAvailableModels().get(100000));

		landscape.setHash(200000);
		RepositoryStorage.writeToFile(landscape, 200000);

		assertEquals(2, RepositoryStorage.getAvailableModels().size());

		assertEquals(100000, RepositoryStorage.readFromFile(100005).getHash());
		assertEquals(200000, RepositoryStorage.readFromFile(200005).getHash());

		RepositoryStorage.clearRepository();
	}

	@Test(expected = FileNotFoundException.class)
	public void testReadFromEmptyRepository() throws Exception {
		RepositoryStorage.clearRepository();
		RepositoryStorage.readFromFile(0).getHash();
	}
}
