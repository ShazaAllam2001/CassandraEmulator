package clientServer;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

public class Server1 {
    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        Server[] servers = ServerStart.startCluster(config);

    }
}
