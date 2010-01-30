package gis2;

import java.io.File;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;

public class Main {

    public static final File DEFAULT_CONFIG_FILE = new File("./config/gis2.cfg");
    public static final int DEFAULT_PORT = 7001;

    public static void main(String[] args) {

        System.out.println("current dir: " + new File(".").getAbsolutePath());
        File configFile = ((args.length > 0) ? new File(args[0]) : DEFAULT_CONFIG_FILE);
        System.out.println("config file: " + configFile.getAbsolutePath());

        try {
            Config config = new Config(configFile);
            GISServer gis = new GISServer(config, System.out, System.err);
            gis.run();
        }
        catch (ConfigException e) {
            e.printStackTrace();
        }
    }
}