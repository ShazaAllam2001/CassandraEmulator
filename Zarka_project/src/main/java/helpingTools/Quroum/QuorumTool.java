package helpingTools.Quroum;

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
        // if counter reaches Quroum, return
        if(Acknowledged == config.getQuorum().getWrite()) {
            return true;
        }
        return false;
    }

    public boolean readQuorum(Server[] servers) {
        for(int i=0; i<servers.length; i++) {
            // read data from all replication nodes

            for(int j=0; j<config.getQuorum().getRead(); j++) {

            }
        }
        // check that all that the number of nodes with the latest version is equal to the read Quorum
        // if true then return
        return false;
    }
}
