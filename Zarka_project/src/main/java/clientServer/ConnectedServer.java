package clientServer;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ConnectedServer {
    public final int port;
    public PrintWriter out;
    public BufferedReader in;

    public ConnectedServer(int port) {
        this.port = port;
    }
}
