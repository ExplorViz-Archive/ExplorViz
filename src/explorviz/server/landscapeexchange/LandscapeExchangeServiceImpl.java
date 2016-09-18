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

	private static Long timestamp = null;
	private static Long activity = null;

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
				// return model.getLastPeriodLandscape();
				return LandscapeDummyCreator.createDummyLandscape();
			} else {
				return getLandscape(timestamp, activity);
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

	private Landscape getLandscape(final long timestamp, final long activity) {
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
			names.add(f.getName());
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
}