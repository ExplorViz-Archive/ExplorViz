/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klay.layered.intermediate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.kiml.options.LabelSide;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.EdgeAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.GraphAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.GraphElementAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.LabelAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.NodeAdapter;
import de.cau.cs.kieler.kiml.util.adapters.GraphAdapters.PortAdapter;
import de.cau.cs.kieler.kiml.util.nodespacing.Spacing.Insets;
import de.cau.cs.kieler.kiml.util.nodespacing.Spacing.Margins;
import de.cau.cs.kieler.klay.layered.graph.LEdge;
import de.cau.cs.kieler.klay.layered.graph.LGraph;
import de.cau.cs.kieler.klay.layered.graph.LInsets;
import de.cau.cs.kieler.klay.layered.graph.LLabel;
import de.cau.cs.kieler.klay.layered.graph.LNode;
import de.cau.cs.kieler.klay.layered.graph.LPort;
import de.cau.cs.kieler.klay.layered.graph.LShape;
import de.cau.cs.kieler.klay.layered.graph.Layer;
import de.cau.cs.kieler.klay.layered.properties.InternalProperties;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * @author uru
 */
public final class LGraphAdapters {

    private LGraphAdapters() {
    }
    
    /**
     * @param graph
     *            the graph that should be wrapped in an adapter
     * @return an {@link LGraphAdapter} for the passed graph.
     */
    public static LGraphAdapter adapt(final LGraph graph) {
        return new LGraphAdapter(graph);
    }
    
    /**
     * .
     */
    private abstract static class AbstractLGraphAdapter<T extends LShape> implements
            GraphElementAdapter<T> {

        // CHECKSTYLEOFF VisibilityModifier
        /** The internal element. */
        protected T element;
        // CHECKSTYLEON VisibilityModifier
        
        /**
         * .
         */
        public AbstractLGraphAdapter(final T element) {
            this.element = element;
        }

        /**
         * {@inheritDoc}
         */
        public KVector getSize() {
            return element.getSize();
        }

        /**
         * {@inheritDoc}
         */
        public void setSize(final KVector size) {
            element.getSize().x = size.x;
            element.getSize().y = size.y;
        }

        /**
         * {@inheritDoc}
         */
        public KVector getPosition() {
            return element.getPosition();
        }

        /**
         * {@inheritDoc}
         */
        public void setPosition(final KVector pos) {
            element.getPosition().x = pos.x;
            element.getPosition().y = pos.y;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public <P> P getProperty(final IProperty<P> prop) {

            // handle some special cases
            if (prop.equals(LayoutOptions.SPACING)) {
                // cast is ok, as both properties are Floats
                return (P) element.getProperty(Properties.OBJ_SPACING);
            } else if (prop.equals(LayoutOptions.OFFSET)) {
                return (P) element.getProperty(InternalProperties.OFFSET);
            }

            return element.getProperty(prop);
        }

    }

    /**
     * .
     */
    public static final class LGraphAdapter implements GraphAdapter<LGraph> {

        // CHECKSTYLEOFF VisibilityModifier
        /** The internal element. */
        protected LGraph element;
        // CHECKSTYLEON VisibilityModifier

        /**
         * @param element
         *            .
         */
        private LGraphAdapter(final LGraph element) {
            this.element = element;
        }

        /**
         * {@inheritDoc}
         */
        public KVector getSize() {
            return element.getSize();
        }

        /**
         * {@inheritDoc}
         */
        public void setSize(final KVector size) {
            element.getSize().x = size.x;
            element.getSize().y = size.y;
        }

        /**
         * {@inheritDoc}
         */
        public KVector getPosition() {
            throw new UnsupportedOperationException("Not supported by LGraph");
        }

        /**
         * {@inheritDoc}
         */
        public void setPosition(final KVector pos) {
            throw new UnsupportedOperationException("Not supported by LGraph");
        }

        /**
         * {@inheritDoc}
         */
        public <P> P getProperty(final IProperty<P> prop) {
            return element.getProperty(prop);
        }

        /**
         * {@inheritDoc}
         */
        public Iterable<NodeAdapter<?>> getNodes() {
            List<NodeAdapter<?>> nodeAdapter = Lists.newLinkedList();
            for (Layer l : element.getLayers()) {
                for (LNode n : l.getNodes()) {
                    nodeAdapter.add(new LNodeAdapter(n));
                }
            }
            return nodeAdapter;
        }

    }

    /**
     * .
     */
    static final class LNodeAdapter extends AbstractLGraphAdapter<LNode> implements NodeAdapter<LNode> {

        /**
         * @param element
         */
        public LNodeAdapter(final LNode element) {
            super(element);
        }

        /**
         * {@inheritDoc}
         */
        public Iterable<LabelAdapter<?>> getLabels() {
            List<LabelAdapter<?>> labelAdapters = Lists.newLinkedList();
            for (LLabel l : element.getLabels()) {
                labelAdapters.add(new LLabelAdapter(l));
            }
            return labelAdapters;
        }

        /**
         * {@inheritDoc}
         */
        public Iterable<PortAdapter<?>> getPorts() {
            List<PortAdapter<?>> portAdapters = Lists.newLinkedList();
            for (LPort p : element.getPorts()) {
                portAdapters.add(new LPortAdapter(p));
            }
            return portAdapters;
        }
        
        /**
         * {@inheritDoc}
         */
        public Iterable<EdgeAdapter<?>> getIncomingEdges() {
            List<EdgeAdapter<?>> edgeAdapters = Lists.newLinkedList();
            for (LEdge l : element.getIncomingEdges()) {
                edgeAdapters.add(new LEdgeAdapter(l));
            }
            return edgeAdapters;
        }

        /**
         * {@inheritDoc}
         */
        public Iterable<EdgeAdapter<?>> getOutgoingEdges() {
            List<EdgeAdapter<?>> edgeAdapters = Lists.newLinkedList();
            for (LEdge l : element.getOutgoingEdges()) {
                edgeAdapters.add(new LEdgeAdapter(l));
            }
            return edgeAdapters;
        }

        /**
         * {@inheritDoc}
         */
        public void sortPortList() {
            sortPortList(DEFAULT_PORTLIST_SORTER);
        }
        
        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public void sortPortList(final Comparator<?> comparator) {
            if (element.getProperty(LayoutOptions.PORT_CONSTRAINTS).isOrderFixed()) {
                // We need to sort the port list accordingly
                Collections.sort(element.getPorts(), (Comparator<LPort>) comparator);
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean isCompoundNode() {
            return element.getProperty(InternalProperties.COMPOUND_NODE);
        }

        /**
         * {@inheritDoc}
         */
        public Insets getInsets() {
            LInsets linsets = element.getInsets();
            return new Insets(linsets.top, linsets.left, linsets.bottom, linsets.right);
        }

        /**
         * {@inheritDoc}
         */
        public void setInsets(final Insets insets) {
            element.getInsets().left = insets.left;
            element.getInsets().top = insets.top;
            element.getInsets().right = insets.right;
            element.getInsets().bottom = insets.bottom;
        }

        /**
         * {@inheritDoc}
         */
        public Margins getMargin() {
            LInsets lmargins = element.getMargin();
            return new Margins(lmargins.top, lmargins.left, lmargins.bottom, lmargins.right);
        }

        /**
         * {@inheritDoc}
         */
        public void setMargin(final Margins margin) {
            element.getMargin().left = margin.left;
            element.getMargin().top = margin.top;
            element.getMargin().right = margin.right;
            element.getMargin().bottom = margin.bottom;
        }
    }

    /**
     * .
     */
    static final class LPortAdapter extends AbstractLGraphAdapter<LPort> implements PortAdapter<LPort> {

        /**
         * @param element
         */
        public LPortAdapter(final LPort element) {
            super(element);
        }

        /**
         * {@inheritDoc}
         */
        public PortSide getSide() {
            return element.getSide();
        }

        /**
         * {@inheritDoc}
         */
        public Iterable<LabelAdapter<?>> getLabels() {
            List<LabelAdapter<?>> labelAdapters =
                    Lists.newArrayListWithExpectedSize(element.getLabels().size());
            for (LLabel l : element.getLabels()) {
                labelAdapters.add(new LLabelAdapter(l));
            }
            return labelAdapters;
        }

        /**
         * {@inheritDoc}
         */
        public Margins getMargin() {
            LInsets lmargins = element.getMargin();
            return new Margins(lmargins.top, lmargins.left, lmargins.bottom, lmargins.right);
        }

        /**
         * {@inheritDoc}
         */
        public void setMargin(final Margins margin) {
            element.getMargin().left = margin.left;
            element.getMargin().top = margin.top;
            element.getMargin().right = margin.right;
            element.getMargin().bottom = margin.bottom;
        }

        /**
         * {@inheritDoc}
         */
        public Iterable<EdgeAdapter<?>> getIncomingEdges() {
            List<EdgeAdapter<?>> edgeAdapters = Lists.newLinkedList();
            for (LEdge e : element.getIncomingEdges()) {
                edgeAdapters.add(new LEdgeAdapter(e));
            }
            return edgeAdapters;
        }

        /**
         * {@inheritDoc}
         */
        public Iterable<EdgeAdapter<?>> getOutgoingEdges() {
            List<EdgeAdapter<?>> edgeAdapters = Lists.newLinkedList();
            for (LEdge e : element.getOutgoingEdges()) {
                edgeAdapters.add(new LEdgeAdapter(e));
            }
            return edgeAdapters;
        }
    }

    /**
     * .
     */
    static final class LLabelAdapter extends AbstractLGraphAdapter<LLabel> implements
            LabelAdapter<LLabel> {

        /**
         * @param element
         */
        public LLabelAdapter(final LLabel element) {
            super(element);
        }

        /**
         * {@inheritDoc}
         */
        public LabelSide getSide() {
            return element.getProperty(LayoutOptions.LABEL_SIDE);
        }

    }

    /**
     * .
     */
    static final class LEdgeAdapter implements EdgeAdapter<LEdge> {

        private LEdge e;

        public LEdgeAdapter(final LEdge edge) {
            this.e = edge;
        }

        /**
         * {@inheritDoc}
         */
        public Iterable<LabelAdapter<?>> getLabels() {
            List<LabelAdapter<?>> labelAdapters =
                    Lists.newArrayListWithExpectedSize(e.getLabels().size());
            for (LLabel l : e.getLabels()) {
                labelAdapters.add(new LLabelAdapter(l));
            }
            return labelAdapters;
        }
    }

    /**
     * A comparer for ports. Ports are sorted by side (north, east, south, west) in
     * clockwise order, beginning at the top left corner.
     */
    public static final PortComparator DEFAULT_PORTLIST_SORTER = new PortComparator();
    
    /**
     * A comparer for ports. Ports are sorted by side (north, east, south, west) in
     * clockwise order, beginning at the top left corner.
     */
    public static class PortComparator implements Comparator<LPort> {

        /**
         * {@inheritDoc}
         */
        public int compare(final LPort port1, final LPort port2) {
            int ordinalDifference = port1.getSide().ordinal() - port2.getSide().ordinal();
            
            // Sort by side first
            if (ordinalDifference != 0) {
                return ordinalDifference;
            }
            
            // In case of equal sides, sort by port index property
            Integer index1 = port1.getProperty(LayoutOptions.PORT_INDEX);
            Integer index2 = port2.getProperty(LayoutOptions.PORT_INDEX);
            if (index1 != null && index2 != null) {
                int indexDifference = index1 - index2;
                if (indexDifference != 0) {
                    return indexDifference;
                }
            }
            
            // In case of equal index, sort by position
            switch (port1.getSide()) {
            case NORTH:
                // Compare x coordinates
                return Double.compare(port1.getPosition().x, port2.getPosition().x);
            
            case EAST:
                // Compare y coordinates
                return Double.compare(port1.getPosition().y, port2.getPosition().y);
            
            case SOUTH:
                // Compare x coordinates in reversed order
                return Double.compare(port2.getPosition().x, port1.getPosition().x);
            
            case WEST:
                // Compare y coordinates in reversed order
                return Double.compare(port2.getPosition().y, port1.getPosition().y);
                
            default:
                // Port sides should not be undefined
                throw new IllegalStateException("Port side is undefined");
            }
        }
        
    }
}
