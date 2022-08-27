import helpingTools.yaml.Configuration;

import java.io.IOException;

public class ClientStart {

    public static Client startClient(Configuration config) throws IOException {
        Client client;
        int[] serversPorts = config.getTCPports();
        // choose the first server to connect to
        client = new Client("127.0.0.1", serversPorts[0]);
        return client;
    }

}
