package explorviz.plugin_server.rootcausedetection.algorithm;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_server.rootcausedetection.model.RanCorrApplication;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;

/**
 * This abstract class represents algorithms concerning the persistence of
 * RootCauseRatings in the underlying ExporViz landscape.
 *
 * @author Christian Claus Wiechmann
 *
 */
public abstract class AbstractPersistAlgorithm {

	/**
	 * Persist RootCauseRatings from a RanCorrLandscape in the underlying
	 * ExplorViz landscape. Also gives values to Capacity Planning.
	 *
	 * @param lscp
	 *            specified landscape
	 */
	public void persist(final RanCorrLandscape lscp) {
		persistRankings(lscp);

		// give application ratings to Capacity Planning
		for (final RanCorrApplication application : lscp.getApplications()) {
			application.putGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY,
					application.getRootCauseRating());
		}
	}

	/**
	 * Persist RootCauseRatings from a RanCorrLandscape in the underlying
	 * ExplorViz landscape.
	 *
	 * @param lscp
	 *            specified landscape
	 */
	protected abstract void persistRankings(RanCorrLandscape lscp);

}
