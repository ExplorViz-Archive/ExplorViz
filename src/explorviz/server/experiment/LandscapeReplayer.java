package explorviz.server.experiment;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;

import explorviz.server.main.FileSystemHelper;
import explorviz.server.repository.RepositoryStorage;
import explorviz.shared.model.*;

public class LandscapeReplayer {
	static final String REPLAY_FOLDER = "replay";
	static final String FULL_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ REPLAY_FOLDER;
	static final String EXTENSION = RepositoryStorage.EXTENSION;

	public void setMaxTimestamp(final long maxTimestamp) {

	}

	public Landscape getCurrentLandscape(final long maxTimestamp) {
		final Map<Long, Long> landscapeList = listAllLandscapes();

		for (final Entry<Long, Long> landscapeEntry : landscapeList.entrySet()) {
			return getLandscape(landscapeEntry.getKey(), landscapeEntry.getValue());
		}

		return null;
	}

	private Map<Long, Long> listAllLandscapes() {
		final Map<Long, Long> result = new HashMap<Long, Long>();

		final File[] fileList = new File(FULL_FOLDER).listFiles();

		for (final File file : fileList) {
			if (file.getName().endsWith(EXTENSION)) {
				final String[] split = file.getName().split("-");
				final long timestamp = Long.parseLong(split[0]);
				final long activities = Long.parseLong(split[1].split("\\.")[0]);
				result.put(timestamp, activities);
			}
		}

		return result;
	}

	private Landscape getLandscape(final long timestamp, final long activity) {
		final Kryo kryo = new Kryo();
		kryo.register(Landscape.class);
		kryo.register(NodeGroup.class);
		kryo.register(Node.class);
		kryo.register(Application.class);
		kryo.register(Component.class);
		kryo.register(Communication.class);
		kryo.register(Clazz.class);
		kryo.register(CommunicationClazz.class);

		UnsafeInput input = null;
		Landscape landscape = null;
		try {
			input = new UnsafeInput(new FileInputStream(FULL_FOLDER + File.separator + timestamp
					+ "-" + activity + EXTENSION));
			landscape = kryo.readObject(input, Landscape.class);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				input.close();
			}
		}

		return landscape;
	}
}