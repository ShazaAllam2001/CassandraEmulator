package helpingTools.ConsistentHashing;

import com.google.common.base.Charsets;
import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import com.google.common.hash.*;

public class ConsistentHashing {
    private Configuration config;
    private TreeMap<Integer,String> nodePlaces;
    private HashFunction hashFunction = Hashing.murmur3_32_fixed();

    public ConsistentHashing(Configuration config) {
        this.config = config;
        this.nodePlaces = new TreeMap<>();
        this.putNodesOnPlace();
    }

    private void putNodesOnPlace() {
        // for each node, put v number of nodes
        for(int i=0; i<config.getNumNodes(); i++) {
            for(int j=0; j<config.getvNodes(); j++) {
                String name = i + "_" + j;
                HashCode hc = hashFunction.newHasher().putString(name, Charsets.UTF_8).hash();
                Integer token = hc.asInt();
                nodePlaces.put(token, name);
            }
        }
    }

    public List<int[]> getServers(String key) {
        List<int[]> servers = new ArrayList<>();
        HashFunction hashFunction = Hashing.murmur3_32_fixed();
        HashCode hc = hashFunction.newHasher().putString(key, Charsets.UTF_8).hash();
        Integer nodeKey = hc.asInt(); // token of the given key
        // find the key of the node with the next higher key
        nodeKey = nodePlaces.higherKey(nodeKey);
        // if there is no node with higher key token, get the lowest key
        if(nodeKey == null) {
            nodeKey = nodePlaces.firstKey();
        }
        // get the name of that node
        String name = nodePlaces.get(nodeKey);
        // parse the name to get the node number and virtual number
        String[] splitName = name.split("_", 3);
        int serverNo = Integer.parseInt(splitName[0]);
        int serverPort = config.getTCPports()[serverNo];
        int virtualNo = Integer.parseInt(splitName[1]);
        int[] serverVirtual = new int[] {serverPort, virtualNo};
        servers.add(serverVirtual);
        // get all server with that number and (RF-1) servers
        servers.addAll(getReplicas(serverNo,virtualNo));

        return servers;
    }

    public List<int[]> getReplicas(int serverNo, int virtualNo) {
        List<int[]> replicas = new ArrayList<>();
        int replicaServerNo, replicaServerPort, replicaVirtualNo;
        // get all (RF-1) servers
        for (int i=0; i<config.getReplication()-1; i++) {
            // parse the name to get the node number and virtual number
            String replica = config.getReplicas()[serverNo].getVirtual(virtualNo)[i];
            String[] splitName = replica.split("_", 3);
            replicaServerNo = Integer.parseInt(splitName[0]);
            replicaServerPort = config.getTCPports()[replicaServerNo];
            replicaVirtualNo = Integer.parseInt(splitName[1]);
            int[] serverVirtual = new int[] { replicaServerPort, replicaVirtualNo };
            replicas.add(serverVirtual);
        }
        return replicas;
    }

    public int[][] addNode() throws FileNotFoundException {
        // update config file
        config.setNumNodes(config.getNumNodes() + 1);
        YamlTool.writeYaml("config.yaml", config);

        int[][] servers = new int[config.getvNodes()][4];
        Integer nodeKey;
        for(int i=0; i<config.getvNodes(); i++) {
            // generate token for the new node
            String name = config.getNumNodes() + "_" + i;
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
            String[] splitName = name.split("_", 3);
            int serverNo = Integer.parseInt(splitName[0]);
            int virtualNo = Integer.parseInt(splitName[1]);
            servers[i][0] = config.getTCPports()[serverNo];
            servers[i][1] = virtualNo;

            // find the key of the node with the next higher key
            nodeKey = nodePlaces.higherKey(token.hashCode());
            if(nodeKey == null) {
                nodeKey = nodePlaces.firstKey();
            }
            // get the name of that node
            name = nodePlaces.get(nodeKey);
            // parse the name to get the node number
            splitName = name.split("_", 3);
            serverNo = Integer.parseInt(splitName[0]);
            virtualNo = Integer.parseInt(splitName[1]);
            servers[i][2] = config.getTCPports()[serverNo];
            servers[i][3] = virtualNo;
        }

        return servers;
    }
}
