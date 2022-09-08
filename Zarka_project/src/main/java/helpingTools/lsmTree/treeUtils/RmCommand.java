package helpingTools.lsmTree.treeUtils;

public class RmCommand extends AbstractCommand {

    private String key;

    private String version;

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public RmCommand(String key) {
        super(CommandTypeEnum.RM);
        this.key = key;
        this.version = String.valueOf(System.currentTimeMillis());
    }
}