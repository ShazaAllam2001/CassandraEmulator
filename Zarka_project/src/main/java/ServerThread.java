import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerThread implements Runnable {
    private Socket coordinatorSocket;
    private final Server server;

    public ServerThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        PrintWriter pwrite = null;
        BufferedReader receiveRead = null;
        try {
            coordinatorSocket = server.serverSocket.accept();
            System.out.println("Server " + server.port + " connected to Coordinator");

            // get the outputstream of client
            pwrite = new PrintWriter(coordinatorSocket.getOutputStream(), true);

            // get the inputstream of client
            receiveRead = new BufferedReader(new InputStreamReader(coordinatorSocket.getInputStream()));

            String receiveMessage, sendMessage;
            while(true) {
                receiveMessage = server.receiveRead.readLine();
                //receiveMessage = "add(130,test130)";//////
                if (receiveMessage == "Exit") {
                    //close connection
                    System.out.println("Closing connection");
                    coordinatorSocket.close();
                    server.pwrite.close();
                    receiveRead.close();
                    break;
                }
                if(receiveMessage  != null) {
                    System.out.println(receiveMessage);
                }
                // parse & execute command
                List<String> parsedCommand = ClientCommand.parseCommand(receiveMessage);
                sendMessage = ClientCommand.executeCommand(parsedCommand, server.tree);
                server.pwrite.println(sendMessage);
                server.pwrite.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (pwrite != null) {
                    pwrite.close();
                }
                if (receiveRead != null) {
                    receiveRead.close();
                    coordinatorSocket.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
