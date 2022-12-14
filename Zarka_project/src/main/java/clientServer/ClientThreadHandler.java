package clientServer;

import helpingTools.lsmTree.model.LSMTree;
import helpingTools.lsmTree.treeUtils.CompactTask;
import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static helpingTools.lsmTree.model.LSMTree.dirTree;

public class ClientThreadHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final Server coordinator;
    private final int i;
    private Socket socket;
    private List<LSMTree> trees = new ArrayList<>();

    public ClientThreadHandler(ServerSocket serverSocket, Server coordinator, int i) {
        this.serverSocket = serverSocket;
        this.coordinator = coordinator;
        this.i = i;
    }

    @Override
    public void run() {
        try {
            // make directory for the node
            String treeName = "server_" + i + "_Tree";
            File theDir = new File(dirTree + treeName + "\\");
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            // create V LSM trees
            Configuration config = YamlTool.readYaml("config.yaml");
            for(int j=0 ; j<config.getvNodes(); j++) {
                trees.add(new LSMTree(treeName + "\\" + "virtual_" + j, config.getStoreThreshold(), config.getIndexRange()));
            }

            // compact segments
            Timer timer = new Timer();
            timer.schedule(new CompactTask(trees), 0, 300000); // run every 5 minutes

            this.socket = serverSocket.accept();
            System.out.println("Client accepted");

            // sending to client (pwrite object)
            coordinator.out = new PrintWriter(socket.getOutputStream(), true);

            // receiving from server (receiveRead  object)
            coordinator.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String receiveMessage;
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
                    // parse & execute command
                    List<String> parsedCommand = ClientCommand.parseCommand(receiveMessage);
                    // get nums of servers associated with the key
                    if(!parsedCommand.get(0).equals("Invalid Command!")) {
                        List<int[]> serversPorts = coordinator.consistentHashing.getServers(parsedCommand.get(1));
                        coordinator.quorumTool.sendRequests(serversPorts,parsedCommand,receiveMessage,trees);

                    } else {
                        coordinator.out.println("Invalid Command!");
                        coordinator.out.flush();
                    }
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
