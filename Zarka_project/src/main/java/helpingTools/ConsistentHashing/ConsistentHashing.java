package helpingTools.ConsistentHashing;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.FileNotFoundException;
import java.util.TreeMap;

public class ConsistentHashing {
    private Configuration config;
    private TreeMap<Integer,String> nodePlaces;

    public ConsistentHashing(Configuration config) {
        this.config = config;
        this.nodePlaces = new TreeMap<>();
        this.putNodesOnPlace();
    }

    private void putNodesOnPlace() {
        // for each node, put v number of nodes
        for(int i=0; i<config.getNumNodes(); i++) {
            for(int j=0; j<config.getvNodes(); j++) {
                String name = "n_" + i + "_" + j;
                Integer token = name.hashCode();
                nodePlaces.put(token, name);
            }
        }
    }

    public int[] getServers(String key) {
        int[] servers = new int[config.getReplication()];
        Integer nodeKey = key.hashCode(); // token of the given key
        // get all server with that number and (RF-1) servers next to it (clockwise)
        for (int i=0; i<config.getReplication(); i++) {
            // find the key of the node with the next higher key
            nodeKey = nodePlaces.higherKey(nodeKey);
            // if there is no node with higher key token, get the lowest key
            if(nodeKey == null) {
                nodeKey = nodePlaces.firstKey();
            }
            // get the name of that node
            String name = nodePlaces.get(nodeKey);
            // parse the name to get the node number
            int serverNo = Integer.parseInt(name.split("_", 3)[2]);
            servers[i] = serverNo;
        }
        return servers;
    }

    public int[][] addNode() throws FileNotFoundException {
        // update config file
        config.setNumNodes(config.getNumNodes() + 1);
        YamlTool.writeYaml("config.yaml", config);

        int[][] servers = new int[config.getvNodes()][2];
        Integer nodeKey;
        for(int i=0; i<config.getvNodes(); i++) {
            // generate token for the new node
            String name = "n_" + config.getNumNodes() + "_" + i;
            Integer token = name.hashCode();
            nodePlaces.put(token, name);

            // get values of the two partitions we need to rebalance

            // find the key of the node with the next lower key
            nodeKey = nodePlaces.lowerKey(token.hashCode());
            if(nodeKey == null) {
                nodeKey = nodePlaces.lastKey();
            }
            // get the name of that node
            name = nodePlaces.get(nodeKey);
            // parse the name to get the node number
            int serverNo = Integer.parseInt(name.split("_", 3)[2]);
            servers[i][0] = serverNo;

            // find the key of the node with the next higher key
            nodeKey = nodePlaces.higherKey(token.hashCode());
            if(nodeKey == null) {
                nodeKey = nodePlaces.firstKey();
            }
            // get the name of that node
            name = nodePlaces.get(nodeKey);
            // parse the name to get the node number
            serverNo = Integer.parseInt(name.split("_", 3)[2]);
            servers[i][1] = serverNo;
        }

        return servers;
    }
}
