package clientServer;

import helpingTools.lsmTree.model.LSMTree;

import java.util.ArrayList;
import java.util.List;

public class ClientCommand {

    public static List<String> parseCommand(String command) {
        List<String> parsedCommand = new ArrayList<>();

        String[] arrOfStr = command.split("[(),& ]+");
        if (arrOfStr[0].equals("add")) {
            parsedCommand.add(arrOfStr[0]); // type of command
            parsedCommand.add(arrOfStr[1]); // key
            parsedCommand.add(arrOfStr[2]); // value
            if(arrOfStr.length > 3) {
                parsedCommand.add(arrOfStr[3]); // virtual node
            }
            if(arrOfStr.length > 4) {
                parsedCommand.add(arrOfStr[4]); // read repair
            }
        } else if (arrOfStr[0].equals("get")) {
            parsedCommand.add(arrOfStr[0]); // type of command
            parsedCommand.add(arrOfStr[1]); // key
            if(arrOfStr.length > 2) {
                parsedCommand.add(arrOfStr[2]); // virtual node
            }
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
            String valueVersion = tree.get(parsedCommand.get(1));
            return parsedCommand.get(1) + ": " + valueVersion;
        }

        return parsedCommand.get(0);
    }
}
