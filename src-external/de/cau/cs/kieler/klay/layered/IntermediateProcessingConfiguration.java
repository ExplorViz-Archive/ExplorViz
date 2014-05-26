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
package de.cau.cs.kieler.klay.layered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import de.cau.cs.kieler.klay.layered.intermediate.IntermediateProcessorStrategy;


/**
 * A strategy for intermediate layout processors to be used.
 * 
 * <p>Layout phases use instances of this class to specify which intermediate layout
 * processors they depend on. The layout provider uses this class to keep track of
 * which intermediate layout processors must be inserted into the layout algorithm
 * workflow.</p>
 * 
 * <p>Intermediate processing configurations are created through the two static creation
 * methods:</p>
 * <ol>
 *   <li>
 *     {@link #createEmpty()}
 *     creates a new empty processing configuration.
 *   </li>
 *   <li>
 *     {@link #fromExisting(IntermediateProcessingConfiguration)}
 *     creates a new processing configuration that is a copy of an existing configuration.
 *   </li>
 * </ol>
 * 
 * <p>The different add methods can then be used to add additional processors to a
 * configuration. The methods are designed to support method chaining:</p>
 * <pre>
 * IntermediateProcessingConfiguration.createEmpty()
 *      .addBeforePhase3(LayoutProcessorStrategy.STRANGE_PORT_SIDE_PROCESSOR)
 *      .addAfterPhase5(LayoutProcessorStrategy.SOME_OTHER_PROCESSOR)
 *      .addAfterPhase5(LayoutProcessorStrategy.YET_ANOTHER_PROCESSOR);
 * </pre>
 * 
 * @author cds
 * @kieler.design proposed by cds
 * @kieler.rating proposed yellow by msp
 */
public final class IntermediateProcessingConfiguration {
    
    /** Constant for the processors that should come before phase 1. */
    public static final int BEFORE_PHASE_1 = 0;
    /** Constant for the processors that should come before phase 2. */
    public static final int BEFORE_PHASE_2 = 1;
    /** Constant for the processors that should come before phase 3. */
    public static final int BEFORE_PHASE_3 = 2;
    /** Constant for the processors that should come before phase 4. */
    public static final int BEFORE_PHASE_4 = 3;
    /** Constant for the processors that should come before phase 5. */
    public static final int BEFORE_PHASE_5 = 4;
    /** Constant for the processors that should come after phase 5. */
    public static final int AFTER_PHASE_5 = 5;
    /** How many slots there are for intermediate processing. */
    public static final int INTERMEDIATE_PHASE_SLOTS = 6;
    
    /** Array of sets describing which processors this strategy is composed of. */
    private List<Set<IntermediateProcessorStrategy>> strategy = 
        new ArrayList<Set<IntermediateProcessorStrategy>>(INTERMEDIATE_PHASE_SLOTS);
    
    
    /**
     * Constructs a new empty strategy.
     */
    private IntermediateProcessingConfiguration() {
        for (int i = 0; i < INTERMEDIATE_PHASE_SLOTS; i++) {
            strategy.add(EnumSet.noneOf(IntermediateProcessorStrategy.class));
        }
    }
    
    /**
     * Creates and returns a new empty processing configuration.
     * 
     * @return the created configuration.
     */
    public static IntermediateProcessingConfiguration createEmpty() {
        return new IntermediateProcessingConfiguration();
    }
    
    /**
     * Creates and returns a new processing configuration that is a copy from the given configuration.
     * 
     * @param existing an existing processing configuration.
     * @return a new configuration that equals the existing configuration.
     */
    public static IntermediateProcessingConfiguration fromExisting(
            final IntermediateProcessingConfiguration existing) {
        
        IntermediateProcessingConfiguration configuration = new IntermediateProcessingConfiguration();
        configuration.addAll(existing);
        return configuration;
    }
    
    
    /**
     * Returns the layout processors in the given slot. Modifications of the returned
     * set do not result in modifications of this strategy. Note that iterating over the
     * returned {@code EnumSet} will iterate over the elements in the natural order in
     * which they occur in the original enumeration. That natural order is in turn just
     * the order in which they must be executed to satisfy all dependencies.
     * 
     * @param slotIndex the slot index. Must be {@code >= 0} and
     *                  {@code < INTERMEDIATE_PHASE_SLOTS}.
     * @return the slot's set of layout processors.
     */
    public EnumSet<IntermediateProcessorStrategy> getProcessors(final int slotIndex) {
        if (slotIndex < 0 || slotIndex >= INTERMEDIATE_PHASE_SLOTS) {
            throw new IllegalArgumentException("slotIndex must be >= 0 and < "
                    + INTERMEDIATE_PHASE_SLOTS + ".");
        }
        
        return EnumSet.copyOf(strategy.get(slotIndex));
    }
    
    /**
     * Adds the given intermediate processor to the slot before phase 1, if it's not already in there.
     * 
     * @param processor the processor to add.
     * @return this strategy.
     */
    public IntermediateProcessingConfiguration addBeforePhase1(
            final IntermediateProcessorStrategy processor) {
        
        strategy.get(BEFORE_PHASE_1).add(processor);
        return this;
    }
    
    /**
     * Adds the given intermediate processor to the slot before phase 2, if it's not already in there.
     * 
     * @param processor the processor to add.
     * @return this strategy.
     */
    public IntermediateProcessingConfiguration addBeforePhase2(
            final IntermediateProcessorStrategy processor) {
        
        strategy.get(BEFORE_PHASE_2).add(processor);
        return this;
    }
    
    /**
     * Adds the given intermediate processor to the slot before phase 3, if it's not already in there.
     * 
     * @param processor the processor to add.
     * @return this strategy.
     */
    public IntermediateProcessingConfiguration addBeforePhase3(
            final IntermediateProcessorStrategy processor) {
        
        strategy.get(BEFORE_PHASE_3).add(processor);
        return this;
    }
    
    /**
     * Adds the given intermediate processor to the slot before phase 4, if it's not already in there.
     * 
     * @param processor the processor to add.
     * @return this strategy.
     */
    public IntermediateProcessingConfiguration addBeforePhase4(
            final IntermediateProcessorStrategy processor) {
        
        strategy.get(BEFORE_PHASE_4).add(processor);
        return this;
    }
    
    /**
     * Adds the given intermediate processor to the slot before phase 5, if it's not already in there.
     * 
     * @param processor the processor to add.
     * @return this strategy.
     */
    public IntermediateProcessingConfiguration addBeforePhase5(
            final IntermediateProcessorStrategy processor) {
        
        strategy.get(BEFORE_PHASE_5).add(processor);
        return this;
    }
    
    /**
     * Adds the given intermediate processor to the slot after phase 5, if it's not already in there.
     * 
     * @param processor the processor to add.
     * @return this strategy.
     */
    public IntermediateProcessingConfiguration addAfterPhase5(
            final IntermediateProcessorStrategy processor) {
        
        strategy.get(AFTER_PHASE_5).add(processor);
        return this;
    }
    
    /**
     * Adds all layout processors in the given collection to the given slot, without
     * duplicates.
     * 
     * @param slotIndex the slot index. Must be {@code >= 0} and
     *                  {@code < INTERMEDIATE_PHASE_SLOTS}.
     * @param processors the layout processors to add. May be {@code null}.
     * @return this strategy.
     */
    public IntermediateProcessingConfiguration addAll(final int slotIndex,
            final Collection<IntermediateProcessorStrategy> processors) {
        
        if (slotIndex < 0 || slotIndex >= INTERMEDIATE_PHASE_SLOTS) {
            throw new IllegalArgumentException("slotIndex must be >= 0 and < "
                    + INTERMEDIATE_PHASE_SLOTS + ".");
        }
        
        if (processors != null) {
            strategy.get(slotIndex).addAll(processors);
        }
        
        return this;
    }
    
    /**
     * Adds the items from the given strategy to this strategy. The different intermediate
     * processing slots will have duplicate processors removed.
     * 
     * @param operand the strategy to unify this strategy with. May be {@code null}.
     * @return this strategy.
     */
    public IntermediateProcessingConfiguration addAll(
            final IntermediateProcessingConfiguration operand) {
        
        if (operand != null) {
            for (int i = 0; i < INTERMEDIATE_PHASE_SLOTS; i++) {
                strategy.get(i).addAll(operand.strategy.get(i));
            }
        }
        
        return this;
    }
    
    /**
     * Removes the given layout processor from the given slot.
     * 
     * @param slotIndex the slot index. Must be {@code >= 0} and
     *                  {@code < INTERMEDIATE_PHASE_SLOTS}.
     * @param processor the layout processor to add.
     */
    public void removeLayoutProcessor(final int slotIndex,
            final IntermediateProcessorStrategy processor) {
        
        if (slotIndex < 0 || slotIndex >= INTERMEDIATE_PHASE_SLOTS) {
            throw new IllegalArgumentException("slotIndex must be >= 0 and < "
                    + INTERMEDIATE_PHASE_SLOTS + ".");
        }
        
        strategy.get(slotIndex).remove(processor);
    }
    
    /**
     * Clears this strategy.
     */
    public void clear() {
        for (Set<IntermediateProcessorStrategy> set : strategy) {
            set.clear();
        }
    }
}
