package clientServer;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

public class Server1 {
    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        for(int j=0; j< config.getNumNodes(); j++) {
            if(j!=0) {
                //StartServerThreadHandler serverThread = new StartServerThreadHandler(config.getTCPports()[j],config,0);
                //Thread thread = new Thread(serverThread);
                //thread.start();
                ServerStart.startServer(config.getTCPports()[j],config,0);
            }
        }

    }
}
