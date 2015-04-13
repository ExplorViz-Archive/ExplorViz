package explorviz.server.experiment;

import java.io.*;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.main.Configuration;
import explorviz.shared.experiment.Step;
import explorviz.visualization.experiment.services.TutorialService;

/**
 * @author Santje Finke
 *
 */
public class TutorialServiceImpl extends RemoteServiceServlet implements TutorialService {

	private static final long serialVersionUID = -3052597724861711546L;
	private static final Logger log = Logger.getLogger("TutorialService");

	@Override
	public String getText(final int number, final boolean controlgroup) throws IOException {

		final String language = Configuration.selectedLanguage;
		final String tutorialFolder = getServletContext().getRealPath("/tutorial/");

		String filePath = tutorialFolder + "/" + language + "/" + number + ".txt";

		if (controlgroup) {
			filePath = tutorialFolder + "/" + language + "/controlgroup/" + number + ".txt";
		}

		BufferedReader br = null;
		String line;
		final StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new FileReader(filePath));
			line = br.readLine();
			while (null != line) {
				sb.append(line + "\n");
				line = br.readLine();
			}
			br.close();
		} catch (final FileNotFoundException e) {
			log.severe(e.getMessage());
		}
		return sb.toString();
	}

	@Override
	public String getLanguage() {
		return Configuration.selectedLanguage;
	}

	@Override
	public String[] getLanugages() {
		return Configuration.languages.toArray(new String[0]);
	}

	@Override
	public Step[] getSteps() {
		return Configuration.tutorialSteps.toArray(new Step[0]);
	}

	@Override
	public Step[] getStepsControllGroup() {
		return Configuration.tutorialControlGroupSteps.toArray(new Step[0]);
	}

	@Override
	public boolean isExperiment() {
		return Configuration.experiment;
	}

	@Override
	public void setTimeshift(final boolean secondLandscape, final long time) {
		Configuration.secondLandscape = secondLandscape;
		Configuration.secondLandscapeTime = time;
	}

	@Override
	public void setTime(final long l) {
		Configuration.tutorialStart = l;
	}
}
