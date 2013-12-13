/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2011 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.force;

import java.util.List;
import java.util.Random;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.kiml.AbstractLayoutProvider;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.force.graph.FGraph;
import de.cau.cs.kieler.klay.force.model.AbstractForceModel;
import de.cau.cs.kieler.klay.force.model.EadesModel;
import de.cau.cs.kieler.klay.force.model.ForceModelStrategy;
import de.cau.cs.kieler.klay.force.model.FruchtermanReingoldModel;
import de.cau.cs.kieler.klay.force.properties.Properties;

/**
 * Layout provider for the force layout algorithms.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class ForceLayoutProvider extends AbstractLayoutProvider {

    /** the force model used by this layout algorithm. */
    private AbstractForceModel forceModel;
    /** connected components processor. */
    private ComponentsProcessor componentsProcessor = new ComponentsProcessor();

    /**
     * {@inheritDoc}
     */
    @Override
    public void doLayout(final KNode kgraph, final IKielerProgressMonitor progressMonitor) {
        progressMonitor.begin("KLay Force", 1);
        
        // transform the input graph
        IGraphImporter<KNode> graphImporter = new KGraphImporter();
        FGraph fgraph = graphImporter.importGraph(kgraph);

        // set special properties for the layered graph
        setOptions(fgraph, kgraph);

        // update the force model depending on user selection
        updateModel(fgraph.getProperty(Properties.FORCE_MODEL));
        
        // split the input graph into components
        List<FGraph> components = componentsProcessor.split(fgraph);
        
        // perform the actual layout
        for (FGraph comp : components) {
            forceModel.layout(comp, progressMonitor.subTask(1.0f / components.size()));
        }
        
        // pack the components back into one graph
        fgraph = componentsProcessor.pack(components);
        
        // apply the layout results to the original graph
        graphImporter.applyLayout(fgraph);
        
        progressMonitor.done();
    }
    
    /**
     * Set special layout options for the force graph.
     * 
     * @param fgraph a new force graph
     * @param parent the original parent node
     */
    private void setOptions(final FGraph fgraph, final KNode parent) {
        // set the random number generator based on the random seed option
        Integer randomSeed = fgraph.getProperty(LayoutOptions.RANDOM_SEED);
        if (randomSeed != null) {
            int val = randomSeed;
            if (val == 0) {
                fgraph.setProperty(Properties.RANDOM, new Random());
            } else {
                fgraph.setProperty(Properties.RANDOM, new Random(val));
            }
        } else {
            fgraph.setProperty(Properties.RANDOM, new Random(1));
        }
    }
    
    /**
     * Update the force model depending on the chosen strategy.
     * 
     * @param strategy a force model strategy
     */
    private void updateModel(final ForceModelStrategy strategy) {
        switch (strategy) {
        case EADES:
            if (!(forceModel instanceof EadesModel)) {
                forceModel = new EadesModel();
            }
            break;
        case FRUCHTERMAN_REINGOLD:
            if (!(forceModel instanceof FruchtermanReingoldModel)) {
                forceModel = new FruchtermanReingoldModel();
            }
            break;
        }
    }

}
