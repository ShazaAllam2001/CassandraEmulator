package clientServer;

import helpingTools.lsmTree.model.LSMTree;
import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.net.Socket;

public class Server5 {
    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        LSMTree tree = new LSMTree("server_" + 4 + "_Tree", config.getStoreThreshold(), 5);
        for(int i=0; i< config.getNumNodes(); i++) {
            if(i!=4) {
                Socket server = ServerStart.startServer(config.getTCPports()[i],tree);
            }
        }

    }
}
