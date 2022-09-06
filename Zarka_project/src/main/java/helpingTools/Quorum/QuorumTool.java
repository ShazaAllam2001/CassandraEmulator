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
    private int ReplicationCount = 0;
    private Map<String,Integer> resultMap = new HashMap<>();
    private int Acknowledged = 0;
    private boolean ReadValid = false;

    public QuorumTool(Configuration config, Server coordinator) {
        this.config = config;
        this.coordinator = coordinator;
    }

    public void sendRequests(List<int[]> serversPorts, List<String> parsedCommand, String receiveMessage, List<LSMTree> trees) {
        resultMap = new HashMap<>();
        ReadValid = false;

        for(int[] serverToAsk : serversPorts) {
            int portToAsk = serverToAsk[0];
            int virtualNode = serverToAsk[1];
            System.out.println("Server chosen to be asked " + portToAsk + " with v = " + virtualNode);
            if(portToAsk == coordinator.port) {
                ReplicationCount++;
                String sendMessage = ClientCommand.executeCommand(parsedCommand, trees.get(virtualNode));
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
                            coordinator.out.println(sendMessage);
                        }
                        resultMap.replace(sendMessage,readCount);
                    }
                    else {
                        resultMap.put(sendMessage,1);
                    }
                    System.out.println("Server port " + portToAsk + ": " + sendMessage);
                    System.out.println("Read Validity = " + ReadValid);
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

    public void checkQuorum(Server serverRequester, ServerSocket serverSocket) throws IOException {
        String line = serverRequester.in.readLine();
        if(line != null) {
            // output the received message from other server (client) to the client
            System.out.println("Server port " + serverSocket.getLocalPort()
                    + ": Received message from server " + serverRequester.port + " = " + line);

            // Write query
            if(line.equals("Added Successfully!")) {
                Acknowledged++;
                System.out.println("Write acknowledged from " + Acknowledged);
                if(Acknowledged == config.getQuorum().getWrite()) {
                    Acknowledged = 0;
                    coordinator.out.println(line);
                }
            }
            // Read Query
            else if(line.contains(":")){
                ReplicationCount++;
                if(resultMap.containsKey(line)) {
                    int readCount = resultMap.get(line) + 1;
                    if(readCount == config.getQuorum().getRead()) {
                        ReadValid = true;
                        coordinator.out.println(line);
                    }
                    resultMap.replace(line,readCount);
                }
                else {
                    resultMap.put(line,1);
                }
                System.out.println("Read Validity = " + ReadValid);
            }
        }

        // return "Read Invalid", if it is a read request which is invalid
        if(!line.equals("Added Successfully!") && ReplicationCount>=config.getReplication()) {
            ReplicationCount = 0;
            if(!ReadValid) {
                coordinator.out.println("Read Invalid!");
            }
        }
    }

}
