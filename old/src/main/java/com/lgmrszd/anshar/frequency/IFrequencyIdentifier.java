package com.lgmrszd.anshar.frequency;

import net.minecraft.util.Identifier;

/*
 * Also supports equality comparison to determine network presence.
 */
public sealed interface IFrequencyIdentifier permits NullFrequencyIdentifier, PyramidFrequencyIdentifier {
    boolean isValid();

    default boolean isValidInDim(Identifier dim) {return isValid();}
}
