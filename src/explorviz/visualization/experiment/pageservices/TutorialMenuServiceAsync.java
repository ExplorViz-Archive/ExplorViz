package explorviz.visualization.experiment.pageservices;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Santje Finke
 * 
 */
public interface TutorialMenuServiceAsync {

	void getPage(AsyncCallback<String> callback);

}
