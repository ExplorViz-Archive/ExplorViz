package explorviz.plugin.rootcausedetection.model;

import java.util.HashSet;
import java.util.Set;

import explorviz.plugin.rootcausedetection.algorithm.AbstractPersistAlgorithm;
import explorviz.plugin.rootcausedetection.algorithm.AbstractRanCorrAlgorithm;
import explorviz.shared.model.*;

/**
 * This class provides methods to calculate and persist RootCauseRatings. It is
 * also used to convert ExplorViz landscapes to a format RanCorr algorithms can
 * use.
 *
 * @author Christian Claus Wiechmann
 *
 */
public class RanCorrLandscape {

	//
	// Attribute declarations
	//

	private final Set<RanCorrApplication> applications;
	private final Set<RanCorrPackage> packages;
	private final Set<RanCorrClass> classes;
	private final Set<RanCorrOperation> operations;

	//
	// Constructors
	//

	/**
	 * Create new RanCorrLandscape.
	 */
	public RanCorrLandscape() {
		applications = new HashSet<>();
		packages = new HashSet<>();
		classes = new HashSet<>();
		operations = new HashSet<>();
	}

	//
	// Getters/Setters
	//

	/**
	 * Add an existing ExplorViz-Application to the RanCorrLandscape as an
	 * application.
	 *
	 * @param app
	 *            specified ExplorViz-Application
	 */
	public void addApplication(final Application app) {
		applications.add((RanCorrApplication) app);
	}

	/**
	 * Add an existing ExplorViz-Component to the RanCorrLandscape as a package.
	 *
	 * @param com
	 *            specified ExplorViz-Component
	 */
	public void addPackage(final Component com) {
		packages.add((RanCorrPackage) com);
	}

	/**
	 * Add an existing ExplorViz-Class to the RanCorrLandscape as a class.
	 *
	 * @param cla
	 *            specified ExplorViz-Class
	 */
	public void addClass(final Clazz cla) {
		classes.add((RanCorrClass) cla);
	}

	/**
	 * Add an existing ExplorViz-CommunicationClazz to the RanCorrLandscape as
	 * an operation.
	 *
	 * @param op
	 *            specified ExplorViz-CommunicationClazz
	 */
	public void addOperation(final CommunicationClazz op) {
		operations.add((RanCorrOperation) op);
	}

	//
	// Public methods
	//

	/**
	 * Calculate the RootCauseRatings of all elements in this landscape. Anomaly
	 * Scores have to be provided beforehand.
	 *
	 * @param rca
	 *            Specifies the concrete algorithm the RootCauseRatings are to
	 *            be calculated with.
	 */
	public void calculateRootCauseRatings(final AbstractRanCorrAlgorithm rca) {
		// TODO calculateRootCauseRatings
	}

	/**
	 * Persist all RootCauseRatings to the underlying ExplorViz landscape.
	 * {@link RanCorrLandscape#calculateRootCauseRatings
	 * calculateRootCauseRatings(final AbstractRanCorrAlgorithm)} has to be
	 * called beforehand.
	 *
	 * @param pa
	 *            Specifies the concrete algorithm the RootCauseRatings are to
	 *            be persisted with (e. g. RGB values in ExplorViz landscape).
	 */
	public void persistRootCauseRatings(final AbstractPersistAlgorithm pa) {
		// TODO persistRootCauseRatings
	}

}
