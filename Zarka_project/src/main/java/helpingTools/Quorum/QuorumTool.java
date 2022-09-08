package helpingTools.Quorum;

import clientServer.ClientCommand;
import clientServer.Server;
import helpingTools.lsmTree.model.LSMTree;
import helpingTools.yaml.Configuration;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuorumTool {
    private final Configuration config;
    private final Server coordinator;
    private List<int[]> serversPorts;
    private List<String> parsedCommand;
    private List<LSMTree> coordinatorTrees;
    private Map<String,Integer> resultMap = new HashMap<>();
    private Map<String,Long> resultVersionMap = new HashMap<>();
    private short ReplicationCount = 0;
    private short Acknowledged = 0;
    private boolean ReadValid = false;

    public QuorumTool(Configuration config, Server coordinator) {
        this.config = config;
        this.coordinator = coordinator;
    }

    public void sendRequests(List<int[]> serversPorts, List<String> parsedCommand, String receiveMessage, List<LSMTree> trees) {
        this.serversPorts = serversPorts;
        this.parsedCommand = parsedCommand;
        this.coordinatorTrees = trees;

        resultMap = new HashMap<>();
        resultVersionMap = new HashMap<>();
        ReadValid = false;
        ReplicationCount = 0;
        Acknowledged = 0;

        for(int[] serverToAsk : serversPorts) {
            int portToAsk = serverToAsk[0];
            int virtualNode = serverToAsk[1];
            System.out.println("Server chosen to be asked " + portToAsk + " with v = " + virtualNode);
            if(portToAsk == coordinator.port) {
                ReplicationCount++;
                String sendMessage = ClientCommand.executeCommand(parsedCommand, trees.get(virtualNode));
                String[] splitMessage = sendMessage.split("&", 3);
                sendMessage = splitMessage[0];
                // Write query
                if(sendMessage.equals("Added Successfully!")) {
                    Acknowledged++;
                    System.out.println("Write acknowledged from " + Acknowledged);
                    if(Acknowledged == config.getQuorum().getWrite()) {
                        Acknowledged = 0;
                        coordinator.out.println(sendMessage);
                    }
                }
                // Read Query
                else if(sendMessage.contains(":")){
                    if(resultMap.containsKey(sendMessage)) {
                        int readCount = resultMap.get(sendMessage) + 1;
                        if(readCount == config.getQuorum().getRead()) {
                            ReadValid = true;
                            resultMap = new HashMap<>();
                            System.out.println(sendMessage);
                            coordinator.out.println(sendMessage);
                        }
                        resultMap.replace(sendMessage,readCount);
                    } else {
                        resultMap.put(sendMessage,1);
                    }

                    // put version on version map
                    if(splitMessage.length>1) {
                        long version = Long.parseLong(splitMessage[1]);
                        if(resultVersionMap.containsKey(sendMessage)) {
                            if(resultVersionMap.get(sendMessage)>version) {
                                resultVersionMap.replace(sendMessage,version);
                            }
                        } else {
                            resultVersionMap.put(sendMessage,version);
                        }
                    }
                    System.out.println("Read Validity = " + ReadValid);

                    System.out.println("Server port " + portToAsk + ": " + sendMessage);
                }
            }
            else {
                // send request to the first server that has the key
                for(Server server : coordinator.servers) {
                    if(portToAsk == server.port) {
                        server.out.println(receiveMessage + "&" + virtualNode);
                        break;
                    }
                }
            }
        }
    }

    public synchronized String checkQuorum(Server serverRequester, ServerSocket serverSocket, String line) {
        String[] splitMessage = line.split("&",3);
        line = splitMessage[0];
        ReplicationCount++;
        // output the received message from other server (client) to the client
        System.out.println("Server port " + serverSocket.getLocalPort()
                + ": Received message from server " + serverRequester.port + " = " + line);

        // Write query
        if(line.equals("Added Successfully!")) {
            Acknowledged++;
            System.out.println("Write acknowledged from " + Acknowledged);
            if(Acknowledged == config.getQuorum().getWrite()) {
                Acknowledged = 0;
                return line;
            }
        }
        // Read Query
        else if(line.contains(":")){
            if(resultMap.containsKey(line)) {
                int readCount = resultMap.get(line) + 1;
                if(readCount == config.getQuorum().getRead()) {
                    ReadValid = true;
                    resultMap = new HashMap<>();
                    System.out.println(line);
                    return line;
                }
                resultMap.replace(line,readCount);
            }
            else {
                resultMap.put(line,1);
            }

            // put version on version map
            if(splitMessage.length>1) {
                long version = Long.parseLong(splitMessage[1]);
                if(resultVersionMap.containsKey(line)) {
                    if(resultVersionMap.get(line)>version) {
                        resultVersionMap.replace(line,version);
                    }
                } else {
                    resultVersionMap.put(line,version);
                }
            }
            System.out.println("Read Validity = " + ReadValid);
        }

        // return "Read Invalid", if it is a read request which is invalid
        if(!line.equals("Added Successfully!") && ReplicationCount>=config.getReplication()) {
            ReplicationCount = 0;
            if(!ReadValid) {
                readRepair();
                return "Read Invalid!";
            }
        }
        return null;
    }

    public void readRepair() {
        // get newer version
        if(resultVersionMap.size()>0) {
            String repairMessage = "";
            for(String key : resultVersionMap.keySet()) {
                if(resultVersionMap.containsKey(repairMessage)) {
                    if(resultVersionMap.get(repairMessage) < resultVersionMap.get(key)) {
                        repairMessage = key;
                    }
                } else {
                    repairMessage = key;
                }
            }
            String[] splitRepairMessage = repairMessage.split("[: ]+");
            repairMessage = "add(" + splitRepairMessage[0] + "," + splitRepairMessage[1] + ")";

            for(int[] serverToAsk : serversPorts) {
                int portToAsk = serverToAsk[0];
                int virtualNode = serverToAsk[1];

                if(portToAsk == coordinator.port) {
                    ClientCommand.executeCommand(parsedCommand, coordinatorTrees.get(virtualNode));
                }
                else {
                    // send request to the first server that has the key
                    for(Server server : coordinator.servers) {
                        if(portToAsk == server.port) {
                            server.out.println(repairMessage + "&" + virtualNode + "&readRepair");
                            break;
                        }
                    }
                }
            }
        }
    }

}
