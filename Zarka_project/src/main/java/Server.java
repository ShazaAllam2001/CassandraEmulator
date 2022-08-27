import helpingTools.lsmTree.model.LSMTree;
import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public ServerSocket serverSocket;
    public Socket socket;
    public List<Server> servers;
    public final int port;
    public PrintWriter pwrite;
    public BufferedReader receiveRead;
    public final LSMTree tree;

    public Server(int port, LSMTree tree) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true); // For being able to use multi-servers
        this.servers = new ArrayList<>();
        this.tree = tree;
        System.out.println("Server started");
    }

    public void connectToClient() throws IOException {
        this.socket = serverSocket.accept();
        System.out.println("Client accepted");

        // sending to client (pwrite object)
        OutputStream ostream = socket.getOutputStream();
        pwrite = new PrintWriter(ostream, true);

        // receiving from server (receiveRead  object)
        InputStream istream = socket.getInputStream();
        receiveRead = new BufferedReader(new InputStreamReader(istream));

        String receiveMessage, sendMessage;
        while(true) {
            receiveMessage = receiveRead.readLine();
            if (receiveMessage == "Exit") {
                //close connection
                System.out.println("Closing connection");
                socket.close();
                pwrite.close();
                receiveRead.close();
                break;
            }
            if(receiveMessage  != null) {
                System.out.println(receiveMessage);
            }
            // parse & execute command
            List<String> parsedCommand = ClientCommand.parseCommand(receiveMessage);
            sendMessage = ClientCommand.executeCommand(parsedCommand, tree);
            pwrite.println(sendMessage);
            pwrite.flush();
        }
    }

    public void connectToServer(Server server) throws IOException {
        // create a new thread object for the server
        ServerThread serverSock = new ServerThread(server);

        // This thread will handle the server separately
        Thread serverThread = new Thread(serverSock);
        serverThread.start();

        // socket object for the client
        Socket coordinator = new Socket("127.0.0.2", server.port);
        this.servers.add(server);

        // sending to client (pwrite object)
        OutputStream ostream = coordinator.getOutputStream();
        server.pwrite = new PrintWriter(ostream, true);

        // receiving from server (receiveRead  object)
        InputStream istream = coordinator.getInputStream();
        server.receiveRead = new BufferedReader(new InputStreamReader(istream));
    }

    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        Server[] servers = ServerStart.startNServers(config);

    }
}