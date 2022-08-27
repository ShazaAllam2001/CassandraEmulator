import helpingTools.lsmTree.model.LSMTree;

import java.util.ArrayList;
import java.util.List;

public class ClientCommand {

    public static List<String> parseCommand(String command) {
        List<String> parsedCommand = new ArrayList<>();

        String[] arrOfStr = command.split("[(), ]+", 4);
        if (arrOfStr[0].equals("add")) {
            parsedCommand.add(arrOfStr[0]); // type of command
            parsedCommand.add(arrOfStr[1]); // key
            parsedCommand.add(arrOfStr[2]); // value
        } else if (arrOfStr[0].equals("get")) {
            parsedCommand.add(arrOfStr[0]); // type of command
            parsedCommand.add(arrOfStr[1]); // key
        } else if (arrOfStr[0].equals("Exit")) {
            parsedCommand.add(arrOfStr[0]); // type of command
        } else {
            parsedCommand.add("Invalid Command!");
        }

        return parsedCommand;
    }

    public static String executeCommand(List<String> parsedCommand, LSMTree tree) {
        if (parsedCommand.get(0).equals("add")) {
            tree.set(parsedCommand.get(1), parsedCommand.get(2));
            return "Added Successfully!";
        }
        else if (parsedCommand.get(0).equals("get")) {
            String value = tree.get(parsedCommand.get(1));
            return parsedCommand.get(1) + ": " + value;
        }

        return parsedCommand.get(0);
    }
}
