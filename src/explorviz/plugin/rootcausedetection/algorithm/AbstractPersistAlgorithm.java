package explorviz.plugin.rootcausedetection.algorithm;

import explorviz.plugin.rootcausedetection.model.RanCorrLandscape;

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
	 * ExplorViz landscape.
	 *
	 * @param lscp
	 *            specified landscape
	 */
	public abstract void persist(RanCorrLandscape lscp);

}
