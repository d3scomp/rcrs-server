package traffic3;

import java.util.Properties;
import java.util.Arrays;
import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import traffic3.log.Logger;
import traffic3.log.LoggerException;

public class Launch {

    public static void main(String[] args) {
            org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(com.infomatiq.jsi.rtree.RTree.class.getName());
            logger.setLevel(org.apache.log4j.Level.OFF);

        if (args.length > 0 && args[0].endsWith("help")) {
            showUsage();
        }
        else {
            try {
                Properties properties = new Properties();
                listDefaultProperties(properties);
                readProperties(properties, args);
                start(properties);
            }
            catch (LaunchException e) {
                System.err.println("ERROR: ");
                e.printStackTrace();
            }
        }
    }

    public static void start(Properties properties) throws LaunchException {
        try {// try to initialize logger
            String log = properties.getProperty("log");
            if ("stdout".equals(log)) {
                Logger.initialize(new BufferedWriter(new OutputStreamWriter(System.out)));
            }
            else {
                File logFile = new File(log);
                Logger.initialize(logFile);
            }
        }
        catch(LoggerException e) {
            throw new LaunchException("cannot initialize logger", e);
        }
        
        String mode = properties.getProperty("mode");
        if ("rcrs".equals(mode)) {
            RCRSLaunch.start(properties);
        }
        else if ("plain".equals(mode)) {
            PlainLaunch.start(properties);
        }
    }

    private static void readProperties(Properties properties, String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-")) {
                arg = arg.substring(1, arg.length());
            }
            String[] keyValues = arg.split("=");
            if (keyValues.length == 1) {
                properties.put(keyValues[0], "true");
            }
            else {
                Object o = properties.get(keyValues);
                if (o == null || o instanceof String) {
                    properties.put(keyValues[0], keyValues[1]);
                }
                else if (o instanceof String[]) {
                    String[] os = (String[])o;
                    String[] nos = new String[os.length + keyValues.length - 1];
                    System.arraycopy(os, 0, nos, 0, os.length);
                    System.arraycopy(keyValues, 1, nos, os.length, keyValues.length - 1);
                    properties.put(keyValues[0], nos);
                }
            }
        }
    }

    private static void listDefaultProperties(Properties properties) {
        RCRSLaunch.listDefaultProperties(properties);
        PlainLaunch.listDefaultProperties(properties);
        properties.put("log", "./traffic3.log");
    }

    public static void showUsage() {
        Properties properties = new Properties();
        listDefaultProperties(properties);
        StringBuffer sb = new StringBuffer();
        sb.append("java Launch");
        for (String key : properties.stringPropertyNames()) {
            Object o = properties.get(key);
            if (o instanceof String) {
                sb.append(" -").append(key).append("=").append((String)o);
            }
            else {
                sb.append(" -").append(key).append("=").append(Arrays.toString((String[])o));
            }
        }
        System.out.println(sb);
    }

    public static String getVersion() {
        return "traffic3[3.0.17]";
    }
}