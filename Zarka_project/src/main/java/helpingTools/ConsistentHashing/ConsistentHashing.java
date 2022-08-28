package helpingTools.ConsistentHashing;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.FileNotFoundException;
import java.util.TreeMap;

public class ConsistentHashing {
    private final int upperRange = Integer.MAX_VALUE;
    private final int lowerRange = Integer.MIN_VALUE;
    private Configuration config;
    private TreeMap<Integer,String> nodePlaces;

    public ConsistentHashing(Configuration config) {
        this.config = config;
        this.nodePlaces = new TreeMap<>();
        this.putNodesOnPlace();
    }

    private void putNodesOnPlace() {
        for(int i=0; i<config.getNumNodes(); i++) {
            String name = "n_" + i;
            Integer token = name.hashCode();
            nodePlaces.put(token, name);
        }
    }

    public int[] getServers(String key) {
        int[] servers = new int[config.getReplication()];
        // find the key of the node with the next higher key
        Integer nodeKey = nodePlaces.higherKey(key.hashCode());
        // get all server with that number and (RF-1) servers next to it (clockwise)
        for (int i=0; i<config.getReplication(); i++) {
            // get the name of that node
            String name = nodePlaces.get(nodeKey);
            // parse the name to get the node number
            int serverNo = Integer.parseInt(name.split("_", 3)[2]);
            servers[i] = serverNo;
            // get the key of the next higher node
            nodeKey = nodePlaces.higherKey(nodeKey);
        }
        return servers;
    }

    public int[] addNode() throws FileNotFoundException {
        // update config file
        config.setNumNodes(config.getNumNodes() + 1);
        YamlTool.writeYaml("config.yaml", config);

        // generate token for the new node
        String name = "n_" + config.getNumNodes();
        Integer token = name.hashCode();
        nodePlaces.put(token, name);

        // get values of the two partitions we need to rebalance partitions
        int[] servers = new int[2];
        // find the key of the node with the next lower key
        Integer nodeKey = nodePlaces.lowerKey(token.hashCode());
        // get the name of that node
        name = nodePlaces.get(nodeKey);
        // parse the name to get the node number
        int serverNo = Integer.parseInt(name.split("_", 3)[2]);
        servers[0] = serverNo;
        // find the key of the node with the next higher key
        nodeKey = nodePlaces.higherKey(token.hashCode());
        // get the name of that node
        name = nodePlaces.get(nodeKey);
        // parse the name to get the node number
        serverNo = Integer.parseInt(name.split("_", 3)[2]);
        servers[1] = serverNo;

        return servers;

    }
}
