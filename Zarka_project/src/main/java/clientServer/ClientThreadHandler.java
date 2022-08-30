package clientServer;

import helpingTools.lsmTree.model.LSMTree;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientThreadHandler implements Runnable {
    private final Socket socket;
    private final Server server;

    public ClientThreadHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // sending to client (pwrite object)
            OutputStream ostream = socket.getOutputStream();
            server.out = new PrintWriter(ostream, true);

            // receiving from server (receiveRead  object)
            InputStream istream = socket.getInputStream();
            server.in = new BufferedReader(new InputStreamReader(istream));

            String receiveMessage, sendMessage;
            while(true) {
                receiveMessage = server.in.readLine();
                if (receiveMessage == "Exit") {
                    //close connection
                    System.out.println("Closing connection");
                    socket.close();
                    server.out.close();
                    server.in.close();
                    break;
                }
                if(receiveMessage  != null) {
                    server.servers.get(0).out.println(receiveMessage);//
                    System.out.println(receiveMessage);
                }
                // parse & execute command
                List<String> parsedCommand = ClientCommand.parseCommand(receiveMessage);
                sendMessage = ClientCommand.executeCommand(parsedCommand, server.tree);
                server.out.println(sendMessage);
                server.out.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (server.out != null) {
                    server.out.close();
                }
                if (server.in != null) {
                    server.in.close();
                    //clientSocket.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
