package clientServer;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader keyRead;
    private PrintWriter pwrite;
    private BufferedReader receiveRead;

    public Client(String address, int port) throws IOException {
        socket = new Socket(address, port);

        // reading from keyboard (keyRead object)
        keyRead = new BufferedReader(new InputStreamReader(System.in));

        // sending to client (pwrite object)
        OutputStream ostream = socket.getOutputStream();
        pwrite = new PrintWriter(ostream, true);

        // receiving from server (receiveRead  object)
        InputStream istream = socket.getInputStream();
        receiveRead = new BufferedReader(new InputStreamReader(istream));

        System.out.println("Connected");
        System.out.println("Type and press Enter key");

        String receiveMessage, sendMessage;
        while(true) {
            sendMessage = keyRead.readLine();  // keyboard reading
            pwrite.println(sendMessage);       // sending to server
            pwrite.flush();

            receiveMessage = receiveRead.readLine(); //receive from server
            if(receiveMessage == "Exit") {
                //close connection
                System.out.println("Closing connection");
                socket.close();
                keyRead.close();
                pwrite.close();
                receiveRead.close();
                break;
            }
            if(receiveMessage != null) {
                System.out.println(receiveMessage);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        Client client = ClientStart.startClient(config);

    }
}
