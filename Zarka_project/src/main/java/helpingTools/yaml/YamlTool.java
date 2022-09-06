package helpingTools.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;

public class YamlTool {
    private final static String currDir = "C:\\Users\\Blu-Ray\\Documents\\OOP Assignments\\CassandraEmulator\\Zarka_project\\src\\main\\java\\clientServer\\files\\configurations\\";

    public static Configuration readYaml(String fileName) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(currDir + fileName);
        Yaml yaml = new Yaml(new Constructor(Configuration.class));
        Configuration config = yaml.load(inputStream);
        return config;
    }

    public static void writeYaml(String fileName, Configuration obj) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(currDir + fileName);
        // dumping options
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        yaml.dump(obj, writer);
    }

    public static void main(String[] args) throws Exception {
        Configuration config = YamlTool.readYaml("config.yaml");
        System.out.println(config.getReplicas()[0].getVirtual(1).length);

    }

}
