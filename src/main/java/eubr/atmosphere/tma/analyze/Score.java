package eubr.atmosphere.tma.analyze;

public class Score {

    private Double cpuPod;
    private Double memoryPod;
    private Double cpuNode;
    private Double memoryNode;

    public Double getCpuPod() {
        return cpuPod;
    }

    public void setCpuPod(Double cpuPod) {
        this.cpuPod = cpuPod;
    }

    public Double getMemoryPod() {
        return memoryPod;
    }

    public void setMemoryPod(Double memoryPod) {
        this.memoryPod = memoryPod;
    }

    public Double getCpuNode() {
        return cpuNode;
    }

    public void setCpuNode(Double cpuNode) {
        this.cpuNode = cpuNode;
    }

    public Double getMemoryNode() {
        return memoryNode;
    }

    public void setMemoryNode(Double memoryNode) {
        this.memoryNode = memoryNode;
    }
    
    @Override
    public String toString() {
      return "Score [cpuPod: " + this.getCpuPod() + 
    		  ", memoryPod: " + this.getMemoryPod() + 
    		  ", cpuNode: " + this.getCpuNode() + 
    		  ", memoryNode: " + this.getMemoryNode() + "]";
    }
}
