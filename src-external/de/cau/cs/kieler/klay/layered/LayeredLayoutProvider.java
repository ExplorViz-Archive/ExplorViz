/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2010 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.kiml.AbstractLayoutProvider;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LGraphElement.HashCodeCounter;
import de.cau.cs.kieler.klay.layered.importexport.IGraphImporter;
import de.cau.cs.kieler.klay.layered.importexport.KGraphImporter;

/**
 * Layout provider to connect the layered layouter to the Eclipse based layout
 * services.
 * 
 * @see KlayLayered
 * 
 * @author msp
 * @author cds
 * @kieler.design 2012-08-10 chsch grh
 * @kieler.rating proposed yellow by msp
 */
public final class LayeredLayoutProvider extends AbstractLayoutProvider {

	// /////////////////////////////////////////////////////////////////////////////
	// Variables

	/** the layout algorithm used for regular layout runs. */
	private final KlayLayered klayLayered = new KlayLayered();

	// /////////////////////////////////////////////////////////////////////////////
	// Regular Layout

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doLayout(final KNode kgraph, final IKielerProgressMonitor progressMonitor) {
		// Create the hash code counter used to create all graph elements; this
		// is used to ensure
		// that all hash codes are unique, but predictable independently of the
		// object instances.
		final HashCodeCounter hashCodeCounter = new HashCodeCounter();

		final KShapeLayout kgraphLayout = kgraph.getData(KShapeLayout.class);

		// Check if hierarchy handling for a compound graph is requested
		// Only the top-level graph is processed
		final IGraphImporter<KNode> graphImporter = new KGraphImporter(hashCodeCounter);

		// Import the graph
		final LGraph layeredGraph = graphImporter.importGraph(kgraph);

		// Perform layer-based layout
		final LGraph result = klayLayered.doLayout(layeredGraph, progressMonitor);

		if (!progressMonitor.isCanceled()) {
			// Apply the layout results to the original graph
			graphImporter.applyLayout(result);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Layout Testing

	/**
	 * Imports the given {@link KGraph} and returns an instance of
	 * {@link KlayLayered} prepared for a test run with the resulting
	 * {@link LGraph}. The layout test run methods can immediately be called on
	 * the returned object. Once finished,
	 * {@link KlayLayered#finalizeLayoutTest()} should be called.
	 * 
	 * <p>
	 * <strong>Note:</strong> This method does not apply the layout back to the
	 * original kgraph!
	 * </p>
	 * 
	 * @param kgraph
	 *            the {@link KGraph} to be used for the layout test run.
	 * @return an instance of {@link KlayLayered} with
	 *         {@link KlayLayered#prepareLayoutTest(LGraph, IKielerProgressMonitor)}
	 *         already called.
	 */
	public KlayLayered startLayoutTest(final KNode kgraph) {

		// Create the hash code counter used to create all graph elements; this
		// is used to ensure
		// that all hash codes are unique, but predictable independently of the
		// object instances.
		final HashCodeCounter hashCodeCounter = new HashCodeCounter();

		final IGraphImporter<KNode> graphImporter = new KGraphImporter(hashCodeCounter);

		final LGraph layeredGraph = graphImporter.importGraph(kgraph);

		// return a new instance of KLay Layered initialized with the given
		// layout test data
		final KlayLayered algorithm = new KlayLayered();
		algorithm.prepareLayoutTest(layeredGraph);

		return algorithm;
	}

}
