package helpingTools.Quorum;

import clientServer.Server;
import helpingTools.yaml.Configuration;

public class QuorumTool {
    private Configuration config;

    public QuorumTool(Configuration config) {
        this.config = config;
    }

    public boolean writeQuorum(Server[] servers) {
        int Acknowledged = 0;
        for(int i=0; i<servers.length; i++) {
            // send data to all replication nodes

        }
        // wait for acknowledge (event listener)
        // then increment Acknowledged counter
        // if counter reaches Quorum, return
        if(Acknowledged == config.getQuorum().getWrite()) {
            return true;
        }
        return false;
    }

    public String readQuorum(Server[] servers) {
        for(int i=0; i<servers.length; i++) {
            // read data from all replication nodes

            for(int j=0; j<config.getQuorum().getRead(); j++) {

            }
        }
        // return the latest version of data
        String data = "";

        return data;
    }
}
