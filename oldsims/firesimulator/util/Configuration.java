package firesimulator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import firesimulator.simulator.Wind;
import firesimulator.world.Wall;

public class Configuration {
	private static final Log LOG = LogFactory.getLog(Configuration.class);

	private static final String PREFIX = "resq-fire.";

	public class Property {

		private String name;
		private String command;
		private String description;
		private String paramName;
		private boolean required;
		private String value;
		boolean active;

		public Property(String name, String command, String description, String paramName, boolean required, String defaultValue) {
			this.name = PREFIX + name;
			this.command = command;
			this.description = description;
			this.paramName = paramName;
			this.required = required;
			if (defaultValue != null) {
				value = defaultValue;
				active = true;
			} else {
				value = null;
				active = false;
			}
		}
		
		public Property(String name) {
			this(name, name, null, null, true, null);
		}
		
		public Property(String name, boolean required, String defaultValue) {
			this(name, name, null, null, required, defaultValue);
		}

		public String getValue() {
			return value;
		}

		public boolean isActive() {
			return active;
		}

		public boolean validate() {
			if (required) {
				if (value == null || value.length() == 0) {
					return false;
				}
			}
			return true;
		}

		public String getDescription() {
			if (description == null) {
				return "";
			}
			return "\n" + name + ":  " + command + " "
					+ (paramName != null ? (!required ? "[" : "") + "<" + paramName + ">" + (!required ? "]" : "") : "")
					+ "\n" + description + "\n";
		}

	}

	private static LinkedList<Property> Props = new LinkedList<>();
	private static final String CONFIG_TXT_PATH = ".";
	public static String VERSION = "06.08.2005";

	public void initialize() {
		Props.add(new Property("store", "s", "Stores the intial data from the kernel in the given file.", "filename", true, null));
		Props.add(new Property("virtual", "v", "Use the virtual kernel instead of the rescue kernel.\nRequires a .scn file.", "filename", true, null));
		Props.add(new Property("host", "h", "The host to connect to. Default host is localhost.", "host", true, "localhost"));
		Props.add(new Property("port", "p", "The port to connect to. Default port is 6000", "port", true, "6000"));
		Props.add(new Property("setup", "stp", "Uses the given setup file", "filename", true, null));
		Props.add(new Property("csetup", "cstp", "Uses the given config.txt file", "filename", true, null));
		Props.add(new Property("ray_rate", "ray_rate", "Number of emitted rays per mm while sampling. Default rate is " + Wall.RAY_RATE,
				"rate", true, Wall.RAY_RATE + ""));
		Props.add(new Property("help", "help", "Prints this text and exits", null, false, null));
		// hidden parameters
		Props.add(new Property("cell_size"));
		Props.add(new Property("max_ray_distance"));
		Props.add(new Property("energy_loss"));
		Props.add(new Property("air_to_air_flow"));
		Props.add(new Property("air_to_building_flow"));
		Props.add(new Property("air_cell_heat_capacity"));
		Props.add(new Property("wooden_capacity"));
		Props.add(new Property("wooden_energy"));
		Props.add(new Property("wooden_ignition"));
		Props.add(new Property("wooden_burning"));
		Props.add(new Property("wooden_speed"));
		Props.add(new Property("steel_capacity"));
		Props.add(new Property("steel_energy"));
		Props.add(new Property("steel_ignition"));
		Props.add(new Property("steel_burning"));
		Props.add(new Property("steel_speed"));
		Props.add(new Property("concrete_capacity"));
		Props.add(new Property("concrete_energy"));
		Props.add(new Property("concrete_ignition"));
		Props.add(new Property("concrete_burning"));
		Props.add(new Property("concrete_speed"));
		Props.add(new Property("max_extinguish_power_sum"));
		Props.add(new Property("water_refill_rate"));
		Props.add(new Property("water_hydrant_refill_rate"));
		Props.add(new Property("water_capacity"));
		Props.add(new Property("water_thermal_capacity"));
		Props.add(new Property("water_distance"));
		Props.add(new Property("radiation_coefficient"));
		Props.add(new Property("wind_random"));
		Props.add(new Property("wind_big_change_probability", false, String.format("%f", Wind.WIND_BIG_CHANGE_PROBABILITY)));
		Props.add(new Property("wind_speed"));
		Props.add(new Property("wind_speed_small_change", false, String.format("%d", Wind.WIND_SPEED_CHANGE)));
		Props.add(new Property("wind_speed_big_change", false, String.format("%d", Wind.WIND_BIG_SPEED_CHANGE)));
		Props.add(new Property("wind_direction"));
		Props.add(new Property("wind_direction_small_change", false, String.format("%d", Wind.WIND_DIRECTION_CHANGE)));
		Props.add(new Property("random.seed"));
		Props.add(new Property("refuge_inflammable"));
		Props.add(new Property("fire_station_inflammable"));
		Props.add(new Property("police_office_inflammable"));
		Props.add(new Property("ambulance_center_inflammable"));
		Props.add(new Property("gamma"));
		Props.add(new Property("rays.dir", "rays", null, null, true, "rays"));
		Props.add(new Property("burn-rate-average", "burn-rate-average", null, null, true, "0.2"));
		Props.add(new Property("burn-rate-variance", "burn-rate-variance", null, null, true, "0"));
	}

	public void parse(String cmdLine) {
		StringTokenizer st = new StringTokenizer(cmdLine, "-");
		try {
			while (st.hasMoreTokens()) {
				String tok = st.nextToken();
				int index = tok.indexOf(" ");
				String cmd;
				if (index == -1) {
					cmd = tok.trim();
					if (cmd.length() == 0)
						continue;
					Property p = propForCmd(cmd);
					p.active = true;
				} else {
					cmd = tok.substring(0, index).trim();
					if (cmd.length() == 0)
						continue;
					Property p = propForCmd(cmd);
					p.active = true;
					p.value = tok.substring(index).trim();
				}
			}
		} catch (Exception e) {
			printHelpAndExit();
		}
		if (isActive("help")) {
			printHelpAndExit();
		}
	}

	private void printHelpAndExit() {
		System.out.println("ResQ Firesimulator");
		System.out.println(VERSION);
		System.out.println("author: Timo N�ssle\nemail: nuessle@informatik.uni-freiburg.de\n");
		System.out.println("java Main [-<option> <value>]*");
		for (Property p : Props) {
			System.out.print(p.getDescription());
		}
		System.exit(0);
	}

	private static Configuration.Property propForCmd(String cmd) {
		for (Property p : Props) {
			if (p.command.compareTo(cmd) == 0)
				return p;
		}
		return null;
	}

	public static boolean isActive(String name) {
		for (Property p : Props) {
			if (p.name.compareTo(name) == 0)
				return p.isActive();
		}
		return false;
	}

	public static String getRawValue(String name) {
		for (Property p : Props) {
			if (p.name.compareTo(name) == 0)
				return p.getValue();
		}
		return null;
	}

	public static int getIntValue(String name) {
		String rawValue = getRawValue(name);
		try {
			int value = Integer.parseInt(rawValue);
			return value;
		} catch(NumberFormatException e) {
			LOG.error("Invalid value of the property \"" + name + "\" with value \"" + rawValue + "\"");
			return 0;
		}
	}
	
	public static long getLongValue(String name) {
		String rawValue = getRawValue(name);
		try {
			long value = Long.parseLong(rawValue);
			return value;
		} catch(NumberFormatException e) {
			LOG.error("Invalid value of the property \"" + name + "\" with value \"" + rawValue + "\"");
			return 0;
		}
	}

	public static float getFloatValue(String name) {
		String rawValue = getRawValue(name);
		try {
			float value = Float.parseFloat(rawValue);
			return value;
		} catch(NumberFormatException e) {
			LOG.error("Invalid value of the property \"" + name + "\" with value \"" + rawValue + "\"");
			return 0;
		}
	}
	
	public static double getDoubleValue(String name) {
		String rawValue = getRawValue(name);
		try {
			double value = Double.parseDouble(rawValue);
			return value;
		} catch(NumberFormatException e) {
			LOG.error("Invalid value of the property \"" + name + "\" with value \"" + rawValue + "\"");
			return 0;
		}
	}
	
	public static boolean getBoolValue(String name) {
		String rawValue = getRawValue(name);
		try {
			boolean value = Boolean.parseBoolean(rawValue);
			return value;
		} catch(NumberFormatException e) {
			LOG.error("Invalid value of the property \"" + name + "\" with value \"" + rawValue + "\"");
			return false;
		}
	}

	public void parse(String[] args) {
		if (args.length < 1)
			return;
		String s = "";
		for (int i = 0; i < args.length; s += " " + args[i], i++)
			;
		parse(s);
	}

	public static boolean loadSetup(String fileName) {
		try {
			FileInputStream fis = new FileInputStream(new File(fileName));
			Properties prop = new Properties();
			prop.load(fis);
			fis.close();
			for (Property p : Props) {
				String val = prop.getProperty(p.command);
				if (val != null) {
					p.value = val;
					p.active = true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void loadConfigTXT(String filename) {
		String fname = CONFIG_TXT_PATH + File.separator + "config.txt";
		if (filename != null)
			fname = filename;
		LOG.info("loading values from \"" + fname + "\"");
		try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
			Pattern comment = Pattern.compile("([^#]*)(#(.*))*", Pattern.DOTALL);
			Pattern keyValue = Pattern.compile("([^:]*):(.*)", Pattern.DOTALL);

			Hashtable<String, String> lines = new Hashtable<>();
			String line;
			String key;
			String value;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				Matcher m = comment.matcher(line);
				if (m.matches()) {
					Matcher gm = keyValue.matcher(m.group(1));
					if (gm.matches() && gm.groupCount() == 2) {
						key = gm.group(1).trim();
						value = gm.group(2).trim();
						lines.put(key, value);
					}
				}
			}
			for (Property p : Props) {
				String name = p.name;
				value = (String) lines.get(name);
				if (value != null) {
					p.active = true;
					p.value = value;
				}
			}

		} catch (Exception e) {
			LOG.error("unable to load \"" + fname + "\"", e);
		}

	}

	public static void setProperty(String name, String value, boolean state) {
		for (Property p : Props) {
			if (p.name.compareTo(name) == 0) {
				p.value = value;
				p.active = state;
				break;
			}
		}
	}

	public static List<String> getPropertyNames() {
		List<String> result = new ArrayList<String>();
		for (Property p : Props) {
			result.add(p.name);
		}
		return result;
	}

	public static void dump() {
		for (Property p : Props) {
			LOG.debug(p.command + "=" + p.value + "[" + p.active + "]");
		}
	}

	public static void storeHiddenProps(String fileName) throws IOException {
		Properties prop = new Properties();
		for (Property p : Props) {
			if (p.description == null) {
				if (p.value == null) {
					LOG.debug(p.command);
				}
				prop.put(p.command, p.value);
			}
		}
		if (!fileName.endsWith(".stp"))
			fileName += ".stp";
		File f = new File(fileName);
		if (f.exists())
			f.delete();
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		prop.store(fos, "fire simulator setup file");
		fos.close();
	}

}
