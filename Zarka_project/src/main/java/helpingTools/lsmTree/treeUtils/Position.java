package helpingTools.lsmTree.treeUtils;

public class Position {

    private long start;

    private long len;

    public Position(long start, long len) {
        this.start = start;
        this.len = len;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getLen() {
        return len;
    }

    public void setLen(long len) {
        this.len = len;
    }
}
