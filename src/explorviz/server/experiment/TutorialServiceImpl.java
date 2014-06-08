package explorviz.server.experiment;

import java.io.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.shared.experiment.Step;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.services.TutorialService;

public class TutorialServiceImpl extends RemoteServiceServlet implements TutorialService {

	private static final long serialVersionUID = -3052597724861711546L;

	@Override
	public String getText(final int number) throws IOException {

		final String language = Configuration.selectedLanguage;
		String filePath = new File("").getAbsolutePath();
		filePath = filePath + "/../tutorial/" + language + "/" + number + ".txt";
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
			Logging.log(e.getMessage());

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

}
