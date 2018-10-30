package eubr.atmosphere.tma.analyze.utils;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }

    public static final int cpuDescriptionId = 27;
    public static final int memoryDescriptionId = 28;

    public static final int podId = 8; //kafka
    public static final int nodeId = 11; // worker node

    // Metrics related to the node
    public static final Double maxCPU = 4165.0;
    public static final Double maxMemory = 7966.0;
}
