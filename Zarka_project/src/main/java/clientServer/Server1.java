package clientServer;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.net.Socket;

public class Server1 {
    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        for(int i=0; i< config.getNumNodes(); i++) {
            if(i!=0) {
                Socket server = ServerStart.startServer(config.getTCPports()[i],0);
            }
        }

    }
}
