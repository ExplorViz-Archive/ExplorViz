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
package de.cau.cs.kieler.klay.layered.p3order;

import java.util.LinkedList;
import java.util.List;

import de.cau.cs.kieler.klay.layered.graph.LNode;

/**
 * A node group contains one or more nodes. Node groups are used to model sets of nodes that are
 * placed next to each other. A node group contains methods to calculate its barycenter value,
 * to merge with another vertex and to generally do cool stuff.
 * 
 * @author cds
 * @author ima
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public final class NodeGroup implements Comparable<NodeGroup> {

    // CHECKSTYLEOFF VisibilityModifier
    
    /**
     * The sum of the node weights. Each node weight is the sum of the weights of the ports the
     * node's ports are connected to.
     */
    public float summedWeight;

    /**
     * The number of ports relevant to the barycenter calculation.
     */
    public int degree;

    /**
     * This vertex' barycenter value. (summedWeight / degree)
     */
    public Float barycenter;

    /**
     * The number of incoming constraints.
     */
    public int incomingConstraintsCount;
    
    /**
     * Whether the node group has been visited in some traversing algorithm.
     */
    public boolean visited;
    
    // CHECKSTYLEON VisibilityModifier

    /**
     * List of nodes this vertex consists of.
     */
    private final LNode[] nodes;

    /**
     * List of outgoing constraints.
     */
    private List<NodeGroup> outgoingConstraints;
    
    /**
     * List of incoming constraints.
     */
    private List<NodeGroup> incomingConstraints;

    /**
     * Constructs a new instance containing the given node.
     * 
     * @param node
     *            the node the vertex should contain
     */
    public NodeGroup(final LNode node) {
        nodes = new LNode[] { node };
    }

    /**
     * Constructs a new vertex that is the concatenation of the given two vertices. The incoming
     * constraints count is set to zero, while the list of successors are merged, updating the
     * successors' incoming count appropriately if both vertices are predecessors.
     * The new barycenter is derived from the barycenters of the given node groups.
     * 
     * @param nodeGroup1
     *            the first vertex
     * @param nodeGroup2
     *            the second vertex
     */
    public NodeGroup(final NodeGroup nodeGroup1, final NodeGroup nodeGroup2) {
        // create a combined nodes array
        int length1 = nodeGroup1.nodes.length;
        int length2 = nodeGroup2.nodes.length;
        nodes = new LNode[length1 + length2];
        for (int i = 0; i < length1; i++) {
            nodes[i] = nodeGroup1.nodes[i];
        }
        for (int i = 0; i < length2; i++) {
            nodes[length1 + i] = nodeGroup2.nodes[i];
        }

        // Add constraints, taking care not to add any constraints to vertex1 or vertex2
        // and to decrement the incoming constraints count of those that are successors to both
        if (nodeGroup1.outgoingConstraints != null) {
            this.outgoingConstraints = new LinkedList<NodeGroup>(nodeGroup1.outgoingConstraints);
            this.outgoingConstraints.remove(nodeGroup2);
            if (nodeGroup2.outgoingConstraints != null) {
                for (NodeGroup candidate : nodeGroup2.outgoingConstraints) {
                    if (candidate == nodeGroup1) {
                        continue;
                    } else if (outgoingConstraints.contains(candidate)) {
                        // The candidate was in both vertices' successor list
                        candidate.incomingConstraintsCount--;
                    } else {
                        outgoingConstraints.add(candidate);
                    }
                }
            }
        } else if (nodeGroup2.outgoingConstraints != null) {
            this.outgoingConstraints = new LinkedList<NodeGroup>(nodeGroup2.outgoingConstraints);
            this.outgoingConstraints.remove(nodeGroup1);
        }

        this.summedWeight = nodeGroup1.summedWeight + nodeGroup2.summedWeight;
        this.degree = nodeGroup1.degree + nodeGroup2.degree;

        if (degree > 0) {
            barycenter = summedWeight / degree;
        } else if (nodeGroup1.barycenter != null && nodeGroup2.barycenter != null) {
            barycenter = (nodeGroup1.barycenter + nodeGroup2.barycenter) / 2;
        } else if (nodeGroup1.barycenter != null) {
            barycenter = nodeGroup1.barycenter;
        } else if (nodeGroup2.barycenter != null) {
            barycenter = nodeGroup2.barycenter;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < nodes.length; i++) {
            sb.append(nodes[i].toString());
            if (barycenter != null) {
                sb.append("<").append(barycenter.toString()).append(">");
            }
            if (i < nodes.length - 1) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }

    /**
     * Returns the list of outgoing constraints, creating it if not yet done before.
     * 
     * @return the outgoing constraints list of the node group
     */
    public List<NodeGroup> getOutgoingConstraints() {
        if (outgoingConstraints == null) {
            outgoingConstraints = new LinkedList<NodeGroup>();
        }
        return outgoingConstraints;
    }
    
    /**
     * Reset the list of outgoing constraints to {@code null}.
     */
    public void resetOutgoingConstraints() {
        outgoingConstraints = null;
    }
    
    /**
     * Determine whether there are any outgoing constraints.
     * 
     * @return true if there are outgoing constraints
     */
    public boolean hasOutgoingConstraints() {
        return outgoingConstraints != null && outgoingConstraints.size() > 0;
    }

    /**
     * Returns the list of incoming constraints, creating it if not yet done before.
     * 
     * @return the incoming constraints list of the node group
     */
    public List<NodeGroup> getIncomingConstraints() {
        if (incomingConstraints == null) {
            incomingConstraints = new LinkedList<NodeGroup>();
        }
        return incomingConstraints;
    }
    
    /**
     * Reset the list of incoming constraints to {@code null}.
     */
    public void resetIncomingConstraints() {
        incomingConstraints = null;
    }
    
    /**
     * Determine whether there are any incoming constraints.
     * 
     * @return true if there are incoming constraints
     */
    public boolean hasIncomingConstraints() {
        return incomingConstraints != null && incomingConstraints.size() > 0;
    }
    
    /**
     * Returns the array of nodes.
     * 
     * @return the contained nodes of the node group
     */
    public LNode[] getNodes() {
        return nodes;
    }
    
    /**
     * Returns the contained node. This may only be used for node groups with exactly one node.
     * 
     * @return the contained node
     */
    public LNode getNode() {
        assert nodes.length == 1;
        return nodes[0];
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final NodeGroup other) {
        if (this.barycenter != null && other.barycenter != null) {
            return barycenter.compareTo(other.barycenter);
        } else if (this.barycenter != null) {
            return -1;
        } else if (other.barycenter != null) {
            return 1;
        }
        return 0;
    }
    
}
