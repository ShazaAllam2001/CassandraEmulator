package clientServer;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

public class Server2 {
    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        for(int j=0; j< config.getNumNodes(); j++) {
            if(j!=1) {
                //StartServerThreadHandler serverThread = new StartServerThreadHandler(config.getTCPports()[j],config,1);
                //Thread thread = new Thread(serverThread);
                //thread.start();
                ServerStart.startServer(config.getTCPports()[j],config,1);
            }
        }

    }
}
