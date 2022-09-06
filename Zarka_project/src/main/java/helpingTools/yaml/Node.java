package helpingTools.yaml;

public class Node {
    private String[][] replicaIndices;

    public String[][] getReplicaIndices() {
        return replicaIndices;
    }

    public void setReplicaIndices(String[][] replicaIndices) {
        this.replicaIndices = replicaIndices;
    }

    public String[] getVirtual(int index) {
        return replicaIndices[index];
    }
}
