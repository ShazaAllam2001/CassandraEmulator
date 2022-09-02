package clientServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ClientThreadHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final Server coordinator;
    private Socket socket;

    public ClientThreadHandler(ServerSocket serverSocket, Server coordinator) {
        this.serverSocket = serverSocket;
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        try {
            this.socket = serverSocket.accept();
            System.out.println("Client accepted");

            // sending to client (pwrite object)
            OutputStream ostream = socket.getOutputStream();
            coordinator.out = new PrintWriter(ostream, true);

            // receiving from server (receiveRead  object)
            InputStream istream = socket.getInputStream();
            coordinator.in = new BufferedReader(new InputStreamReader(istream));

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
                if(receiveMessage  != null) {
                    System.out.println(receiveMessage);
                    // parse & execute command
                    List<String> parsedCommand = ClientCommand.parseCommand(receiveMessage);
                    // get nums of servers associated with the key
                    if(!parsedCommand.get(0).equals("Invalid Command!")) {
                        int[] serversNums = coordinator.consistentHashing.getServers(parsedCommand.get(1));
                        // if the key exists in the coordinator
                        if(serversNums[0] == 0) {
                            sendMessage = ClientCommand.executeCommand(parsedCommand, coordinator.tree);
                            coordinator.out.println(sendMessage);
                            coordinator.out.flush();
                        }
                        else {
                            int serverToAsk = serversNums[0];
                            // send request to the first server that has the key
                            coordinator.servers.get(serverToAsk-1).out.println(receiveMessage);
                        }
                    } else {
                        sendMessage = "Invalid Command!";
                        coordinator.out.println(sendMessage);
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
                    //clientSocket.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
