package clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThreadHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final Server server;
    private final Server coordinator;

    public ServerThreadHandler(ServerSocket serverSocket, Server server, Server coordinator) {
        this.serverSocket = serverSocket;
        this.server = server;
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        try {
            Socket socket = serverSocket.accept();
            System.out.println("New Server Connected");

            // get the inputstream of other server (client)
            server.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // get the outputstream of other server (client)
            server.out = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while ((line = server.in.readLine()) != null) {
                // output the received message from other server (client) to the client
                System.out.println("Server port " + coordinator.port + ": Received message from server " + server.port + " = " + line);
                coordinator.out.println(line);
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
