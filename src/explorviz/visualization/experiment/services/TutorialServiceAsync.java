package explorviz.visualization.experiment.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

import explorviz.shared.experiment.Step;

/**
 * @author Santje Finke
 *
 */
public interface TutorialServiceAsync {

	void getText(int number, final boolean controlgroup, AsyncCallback<String> callback);

	void getLanguage(AsyncCallback<String> callback);

	void getLanugages(AsyncCallback<String[]> callback);

	void getSteps(AsyncCallback<Step[]> callback);

	void isExperiment(AsyncCallback<Boolean> callback);

	void setTimeshift(boolean secondLandscape, long l, AsyncCallback<Void> callback);

	void setTime(long l, AsyncCallback<Void> callback);

	void getStepsControllGroup(AsyncCallback<Step[]> callback);

	void getExperimentFilename(AsyncCallback<String> callback);
}
