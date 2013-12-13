package explorviz.server.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.esotericsoftware.kryo.io.UnsafeOutput;

import explorviz.shared.model.*;

public class RepositoryStorage {
	private static final String FOLDER = "landscapeRepository";
	private static final Kryo kryo;

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

		new File(FOLDER).mkdir();
	}

	public static void writeToFile(final Landscape landscape, final long timestamp) {
		UnsafeOutput output = null;
		try {
			output = new UnsafeOutput(new FileOutputStream(FOLDER + "/" + timestamp));
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
		final List<Long> availableModels = getAvailableModels();
		Long readInModel = null;

		for (final Long availableModel : availableModels) {
			if (availableModel <= timestamp) {
				readInModel = availableModel;
			}
		}

		if (readInModel == null) {
			throw new FileNotFoundException("Model not found");
		}

		final UnsafeInput input = new UnsafeInput(new FileInputStream(FOLDER + "/" + readInModel));
		final Landscape landscape = kryo.readObject(input, Landscape.class);
		input.close();

		return landscape;
	}

	public static List<Long> getAvailableModels() {
		final List<Long> result = new ArrayList<Long>();

		final File[] files = new File(FOLDER).listFiles();
		for (final File file : files) {
			if (!file.getName().equals(".") && !file.getName().equals("..")) {
				result.add(Long.parseLong(file.getName()));
			}
		}

		return result;
	}

	public static void cleanUpTooOldFiles(final long currentTimestamp) {
		final long enddate = currentTimestamp - TimeUnit.HOURS.toNanos(1);
		final File[] files = new File(FOLDER).listFiles();
		for (final File file : files) {
			if (!file.getName().equals(".") && !file.getName().equals("..")) {
				if (Long.parseLong(file.getName()) <= enddate) {
					file.delete();
				}
			}
		}
	}

	public static void clearRepository() {
		final File[] files = new File(FOLDER).listFiles();
		for (final File file : files) {
			if (!file.getName().equals(".") && !file.getName().equals("..")) {
				file.delete();
			}
		}
	}
}
