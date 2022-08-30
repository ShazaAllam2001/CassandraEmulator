package clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThreadHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final Server coordinator;

    public ServerThreadHandler(Socket socket, Server server, Server coordinator) {
        this.socket = socket;
        this.server = server;
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        try {
            // get the outputstream of other server (client)
            server.out = new PrintWriter(socket.getOutputStream(), true);

            // get the inputstream of  other server (client)
            server.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = server.in.readLine()) != null) {
                // output the received message from client
                System.out.printf("Sent from the server: %s\n", line);
                coordinator.out.println(line);
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
