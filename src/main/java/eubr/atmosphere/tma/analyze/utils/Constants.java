package eubr.atmosphere.tma.analyze.utils;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }

    public static final int cpuDescriptionId = 27;
    public static final int memoryDescriptionId = 28;

    public static final int responseTimeDescriptionId = 29; // mean response time (s) of the last second
    public static final int throughputDescriptionId = 30; // throughput (req/s) of the last second

    public static final int demandDescriptionId = 58; // expected demand of requests per second
    public static final int rateRequestUnderContractedDescriptionId = 59; // rate of requests under contracted

    public static final int nodeId = 11; // worker node

    // Metrics related to the node
    public static final Double maxCPU = 4165.0;
    public static final Double maxMemory = 7966.0;
}
