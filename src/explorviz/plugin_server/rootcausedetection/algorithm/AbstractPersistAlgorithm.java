package explorviz.plugin_server.rootcausedetection.algorithm;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_server.rootcausedetection.model.RanCorrLandscape;
import explorviz.shared.model.Application;
import explorviz.shared.model.helper.GenericModelElement;

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
		for (final Application application : lscp.getApplications()) {
			saveRCRWithSign(application,
					application.isIsRankingPositive() ? application.getRootCauseRating()
							: -application.getRootCauseRating());
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

	private void saveRCRWithSign(final GenericModelElement element, final Double rcr) {
		element.putGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY, rcr);
	}

}
