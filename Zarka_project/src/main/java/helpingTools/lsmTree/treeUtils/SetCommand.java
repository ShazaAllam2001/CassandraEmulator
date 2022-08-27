package helpingTools.lsmTree.treeUtils;

public class SetCommand extends AbstractCommand {

    private String key;

    private String value;

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SetCommand(String key, String value) {
        super(CommandTypeEnum.SET);
        this.key = key;
        this.value = value;
    }
}
