package helpingTools.yaml;

public class Configuration {
    private int numNodes;
    private int[] TCPports;
    private Quorum quorum;
    private int replication;

    // getters and setters
    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public int[] getTCPports() {
        return TCPports;
    }

    public void setTCPports(int[] TCPports) {
        this.TCPports = TCPports;
    }

    public Quorum getQuroum() {
        return quorum;
    }

    public void setQuroum(Quorum quorum) {
        this.quorum = quorum;
    }

    public int getReplication() {
        return replication;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }
}


