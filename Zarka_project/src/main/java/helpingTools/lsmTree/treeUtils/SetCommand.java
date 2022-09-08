package helpingTools.lsmTree.treeUtils;

public class SetCommand extends AbstractCommand {

    private String key;

    private String value;

    private String version;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SetCommand(String key, String value) {
        super(CommandTypeEnum.SET);
        this.key = key;
        this.value = value;
        this.version = String.valueOf(System.currentTimeMillis());
    }
}
