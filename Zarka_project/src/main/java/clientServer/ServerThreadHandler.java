package clientServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThreadHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final Server serverRequester;
    private final Server coordinator;

    public ServerThreadHandler(ServerSocket serverSocket, Server serverRequester, Server coordinator) {
        this.serverSocket = serverSocket;
        this.serverRequester = serverRequester;
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        try {
            Socket socket = serverSocket.accept();
            System.out.println("Server " + socket.getLocalPort() + " Connected to " + serverRequester.port);

            // get the inputstream of other server (client)
            serverRequester.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // get the outputstream of other server (client)
            serverRequester.out = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while(true) {
                line = serverRequester.in.readLine();
                if(line != null) {
                    String sendMessage = coordinator.quorumTool.checkQuorum(serverRequester, serverSocket, line);
                    if(sendMessage != null) {
                        coordinator.out.println(sendMessage);
                    }
                }
                /*line = serverRequester.in.readLine();
                if(line != null) {
                    // output the received message from other server (client) to the client
                    System.out.println("Server port " + serverSocket.getLocalPort()
                            + ": Received message from server " + serverRequester.port + " = " + line);

                    coordinator.out.println(line);
                }*/
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (serverRequester.out != null) {
                    serverRequester.out.close();
                }
                if (serverRequester.in != null) {
                    serverRequester.in.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
