package clientServer;

import helpingTools.lsmTree.model.LSMTree;
import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientThreadHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final Server coordinator;
    private Socket socket;
    private final int i;

    public ClientThreadHandler(ServerSocket serverSocket, Server coordinator, int i) {
        this.serverSocket = serverSocket;
        this.coordinator = coordinator;
        this.i = i;
    }

    @Override
    public void run() {
        try {
            //Configuration config = YamlTool.readYaml("config.yaml");
            //LSMTree tree = new LSMTree("server_" + i + "_Tree", config.getStoreThreshold(), 5);

            this.socket = serverSocket.accept();
            System.out.println("Client accepted");

            // sending to client (pwrite object)
            coordinator.out = new PrintWriter(socket.getOutputStream(), true);

            // receiving from server (receiveRead  object)
            coordinator.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String receiveMessage, sendMessage;
            while(true) {
                receiveMessage = coordinator.in.readLine();
                if (receiveMessage == "Exit") {
                    //close connection
                    System.out.println("Closing connection");
                    socket.close();
                    coordinator.out.close();
                    coordinator.in.close();
                    break;
                }

                if(receiveMessage != null) {
                    System.out.println("Server port " + coordinator.port + ": Received message from client = " + receiveMessage);
                    coordinator.servers.get(0).out.println(receiveMessage);
                    // parse & execute command
                    /*List<String> parsedCommand = ClientCommand.parseCommand(receiveMessage);
                    // get nums of servers associated with the key
                    if(!parsedCommand.get(0).equals("Invalid Command!")) {
                        List<Integer> serversPorts = coordinator.consistentHashing.getServers(parsedCommand.get(1));
                        // if the key exists in the coordinator
                        if(serversPorts.contains(config.getTCPports()[config.getCoordinatorIdx()])) {
                            sendMessage = ClientCommand.executeCommand(parsedCommand, tree);
                            coordinator.out.println(sendMessage);
                            coordinator.out.flush();
                        }
                        else {
                            int portToAsk = serversPorts.get(0);
                            // send request to the first server that has the key
                            for(int i=0; i<coordinator.servers.size(); i++) {
                                if(portToAsk == coordinator.servers.get(i).port) {
                                    coordinator.servers.get(i).out.println(receiveMessage);
                                    break;
                                }
                            }
                        }
                    }*/ /*else {
                        sendMessage = "Invalid Command!";
                        coordinator.out.println(sendMessage);
                        coordinator.out.flush();
                    }*/
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (coordinator.out != null) {
                    coordinator.out.close();
                }
                if (coordinator.in != null) {
                    coordinator.in.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
