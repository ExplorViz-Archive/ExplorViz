package explorviz.server.repository;

import java.io.*;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.esotericsoftware.kryo.io.UnsafeOutput;

import explorviz.shared.model.Landscape;

public class RepositoryStorage {
	private static final String filenameStart = "landscapeRepository/";
	private final Kryo kryo;

	public RepositoryStorage() {
		kryo = new Kryo();
		kryo.register(Landscape.class);

		new File("landscapeRepository").mkdir();
	}

	public void writeToFile(final Landscape landscape, final long timestamp) {
		UnsafeOutput output = null;
		try {
			output = new UnsafeOutput(new FileOutputStream(filenameStart + timestamp + ".bin"));
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

	public Landscape readFromFile(final long timestamp) throws FileNotFoundException {
		final UnsafeInput input = new UnsafeInput(new FileInputStream(filenameStart + timestamp
				+ ".bin")); // TODO search for latest timestamp
		final Landscape landscape = kryo.readObject(input, Landscape.class);
		input.close();

		return landscape;
	}

	public void cleanUpTooOldFiles(final long currentTimestamp) {
		final long enddate = currentTimestamp - TimeUnit.HOURS.toNanos(1);
		final File[] files = new File("landscapeRepository").listFiles();
		for (final File file : files) {
			if (file.getName().endsWith(".bin")) {
				final String date = file.getName().substring(0, file.getName().indexOf(".bin"));
				if (Long.parseLong(date) <= enddate) {
					file.delete();
				}
			}
		}
	}
}
