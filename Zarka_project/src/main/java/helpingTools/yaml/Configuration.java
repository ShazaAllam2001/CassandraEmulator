package helpingTools.yaml;

public class Configuration {
    private int numNodes;
    private int[] TCPports;
    private Quroum quroum;
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

    public Quroum getQuroum() {
        return quroum;
    }

    public void setQuroum(Quroum quroum) {
        this.quroum = quroum;
    }

    public int getReplication() {
        return replication;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }
}


