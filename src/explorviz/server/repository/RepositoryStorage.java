package explorviz.server.repository;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.esotericsoftware.kryo.io.UnsafeOutput;

import explorviz.server.main.FileSystemHelper;
import explorviz.shared.model.*;

public class RepositoryStorage {
	private static String FOLDER;
	private static final Kryo kryo;

	public static final String EXTENSION = ".expl";
	private static final int HISTORY_INTERVAL_IN_MINUTES = 20;

	static {
		kryo = new Kryo();
		kryo.register(Landscape.class);
		kryo.register(NodeGroup.class);
		kryo.register(Node.class);
		kryo.register(Application.class);
		kryo.register(Component.class);
		kryo.register(Communication.class);
		kryo.register(Clazz.class);
		kryo.register(CommunicationClazz.class);

		FOLDER = FileSystemHelper.getExplorVizDirectory() + "/" + "landscapeRepository";

		java.lang.System.out.println("writing to " + FOLDER);

		new File(FOLDER).mkdir();
	}

	public static void writeToFile(final Landscape landscape, final long timestamp) {
		UnsafeOutput output = null;
		try {
			output = new UnsafeOutput(new FileOutputStream(FOLDER + "/" + timestamp + "-"
					+ landscape.getActivities() + EXTENSION));
			kryo.writeObject(output, landscape);
			output.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	public static Landscape readFromFile(final long timestamp) throws FileNotFoundException {
		final Map<Long, Long> availableModels = getAvailableModels();
		String readInModel = null;

		for (final Entry<Long, Long> availableModel : availableModels.entrySet()) {
			if (availableModel.getKey() <= timestamp) {
				readInModel = availableModel.getKey() + "-" + availableModel.getValue() + EXTENSION;
			}
		}

		if (readInModel == null) {
			throw new FileNotFoundException("Model not found for timestamp " + timestamp);
		}

		final UnsafeInput input = new UnsafeInput(new FileInputStream(FOLDER + "/" + readInModel));
		final Landscape landscape = kryo.readObject(input, Landscape.class);
		input.close();

		return landscape;
	}

	public static Map<Long, Long> getAvailableModels() {
		final Map<Long, Long> result = new TreeMap<Long, Long>();

		final File[] files = new File(FOLDER).listFiles();
		for (final File file : files) {
			if (!file.getName().equals(".") && !file.getName().equals("..")
					&& file.getName().endsWith(EXTENSION)) {
				final String[] split = file.getName().split("-");
				final long timestamp = Long.parseLong(split[0]);
				final long activities = Long.parseLong(split[1].split("\\.")[0]);
				result.put(timestamp, activities);
			}
		}

		return result;
	}

	public static void cleanUpTooOldFiles(final long currentTimestamp) {
		final long enddate = currentTimestamp
				- TimeUnit.MINUTES.toMillis(HISTORY_INTERVAL_IN_MINUTES);
		final File[] files = new File(FOLDER).listFiles();
		for (final File file : files) {
			if (!file.getName().equals(".") && !file.getName().equals("..")
					&& file.getName().endsWith(EXTENSION)) {
				if (Long.parseLong(file.getName().substring(0, file.getName().indexOf("-"))) <= enddate) {
					file.delete();
				}
			}
		}
	}

	public static void clearRepository() {
		final File[] files = new File(FOLDER).listFiles();
		for (final File file : files) {
			if (!file.getName().equals(".") && !file.getName().equals("..")
					&& file.getName().endsWith(EXTENSION)) {
				file.delete();
			}
		}
	}
}
