package explorviz.server.experiment;

import java.io.*;
import java.util.Map.Entry;
import java.util.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;

import explorviz.server.main.FileSystemHelper;
import explorviz.server.repository.RepositoryStorage;
import explorviz.shared.model.*;

public class LandscapeReplayer {
	static String FULL_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ "replay";
	static final String EXTENSION = RepositoryStorage.EXTENSION;

	private long maxTimestamp;
	private long lastTimestamp = 0;
	private long lastActivity = 0;
	private final Kryo kryo;

	/**
	 * Attention: Only single threaded!
	 */
	public LandscapeReplayer() {
		setMaxTimestamp(0);

		kryo = new Kryo();
		kryo.register(Landscape.class);
		kryo.register(NodeGroup.class);
		kryo.register(Node.class);
		kryo.register(Application.class);
		kryo.register(Component.class);
		kryo.register(Communication.class);
		kryo.register(Clazz.class);
		kryo.register(CommunicationClazz.class);
	}

	public void setMaxTimestamp(final long maxTimestamp) {
		this.maxTimestamp = maxTimestamp;
	}

	public void reset() {
		maxTimestamp = 0;
		lastTimestamp = 0;
		lastActivity = 0;
	}

	public Landscape getCurrentLandscape() {
		final SortedMap<Long, Long> landscapeList = listAllLandscapes();

		// TODO real time must have passed - what if user pushes F5?

		for (final Entry<Long, Long> landscapeEntry : landscapeList.entrySet()) {
			final long key = landscapeEntry.getKey();
			if ((lastTimestamp < key) && (key <= maxTimestamp)) {
				lastTimestamp = key;
				lastActivity = landscapeEntry.getValue();
				break;
			}
		}

		if (lastTimestamp > 0) {
			return getLandscape(lastTimestamp, lastActivity);
		} else {
			return null;
		}
	}

	private SortedMap<Long, Long> listAllLandscapes() {
		final SortedMap<Long, Long> result = new TreeMap<Long, Long>();

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