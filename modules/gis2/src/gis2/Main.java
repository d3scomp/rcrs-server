package gis2;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import traffic3.log.event.*;

public class Main {
    
    public static final File DEFAULT_CONFIG_FILE = new File("./config/gis2.gml");
    public static final File DEFAULT_AGENT_FILE = new File("./config/agent.xml");

    public static void main(String[] args) throws Exception {

	OutputStream out = new OutputStream(){
		public void write(int b) {
		    //System.out.println((char)b);
		}
	    };
	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
	traffic3.log.Logger.initialize(writer);
	traffic3.log.Logger.addLogListener(new LogListener(){
		public void log(LogEvent e) {
		    Object message = e.getMessage();
		    if(e.getType()==javax.swing.JOptionPane.ERROR_MESSAGE) {
			System.err.println(message);
		    } else {
			System.out.println(message);
		    }
		}
	    });
	

	int port = 7001;
	File file1 = (args.length >= 1 ? new File(args[0]) : DEFAULT_CONFIG_FILE);
	File file2 = (args.length >= 2 ? new File(args[1]) : DEFAULT_AGENT_FILE);
	GISServer.startProcess(file1, file2, port);
    }
    
}
