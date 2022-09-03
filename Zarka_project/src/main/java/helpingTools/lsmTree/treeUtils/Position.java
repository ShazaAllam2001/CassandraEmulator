package helpingTools.lsmTree.treeUtils;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return start == position.start && len == position.len;
    }

}
