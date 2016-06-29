package explorviz.visualization.landscapeexchange;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.model.Landscape;

@RemoteServiceRelativePath("landscapeexchange")
public interface LandscapeExchangeService extends RemoteService {
	public Landscape getCurrentLandscape();

	void resetLandscape();

	public Landscape getLandscape(long timestamp);

	public Landscape getLandscapeByTimestampAndActivity(final long timestamp, final long activity);
}
