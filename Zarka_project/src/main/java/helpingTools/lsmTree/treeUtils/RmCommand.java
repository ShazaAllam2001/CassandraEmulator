package helpingTools.lsmTree.treeUtils;

public class RmCommand extends AbstractCommand {

    private String key;

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public RmCommand(String key) {
        super(CommandTypeEnum.RM);
        this.key = key;
    }
}