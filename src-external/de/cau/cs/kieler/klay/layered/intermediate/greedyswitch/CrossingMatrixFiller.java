/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2015 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.intermediate.greedyswitch;

import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.intermediate.greedyswitch.SwitchDecider.CrossingCountSide;
import de.cau.cs.kieler.klay.layered.properties.GreedySwitchType;

/**
 * This class manages the crossing matrix and fills it on demand. It needs to be reinitialized for
 * each free layer. For each layer the node.id fields MUST be set from 0 to layer.getSize() - 1!
 * 
 * @author alan
 */
class CrossingMatrixFiller {
    private final boolean[][] isCrossingMatrixFilled;
    private final int[][] crossingMatrix;
    private final BetweenLayerEdgeTwoNodeCrossingsCounter inBetweenLayerCrossingCounter;
    private final CrossingCountSide direction;
    private final boolean oneSided;

    /**
     * Constructs class which manages the crossing matrix.
     */
    public CrossingMatrixFiller(final GreedySwitchType greedyType, final LNode[][] graph,
            final int freeLayerIndex, final CrossingCountSide direction) {
        
        this.direction = direction;
        oneSided = greedyType.isOneSided();

        LNode[] freeLayer = graph[freeLayerIndex];
        isCrossingMatrixFilled = new boolean[freeLayer.length][freeLayer.length];
        crossingMatrix = new int[freeLayer.length][freeLayer.length];

        inBetweenLayerCrossingCounter =
                new BetweenLayerEdgeTwoNodeCrossingsCounter(graph, freeLayerIndex);
    }

    /**
     * Returns entry for crossings between edges incident to two nodes, where upperNode is above
     * lowerNode in the layer.
     */
    public int getCrossingMatrixEntry(final LNode upperNode, final LNode lowerNode) {
        if (!isCrossingMatrixFilled[upperNode.id][lowerNode.id]) {
            fillCrossingMatrix(upperNode, lowerNode);
            isCrossingMatrixFilled[upperNode.id][lowerNode.id] = true;
            isCrossingMatrixFilled[lowerNode.id][upperNode.id] = true;
        }
        return crossingMatrix[upperNode.id][lowerNode.id];
    }

    private void fillCrossingMatrix(final LNode upperNode, final LNode lowerNode) {
        if (oneSided) {
            switch (direction) {
            case EAST:
                inBetweenLayerCrossingCounter.countEasternEdgeCrossings(upperNode, lowerNode);
                break;
            case WEST:
                inBetweenLayerCrossingCounter.countWesternEdgeCrossings(upperNode, lowerNode);
            }
        } else {
            inBetweenLayerCrossingCounter.countBothSideCrossings(upperNode, lowerNode);
        }
        crossingMatrix[upperNode.id][lowerNode.id] =
                inBetweenLayerCrossingCounter.getUpperLowerCrossings();
        crossingMatrix[lowerNode.id][upperNode.id] =
                inBetweenLayerCrossingCounter.getLowerUpperCrossings();
    }

}
