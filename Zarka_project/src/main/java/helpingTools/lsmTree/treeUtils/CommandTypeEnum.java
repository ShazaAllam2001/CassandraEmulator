package helpingTools.lsmTree.treeUtils;

public enum CommandTypeEnum {
    // SET Command
    SET("0"),

    // REMOVE Command
    RM("1");

    private final String num;

    CommandTypeEnum(String num) {
        this.num = num;
    }

    public String getNum() {
        return this.num;
    }
}
