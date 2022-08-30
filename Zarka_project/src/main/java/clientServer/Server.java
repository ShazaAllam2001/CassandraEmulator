package clientServer;

import helpingTools.lsmTree.model.LSMTree;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public ServerSocket serverSocket;
    public Socket socket;
    public final int port;
    public List<Server> servers = new ArrayList<>();
    public PrintWriter out;
    public BufferedReader in;
    public final LSMTree tree;

    public Server(int port, LSMTree tree) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true); // For being able to use multi-servers
        this.tree = tree;
        System.out.println("Server started");
    }

    public void connectToClient() throws IOException {
        this.socket = serverSocket.accept();
        System.out.println("Client accepted");

        ClientThreadHandler serverThread = new ClientThreadHandler(socket, this);
        Thread thread = new Thread(serverThread);
        thread.start();
    }

    public void connectToServer(Server server) throws IOException {
        // We connect with other servers as Clients
        this.servers.add(server);
        Socket socketServer = serverSocket.accept();
        System.out.println("New Server Connected");

        ServerThreadHandler serverThread = new ServerThreadHandler(socketServer, server, this);
        Thread thread = new Thread(serverThread);
        thread.start();
    }

}