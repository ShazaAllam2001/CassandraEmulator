package clientServer;

import helpingTools.lsmTree.model.LSMTree;
import helpingTools.yaml.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerStart {

    public static Server[] startCluster(Configuration config) throws IOException {
        Server[] servers = new Server[config.getNumNodes()];
        // create servers & connect server[0] (coordinator) to other servers
        for (int i=0; i<config.getNumNodes(); i++) {
            LSMTree tree = new LSMTree("server_" + i + "_Tree", 5, 5);
            servers[i] = new Server(config.getTCPports()[i],tree);
            if (i!=0) {
                servers[0].connectToServer(servers[i]);
            } else {
                servers[0].connectToClient();
            }
        }

        return servers;
    }

    public static Socket startServer(int port) throws IOException {
        // establish a connection by providing host and port number
        Socket socket = new Socket("127.0.0.1", port);

        // writing to server
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // reading from server
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String receiveMessage;
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

                //////
                System.out.println(receiveMessage);
                out.println(receiveMessage); // send to coordinator
            }
        }

        return socket;
    }

}
