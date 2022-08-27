package helpingTools.lsmTree;

import helpingTools.lsmTree.model.LSMTree;

public class TestTree {
    private final static String currDir = "C:\\Users\\Blu-Ray\\Documents\\OOP Assignments\\Zarka_project\\src\\main\\java\\files\\trees\\";

    public static void main(String args[]) {
        LSMTree obj = new LSMTree("server_" + 0 + "_Tree", 5, 5);

        obj.set("10", "test1");
        obj.set("20", "test2");
        obj.set("70", "test7");
        obj.set("40", "test4");
        obj.set("90", "test9");
        obj.set("60", "test6");
        obj.set("50", "test5");
        System.out.println(obj.get("50"));
    }

}
