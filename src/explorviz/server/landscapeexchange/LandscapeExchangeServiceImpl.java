package explorviz.server.landscapeexchange;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.experiment.LandscapeReplayer;
import explorviz.server.main.Configuration;
import explorviz.server.main.FileSystemHelper;
import explorviz.server.repository.*;
import explorviz.shared.model.Landscape;
import explorviz.visualization.landscapeexchange.LandscapeExchangeService;

public class LandscapeExchangeServiceImpl extends RemoteServiceServlet
		implements LandscapeExchangeService {

	private static final long serialVersionUID = 4310863128987822861L;
	private static LandscapeRepositoryModel model;

	// czi 22.03.2017
	// Loading fixed landscape for icsa `17 study
	// define coressponding timestamp of landscape.expl in timestamp
	// and put this file into .explorviz/replay folder
	private static Long timestamp = null;
	// 1490794780233L verteilt 29.03.2017
	// default: null
	// 1490291791162L verteilt
	// 1490701381490L zusammengefasst
	private static Long activity = null; // null

	static String FULL_FOLDER = FileSystemHelper.getExplorVizDirectory() + File.separator
			+ "replay";

	static {
		startRepository();
	}

	@Override
	public Landscape getLandscapeByTimestampAndActivity(final long timestamp, final long activity) {
		LandscapeExchangeServiceImpl.timestamp = timestamp;
		LandscapeExchangeServiceImpl.activity = activity;
		return getCurrentLandscape();
	}

	public static LandscapeRepositoryModel getModel() {
		return model;
	}

	@Override
	public Landscape getCurrentLandscape() {
		if (Configuration.experiment) {
			final LandscapeReplayer replayer = LandscapeReplayer.getReplayerForCurrentUser();

			return replayer.getCurrentLandscape();
		} else {
			if (timestamp == null) {
				if (Configuration.dummyMode) {
					return LandscapeDummyCreator.createDummyLandscape();
				} else {
					return model.getLastPeriodLandscape();
				}

			} else {
				if (Configuration.staticReplay) {
					final LandscapeReplayer replayer = LandscapeReplayer
							.getReplayerForCurrentUser();
					return replayer.getLandscape(timestamp);
				} else {
					return getLandscape(timestamp, activity);
				}
			}
		}
	}

	@Override
	public Landscape getCurrentLandscapeByFlag(final boolean isExperiment) {
		if (isExperiment) {
			final LandscapeReplayer replayer = LandscapeReplayer.getReplayerForCurrentUser();

			return replayer.getCurrentLandscape();
		} else {
			if (timestamp == null) {
				// return model.getLastPeriodLandscape();
				return LandscapeDummyCreator.createDummyLandscape();
			} else {
				return getLandscape(timestamp, activity);
			}
		}
	}

	@Override
	public Landscape getLandscape(final long timestamp, final long activity) {
		// IMPORTANT: Kryo depends heavily on used JDK version for
		// serialization.
		// Landscapes that are serialized with an older JDK
		// are not supported for deserialization.
		Input input = null;
		Landscape landscape = null;
		try {
			input = new Input(new FileInputStream(FULL_FOLDER + File.separator + timestamp + "-"
					+ activity + Configuration.MODEL_EXTENSION));
			final Kryo kryo = RepositoryStorage.createKryoInstance();
			landscape = kryo.readObject(input, Landscape.class);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				input.close();
			}
		}

		return LandscapePreparer.prepareLandscape(landscape);
	}

	@Override
	public List<String> getReplayNames() {
		final List<String> names = new ArrayList<String>();

		final File directory = new File(FULL_FOLDER);

		final File[] fList = directory.listFiles();

		for (final File f : fList) {

			final String filename = f.getName();

			if (filename.endsWith(".expl")) {

				// first validation check -> filename
				long timestamp;
				long activity;

				try {
					timestamp = Long.parseLong(filename.split("-")[0]);
					activity = Long.parseLong(filename.split("-")[1].split(".expl")[0]);
				} catch (final NumberFormatException e) {
					continue;
				}

				// second validation check -> deserialization
				try {
					LandscapeExchangeServiceImpl.getLandscapeStatic(timestamp, activity);
				} catch (final Exception e) {
					continue;
				}

				names.add(filename);
			}

		}

		return names;
	}

	@Override
	public Landscape getLandscape(final long timestamp) {
		try {
			if (Configuration.experiment) {
				final LandscapeReplayer replayer = LandscapeReplayer.getReplayerForCurrentUser();

				return replayer.getLandscape(timestamp);
			} else {
				return model.getLandscape(timestamp);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void resetLandscape() {
		timestamp = null;
		activity = null;

		model.reset();
	}

	private static void startRepository() {
		model = new LandscapeRepositoryModel();
		new Thread(new Runnable() {

			@Override
			public void run() {
				new RepositoryStarter().start(model);
			}
		}).start();
	}

	public static Landscape getLandscapeStatic(final long timestamp, final long activity)
			throws Exception {
		// IMPORTANT: Kryo depends heavily on used JDK version for
		// serialization.
		// Landscapes that are serialized with an older JDK
		// are not supported for deserialization.
		Input input = null;
		Landscape landscape = null;
		try {
			input = new Input(new FileInputStream(FULL_FOLDER + File.separator + timestamp + "-"
					+ activity + Configuration.MODEL_EXTENSION));
			final Kryo kryo = RepositoryStorage.createKryoInstance();
			landscape = kryo.readObject(input, Landscape.class);
		} catch (final Exception e) {
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
		}

		return LandscapePreparer.prepareLandscape(landscape);
	}

	public static Landscape getLandscapeByByte(final byte[] landscapeBytes) throws Exception {
		// IMPORTANT: Kryo depends heavily on used JDK version for
		// serialization.
		// Landscapes that are serialized with an older JDK
		// are not supported for deserialization.
		Input input = null;
		Landscape landscape = null;
		try {
			input = new Input(landscapeBytes);
			final Kryo kryo = RepositoryStorage.createKryoInstance();
			landscape = kryo.readObject(input, Landscape.class);
		} catch (final Exception e) {
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
		}

		return LandscapePreparer.prepareLandscape(landscape);
	}
}