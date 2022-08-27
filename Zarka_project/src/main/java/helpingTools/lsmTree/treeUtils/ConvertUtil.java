package helpingTools.lsmTree.treeUtils;

import com.alibaba.fastjson.JSONObject;

public class ConvertUtil {

    public static final String TYPE = "type";

    public static Command jsonToCommand(JSONObject value) {
        if (value.getString(TYPE).equals(CommandTypeEnum.SET.getNum())) {
            return value.toJavaObject(SetCommand.class);
        } else if (value.getString(TYPE).equals(CommandTypeEnum.RM.getNum())) {
            return value.toJavaObject(RmCommand.class);
        }
        return null;
    }
}
