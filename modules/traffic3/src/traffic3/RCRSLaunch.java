package traffic3;

import java.util.Properties;
import java.util.Arrays;
import java.util.List;
import java.io.File;

import rescuecore2.config.Config;

import org.util.xml.io.XMLConfigManager;

import traffic3.manager.WorldManager;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.simulator.RCRSTrafficSimulator;
import static traffic3.log.Logger.log;

/**
 *
 */
public class RCRSLaunch {

    /**
     *
     */
    public static void start(Properties properties) {
        try {

            XMLConfigManager config = new XMLConfigManager();
            
            for (String c : properties.stringPropertyNames()) {
                String rp = c;
                String tp = c.replaceAll("\\.", "/");
                config.setValue(tp, properties.getProperty(c));
            }

            Object filepath = (Object)properties.get("rcrs.traffic3.setting");
            if (filepath != null) {
                File file = new File((String)filepath);
                log("read rcrs setting file: " + file.getAbsolutePath());
                Config rcrsConfig = new Config(file);
                for (String rcrsKey : rcrsConfig.getAllKeys()) {
                    if ("traffic3.gui.menu.Menu".equals(rcrsKey)) {
                        try {
                            List<String> menuList = rcrsConfig.getArrayValue("traffic3.gui.menu.Menu");
                            org.util.xml.element.TagElement menuListTag = new org.util.xml.element.TagElement("menu-item"); 
                            menuListTag.setAttribute("type", "list");
                            menuListTag.setAttribute("name", "menu");
                            for (String menu : menuList) {
                                String[] nameAction = menu.split("=");
                                String actionName = nameAction[0];
                                String className = nameAction[1];
                                org.util.xml.element.TagElement menuItemTag = new org.util.xml.element.TagElement("menu-item");
                                menuItemTag.setAttribute("type", "action");
                                menuItemTag.setAttribute("name", actionName);
                                menuItemTag.setAttribute("class", className);
                                menuListTag.addChild(menuItemTag);
                            }
                            config.getTag("gui/menu/").setChildren(menuListTag);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        String trafficKey = "rcrs/" + rcrsKey.replaceAll("\\.", "/");
                        config.setValue(trafficKey, rcrsConfig.getValue(rcrsKey));
                    }
                }
            }
            log(config);
            WorldManager manager = new WorldManager();

            if ("true".equals(config.getValue("rcrs/traffic3/gui", "false"))) {
                log("create GUI");                
                WorldManagerGUI gui = new WorldManagerGUI(manager, config);
                javax.swing.JFrame frame = org.util.Handy.showFrame(gui);
                frame.setJMenuBar(gui.createMenuBar());
            }

            RCRSTrafficSimulator rcrsSimulator = new RCRSTrafficSimulator(manager, config);
            rcrsSimulator.start();
        }
        catch (Exception e) {
            log(e);
        }
    }

    public static void listDefaultProperties(Properties properties) {
        properties.put("mode", "rcrs");
        properties.put("rcrs.traffic3.setting", "./traffic3.cfg");
        properties.put("rcrs.traffic3.port", "7000");
        properties.put("rcrs.traffic3.gui", "false");
        properties.put("rcrs.traffic3.microStep", "100");
    }

    public static void showUsage(String pre) {
        System.out.println(pre + " -mode=rcrs -rcrs.traffic3.setting=./traffic3.cfg");
        System.out.println(pre + " -mode=rcrs -rcrs.traffic3.port=7000 -rcrs.traffic3.gui=true rcrs.traffic3.microStep=100");
    }
}