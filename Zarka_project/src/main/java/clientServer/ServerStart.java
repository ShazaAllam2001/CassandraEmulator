package clientServer;

import helpingTools.lsmTree.model.LSMTree;
import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static helpingTools.lsmTree.model.LSMTree.dirTree;

public class ServerStart {

    public static Server[] startCluster(Configuration config) throws IOException {
        Server[] servers = new Server[config.getNumNodes()];
        // create servers
        for (int i=0; i<config.getNumNodes(); i++) {
            servers[i] = new Server(config.getTCPports()[i],config);
        }
        // connect servers together
        for(int i=0; i<config.getNumNodes(); i++) {
            for(int j=0; j<config.getNumNodes(); j++) {
                if(i!=j) {
                    servers[i].connectToServer(servers[j]);
                }
            }
        }
        // connect servers to the client
        for(int i=0; i<config.getNumNodes(); i++) {
            servers[i].connectToClient(i);
        }

        return servers;
    }

    public static Socket startServer(int port, Configuration config, int i) throws IOException {
        List<LSMTree> trees = new ArrayList<>();

        // make directory for the node
        String treeName = "server_" + i + "_Tree";
        File theDir = new File(dirTree + treeName + "\\");
        if (!theDir.exists()){
            theDir.mkdirs();
        }
        // create V LSM trees
        for(int j=0 ; j<config.getvNodes(); j++) {
            trees.add(new LSMTree(treeName + "\\" + "virtual_" + j, config.getStoreThreshold(), config.getIndexRange()));
        }

        // establish a connection by providing host and port number
        Socket socket = new Socket("127.0.0.1", port);

        // writing to server
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // reading from server
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String receiveMessage, sendMessage;
        while(true) {
            receiveMessage = in.readLine(); // receive from coordinator
            if(receiveMessage == "Exit") {
                //close connection
                System.out.println("Closing connection");
                socket.close();
                out.close();
                in.close();
                break;
            }
            if(receiveMessage != null) {
                System.out.println("Received message from server port " + port + " = " + receiveMessage);
                // parse & execute command
                List<String> parsedCommand = ClientCommand.parseCommand(receiveMessage);
                int virtualNode = Integer.parseInt(parsedCommand.get(parsedCommand.size()-1));
                sendMessage = ClientCommand.executeCommand(parsedCommand, trees.get(virtualNode));
                out.println(sendMessage); // send to coordinator
            }
        }

        return socket;
    }

}
