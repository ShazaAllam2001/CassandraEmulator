package clientServer;

import helpingTools.lsmTree.model.LSMTree;
import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

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

    public static Socket startServer(int port, int i) throws IOException {
        Configuration config = YamlTool.readYaml("config.yaml");
        LSMTree tree = new LSMTree("server_" + i + "_Tree", config.getStoreThreshold(), 5);

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
                sendMessage = ClientCommand.executeCommand(parsedCommand, tree);
                out.println(sendMessage); // send to coordinator
            }
        }

        return socket;
    }

}
