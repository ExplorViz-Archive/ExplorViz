package explorviz.plugin_client.interfaces;

import explorviz.plugin_client.main.Perspective;
import explorviz.shared.model.*;

public interface IPluginClientSide {
	void switchedToPerspective(Perspective perspective);

	void popupMenuOpenedOn(Node node);

	void popupMenuOpenedOn(Application app);

	void newLandscapeReceived(Landscape landscape);
}
