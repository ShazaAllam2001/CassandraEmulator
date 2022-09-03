package clientServer;

import helpingTools.ConsistentHashing.ConsistentHashing;
import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public ServerSocket serverSocket;
    public final int port;
    public List<Server> servers = new ArrayList<>();
    public PrintWriter out;
    public BufferedReader in;
    public final ConsistentHashing consistentHashing;

    public Server(int port, Configuration config) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true); // For being able to use multi-servers
        this.consistentHashing = new ConsistentHashing(config);
        System.out.println("Server started");
    }

    public void connectToClient(int i) {
        ClientThreadHandler serverThread = new ClientThreadHandler(serverSocket, this, i);
        Thread thread = new Thread(serverThread);
        thread.start();
    }

    public void connectToServer(Server server) {
        // We connect with other servers as Clients
        this.servers.add(server);

        ServerThreadHandler serverThread = new ServerThreadHandler(serverSocket, server, this);
        Thread thread = new Thread(serverThread);
        thread.start();
    }

    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        Server[] servers = ServerStart.startCluster(config);

    }

}