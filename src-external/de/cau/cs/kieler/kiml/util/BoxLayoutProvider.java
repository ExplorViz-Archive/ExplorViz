/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2009 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.AbstractLayoutProvider;
import de.cau.cs.kieler.kiml.klayoutdata.KInsets;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.LayoutOptions;

/**
 * A layout algorithm that does not take edges into account, but treats all nodes as isolated boxes.
 * This is useful for parts of a diagram that consist of objects without connections, such as
 * parallel regions in Statecharts.
 * 
 * @kieler.rating yellow 2012-08-10 review KI-23 by cds, sgu
 * @kieler.design proposed by msp
 * @author msp
 */
public class BoxLayoutProvider extends AbstractLayoutProvider {
    
    /** the layout provider id. */
    public static final String ID = "de.cau.cs.kieler.box";

    /** default value for spacing between boxes. */
    private static final float DEF_SPACING = 15.0f;
    /** default value for aspect ratio. */
    public static final float DEF_ASPECT_RATIO = 1.3f;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doLayout(final KNode layoutNode, final IKielerProgressMonitor progressMonitor) {
        progressMonitor.begin("Box layout", 2);
        KShapeLayout parentLayout = layoutNode.getData(KShapeLayout.class);
        // set option for minimal spacing
        float objSpacing = parentLayout.getProperty(LayoutOptions.SPACING);
        if (objSpacing < 0) {
            objSpacing = DEF_SPACING;
        }
        // set option for border spacing
        float borderSpacing = parentLayout.getProperty(LayoutOptions.BORDER_SPACING);
        if (borderSpacing < 0) {
            borderSpacing = DEF_SPACING;
        }
        // set expand nodes option
        boolean expandNodes = parentLayout.getProperty(LayoutOptions.EXPAND_NODES);
        // set interactive option
        boolean interactive = parentLayout.getProperty(LayoutOptions.INTERACTIVE);

        // sort boxes according to priority and position or size
        List<KNode> sortedBoxes = sort(layoutNode, interactive);
        // place boxes on the plane
        placeBoxes(sortedBoxes, layoutNode, objSpacing, borderSpacing, expandNodes);

        progressMonitor.done();
    }
    

    /**
     * Sorts nodes according to priority and size or position. Nodes with higher priority are
     * put to the start of the list.
     * 
     * @param parentNode parent node
     * @param interactive whether position should be considered instead of size
     * @return sorted list of children
     */
    private List<KNode> sort(final KNode parentNode, final boolean interactive) {
        List<KNode> sortedBoxes = new LinkedList<KNode>(
                parentNode.getChildren());
        
        Collections.sort(sortedBoxes, new Comparator<KNode>() {
            public int compare(final KNode child1, final KNode child2) {
                KShapeLayout layout1 = child1.getData(KShapeLayout.class);
                Integer prio1 = layout1.getProperty(LayoutOptions.PRIORITY);
                if (prio1 == null) {
                    prio1 = 0;
                }
                KShapeLayout layout2 = child2.getData(KShapeLayout.class);
                Integer prio2 = layout2.getProperty(LayoutOptions.PRIORITY);
                if (prio2 == null) {
                    prio2 = 0;
                }
                if (prio1 > prio2) {
                    return -1;
                } else if (prio1 < prio2) {
                    return 1;
                } else {
                    // boxes have same priority - compare their position or size
                    if (interactive) {
                        int c = Float.compare(layout1.getYpos(), layout2.getYpos());
                        if (c != 0) {
                            return c;
                        }
                        c = Float.compare(layout1.getXpos(), layout2.getXpos());
                        if (c != 0) {
                            return c;
                        }
                    }
                    float size1 = layout1.getWidth() * layout1.getHeight();
                    float size2 = layout2.getWidth() * layout2.getHeight();
                    return Float.compare(size1, size2);
                }
            }
        });
        
        return sortedBoxes;
    }

    /**
     * Place the boxes of the given sorted list according to their order in the list.
     * Furthermore, adjust the parent size to fit the bounding box.
     * 
     * @param sortedBoxes sorted list of boxes
     * @param parentNode parent node
     * @param objSpacing minimal spacing between elements
     * @param borderSpacing spacing to the border
     * @param expandNodes if true, the nodes are expanded to fill their parent
     */
    private void placeBoxes(final List<KNode> sortedBoxes, final KNode parentNode,
            final float objSpacing, final float borderSpacing, final boolean expandNodes) {
        KShapeLayout parentLayout = parentNode.getData(KShapeLayout.class);
        KInsets insets = parentLayout.getInsets();
        float minWidth = Math.max(parentLayout.getProperty(LayoutOptions.MIN_WIDTH)
                - insets.getLeft() - insets.getRight(), 0);
        float minHeight = Math.max(parentLayout.getProperty(LayoutOptions.MIN_HEIGHT)
                - insets.getTop() - insets.getBottom(), 0);
        float aspectRatio = parentLayout.getProperty(LayoutOptions.ASPECT_RATIO);
        if (aspectRatio <= 0) {
            aspectRatio = DEF_ASPECT_RATIO;
        }

        // do place the boxes
        KVector parentSize = placeBoxes(sortedBoxes, objSpacing, borderSpacing, minWidth, minHeight,
                expandNodes, aspectRatio);

        // adjust parent size
        float width = insets.getLeft() + (float) parentSize.x + insets.getRight();
        float height = insets.getTop() + (float) parentSize.y + insets.getBottom();
        KimlUtil.resizeNode(parentNode, width, height, false, true);
    }

    /**
     * Place the boxes of the given sorted list according to their order in the list.
     * 
     * @param sortedBoxes sorted list of boxes
     * @param minSpacing minimal spacing between elements
     * @param borderSpacing spacing to the border
     * @param minTotalWidth minimal width of the parent node
     * @param minTotalHeight minimal height of the parent node
     * @param expandNodes if true, the nodes are expanded to fill their parent
     * @param aspectRatio the desired aspect ratio
     * @return the bounding box of the resulting layout
     */
    private KVector placeBoxes(final List<KNode> sortedBoxes, final float minSpacing,
            final float borderSpacing, final float minTotalWidth, final float minTotalHeight,
            final boolean expandNodes, final float aspectRatio) {
        // determine the maximal row width by the maximal box width and the total area
        float maxRowWidth = 0.0f;
        float totalArea = 0.0f;
        for (KNode box : sortedBoxes) {
            KShapeLayout boxLayout = box.getData(KShapeLayout.class);
            KimlUtil.resizeNode(box);
            maxRowWidth = Math.max(maxRowWidth, boxLayout.getWidth());
            totalArea += boxLayout.getWidth() * boxLayout.getHeight();
        }
        maxRowWidth = Math.max(maxRowWidth, (float) Math.sqrt(totalArea) * aspectRatio)
                + borderSpacing;

        // place nodes iteratively into rows
        float xpos = borderSpacing;
        float ypos = borderSpacing;
        float highestBox = 0.0f;
        float broadestRow = 2 * borderSpacing;
        LinkedList<Integer> rowIndices = new LinkedList<Integer>();
        rowIndices.add(Integer.valueOf(0));
        LinkedList<Float> rowHeights = new LinkedList<Float>();
        ListIterator<KNode> boxIter = sortedBoxes.listIterator();
        while (boxIter.hasNext()) {
            KNode box = boxIter.next();
            KShapeLayout boxLayout = box.getData(KShapeLayout.class);
            float width = boxLayout.getWidth();
            float height = boxLayout.getHeight();
            if (xpos + width > maxRowWidth) {
                // place box into the next row
                if (expandNodes) {
                    rowHeights.addLast(Float.valueOf(highestBox));
                    rowIndices.addLast(Integer.valueOf(boxIter.previousIndex()));
                }
                xpos = borderSpacing;
                ypos += highestBox + minSpacing;
                highestBox = 0.0f;
                broadestRow = Math.max(broadestRow, 2 * borderSpacing + width);
            }
            boxLayout.setPos(xpos, ypos);
            broadestRow = Math.max(broadestRow, xpos + width + borderSpacing);
            highestBox = Math.max(highestBox, height);
            xpos += width + minSpacing;
        }
        broadestRow = Math.max(broadestRow, minTotalWidth);
        float totalHeight = ypos + highestBox + borderSpacing;
        if (totalHeight < minTotalHeight) {
            highestBox += minTotalHeight - totalHeight;
            totalHeight = minTotalHeight;
        }

        // expand nodes if required
        if (expandNodes) {
            xpos = borderSpacing;
            boxIter = sortedBoxes.listIterator();
            rowIndices.addLast(Integer.valueOf(sortedBoxes.size()));
            ListIterator<Integer> rowIndexIter = rowIndices.listIterator();
            int nextRowIndex = rowIndexIter.next();
            rowHeights.addLast(Float.valueOf(highestBox));
            ListIterator<Float> rowHeightIter = rowHeights.listIterator();
            float rowHeight = 0.0f;
            while (boxIter.hasNext()) {
                if (boxIter.nextIndex() == nextRowIndex) {
                    xpos = borderSpacing;
                    rowHeight = rowHeightIter.next();
                    nextRowIndex = rowIndexIter.next();
                }
                KNode box = boxIter.next();
                KShapeLayout boxLayout = box.getData(KShapeLayout.class);
                boxLayout.setHeight(rowHeight);
                if (boxIter.nextIndex() == nextRowIndex) {
                    float newWidth = broadestRow - xpos - borderSpacing;
                    float oldWidth = boxLayout.getWidth();
                    boxLayout.setWidth(newWidth);
                    KimlUtil.translate(box, (newWidth - oldWidth) / 2, 0.0f);
                }
                xpos += boxLayout.getWidth() + minSpacing;
            }
        }

        // return parent size
        return new KVector(broadestRow, totalHeight);
    }

}
