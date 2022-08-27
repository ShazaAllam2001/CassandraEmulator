package helpingTools.lsmTree.treeUtils;

import com.alibaba.fastjson.JSON;

public abstract class AbstractCommand implements Command {

    private CommandTypeEnum type;

    public AbstractCommand(CommandTypeEnum type) {
        this.type = type;
    }

    public CommandTypeEnum getType() {
        return type;
    }

    public void setType(CommandTypeEnum type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
