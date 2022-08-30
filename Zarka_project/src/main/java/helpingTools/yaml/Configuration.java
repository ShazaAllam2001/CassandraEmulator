package helpingTools.yaml;

public class Configuration {
    private int numNodes;
    private int vNodes;
    private int[] TCPports;
    private Quorum quorum;
    private int replication;
    private int storeThreshold;

    // getters and setters
    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public int getvNodes() { return vNodes; }

    public void setvNodes(int vNodes) { this.vNodes = vNodes; }

    public int[] getTCPports() {
        return TCPports;
    }

    public void setTCPports(int[] TCPports) {
        this.TCPports = TCPports;
    }

    public Quorum getQuorum() {
        return quorum;
    }

    public void setQuorum(Quorum quorum) {
        this.quorum = quorum;
    }

    public int getReplication() {
        return replication;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    public int getStoreThreshold() { return storeThreshold; }

    public void setStoreThreshold(int storeThreshold) { this.storeThreshold = storeThreshold; }
}


