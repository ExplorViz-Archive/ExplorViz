package explorviz.plugin_server.rootcausedetection.algorithm;

import explorviz.plugin_client.attributes.IPluginKeys;
import explorviz.plugin_server.rootcausedetection.exception.PersistAlgorithmException;
import explorviz.plugin_server.rootcausedetection.model.*;
import explorviz.shared.model.helper.GenericModelElement;

/**
 * This class contains an algorithm which converts RootCauseRatings to RGB
 * values and stores them in an ExplorViz landscape.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class RGBAlgorithm extends AbstractPersistAlgorithm {

	/**
	 * This class represents an RGB-Value as used by {@link RGBAlgorithm}.
	 *
	 * @author Christian Claus Wiechmann
	 *
	 */
	public final class RGBTuple {
		private final int red, green, blue;

		public RGBTuple(final int red, final int green, final int blue) {
			if ((red < 0) || (red > 255)) {
				throw new PersistAlgorithmException("RGBTuple: Red is out of range. (value=" + red
						+ ")");
			}
			if ((green < 0) || (green > 255)) {
				throw new PersistAlgorithmException("RGBTuple: Green is out of range. (value="
						+ green + ")");
			}
			if ((blue < 0) || (blue > 255)) {
				throw new PersistAlgorithmException("RGBTuple: Blue is out of range. (value="
						+ blue + ")");
			}

			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		@Override
		public String toString() {
			return new String(red + "," + green + "," + blue);
		}
	}

	@Override
	protected void persistRankings(final RanCorrLandscape lscp) {
		for (final RanCorrClass clazz : lscp.getClasses()) {
			saveToElement(clazz, calculateColorFromRCR(clazz.getRootCauseRating()));
		}

		for (final RanCorrPackage component : lscp.getPackages()) {
			saveToElement(component, calculateColorFromRCR(component.getRootCauseRating()));
		}

		for (final RanCorrApplication application : lscp.getApplications()) {
			saveToElement(application, calculateColorFromRCR(application.getRootCauseRating()));
			saveRCRWithSign(
					application,
					application.isRankingPositive ? application.getRootCauseRating() : -application
							.getRootCauseRating());
		}
	}

	/**
	 * This method calculates an RGB color between green and red based on a RCR.
	 *
	 * Examples:
	 *
	 * rating = 0 => (0,255,0) - green
	 *
	 * rating = 0.5 => (255,255,0) - yellow
	 *
	 * rating = 1 => (255,0,0) - red
	 *
	 * @param rating
	 *            Specifies the rating to be converted to RGB
	 * @return RGB value corresponding to input rating
	 */
	public RGBTuple calculateColorFromRCR(final double rating) {
		int red = new Double(rating * 510.0d).intValue();
		int green = 255 - (new Double(Math.max(0, rating - 0.5d) * 510.0d).intValue());
		int blue = 0;

		// ensure that values are in boundaries
		red = Math.min(255, Math.max(0, red));
		green = Math.min(255, Math.max(0, green));
		blue = Math.min(255, Math.max(0, blue));

		return new RGBTuple(red, green, blue);
	}

	private void saveToElement(final GenericModelElement element, final RGBTuple colors) {
		element.putGenericStringData(IPluginKeys.ROOTCAUSE_RGB_INDICATOR, colors.toString());
	}

	private void saveRCRWithSign(final GenericModelElement element, final Double rcr) {
		element.putGenericDoubleData(IPluginKeys.ROOTCAUSE_APPLICATION_PROBABILITY, rcr);
	}

}
