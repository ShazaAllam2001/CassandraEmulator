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
    private int Acknowledged = 0;
    private boolean ReadValid = false;

    public QuorumTool(Configuration config, Server coordinator) {
        this.config = config;
        this.coordinator = coordinator;
    }

    public void sendRequests(List<int[]> serversPorts, List<String> parsedCommand, String receiveMessage, List<LSMTree> trees) {
        for(int[] serverToAsk : serversPorts) {
            int portToAsk = serverToAsk[0];
            int virtualNode = serverToAsk[1];
            System.out.println("Server chosen to be asked " + portToAsk + " with v = " + virtualNode);
            if(portToAsk == coordinator.port) {
                String sendMessage = ClientCommand.executeCommand(parsedCommand, trees.get(virtualNode));
                coordinator.out.println(sendMessage); // send to client
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
        String line = "";
        Map<String,Integer> resultMap = new HashMap<>();
        ReadValid = false;

        for(int i=0; i<config.getReplication(); i++) {
            line = serverRequester.in.readLine();
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
            System.out.println(Acknowledged + " " + ReadValid);
        }
        // return "Read Invalid", if it is a read request which is invalid
        if(!line.equals("Added Successfully!") && !ReadValid) {
            if(resultMap.get(line) < config.getQuorum().getRead()) {
                coordinator.out.println("Read Invalid!");
            }
        }
    }

}
