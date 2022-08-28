package clientServer;

import helpingTools.lsmTree.model.LSMTree;
import helpingTools.yaml.Configuration;

import java.io.IOException;

public class ServerStart {

    public static Server[] startNServers(Configuration config) throws IOException {
        Server[] servers = new Server[config.getNumNodes()];
        // create servers & connect server[0] (coordinator) to other servers
        for (int i=0; i<config.getNumNodes(); i++) {
            LSMTree tree = new LSMTree("server_" + i + "_Tree", 5, 5);
            servers[i] = new Server(config.getTCPports()[i],tree);
            if (i!=0) {
                servers[0].connectToServer(servers[i]);
            } else {
                servers[0].connectToClient();
            }
        }

        return servers;
    }

}
