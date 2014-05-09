package explorviz.server.experiment;

import java.io.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.visualization.experiment.services.TutorialService;

public class TutorialServiceImpl extends RemoteServiceServlet implements TutorialService {

	private static final long serialVersionUID = -3052597724861711546L;

	@Override
	public String getText(final int number) throws IOException {
		final String language = Configuration.selectedLanguage;
		final String path = "./" + language + "/" + number + ".txt";
		BufferedReader br = null;
		String line;
		final StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new FileReader(path));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}

		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			br.close();
		}
		return sb.toString();
	}

	@Override
	public String getLanguage() {
		return Configuration.selectedLanguage;
	}

	@Override
	public String[] getLanugages() {
		System.err.println(Configuration.languages);
		return Configuration.languages.toArray(new String[0]);
	}

}
