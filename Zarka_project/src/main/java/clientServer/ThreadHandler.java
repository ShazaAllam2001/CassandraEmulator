package clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadHandler implements Runnable {
    private final Socket socket;
    private final Server server;

    public ThreadHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // get the outputstream of other server (client)
            server.out = new PrintWriter(socket.getOutputStream(), true);

            // get the inputstream of the client
            server.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = server.in.readLine()) != null) {
                // output the received message from client
                System.out.printf("Sent from the coordinator: %s\n", line);
                server.out.println(line);
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
