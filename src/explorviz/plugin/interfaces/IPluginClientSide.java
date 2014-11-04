package explorviz.plugin.interfaces;

import explorviz.plugin.main.Perspective;
import explorviz.shared.model.*;

public interface IPluginClientSide {
	void switchedToPerspective(Perspective perspective);

	void popupMenuOpenedOn(Node node);

	void popupMenuOpenedOn(Application app);

	void newLandscapeReceived(Landscape landscape);
}
