package explorviz.visualization.experiment.services;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.experiment.Step;

/**
 * @author Santje Finke
 * 
 */
@RemoteServiceRelativePath("tutorialservice")
public interface TutorialService extends RemoteService {
	/**
	 * Fetches the text of a tutorialstep based on the steps number.
	 * 
	 * @param number
	 *            The number of the tutorialstep whose text is to be fetched
	 * @return The tutorialtext
	 * @throws IOException
	 */
	public String getText(int number) throws IOException;

	/**
	 * @return The language setting
	 */
	public String getLanguage();

	/**
	 * @return All available languages
	 */
	public String[] getLanugages();

	/**
	 * @return All tutorialsteps
	 */
	Step[] getSteps();

	/**
	 * @return The experiment setting
	 */
	boolean isExperiment();

	/**
	 * Sets the correct landscape to be displayed.
	 * 
	 * @param secondLandscape
	 *            true if the second landscape is to be loaded, false if the
	 *            first is to be loaded
	 * @param l
	 *            The time when the timeshift occures
	 */
	void setTimeshift(boolean secondLandscape, long l);

	/**
	 * Saves when the tutorial was started to create the timeshift graph
	 * 
	 * @param l
	 *            The time when the tutorial was started
	 */
	void setTime(long l);
}
