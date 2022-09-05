package clientServer;

import helpingTools.yaml.Configuration;
import helpingTools.yaml.YamlTool;

import java.io.IOException;
import java.util.Random;

public class ClientStart {

    public static Client startClient(Configuration config) throws IOException {
        Client client;
        int[] serversPorts = config.getTCPports();
        // choose the random server to connect to
        /*Random random = new Random();
        int rn = random.nextInt(serversPorts.length);
        System.out.println("Coordinator chosen " + serversPorts[rn]);
        // write configuration file with Coordinator port
        config.setCoordinatorPort(serversPorts[rn]);
        YamlTool.writeYaml("config.yaml", config);

        client = new Client("127.0.0.1", serversPorts[rn]);*/
        client = new Client("127.0.0.1", serversPorts[0]);
        return client;
    }

}
