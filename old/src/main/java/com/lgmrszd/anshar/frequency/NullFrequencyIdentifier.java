package com.lgmrszd.anshar.frequency;

public final class NullFrequencyIdentifier implements IFrequencyIdentifier {
    
    // disallow creation, require singleton instance with get()
    private NullFrequencyIdentifier() {}

    @Override
    public boolean isValid() { return false; }

    private static final NullFrequencyIdentifier INSTANCE = new NullFrequencyIdentifier();
    public static NullFrequencyIdentifier get() { return INSTANCE; }

    @Override
    public String toString() {
        return "NullFreq";
    }
}