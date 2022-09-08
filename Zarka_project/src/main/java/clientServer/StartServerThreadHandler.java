package clientServer;

import helpingTools.yaml.Configuration;

import java.io.IOException;

public class StartServerThreadHandler implements Runnable {
    private final int port;
    private final Configuration config;
    private final int i;

    public StartServerThreadHandler(int port, Configuration config, int i) {
        this.port = port;
        this.config = config;
        this.i = i;
    }

    @Override
    public void run() {
        try {
            ServerStart.startServer(port,config,i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
