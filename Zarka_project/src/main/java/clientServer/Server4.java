package clientServer;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.net.Socket;

public class Server4 {
    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        Socket server = ServerStart.startServer(config.getTCPports()[0]);

    }
}
