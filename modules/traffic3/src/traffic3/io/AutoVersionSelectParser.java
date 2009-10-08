package traffic3.io;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import traffic3.manager.*;
import traffic3.objects.area.*;

import org.util.xml.parse.*;
import org.util.xml.parse.policy.*;
import org.util.xml.element.*;

import static traffic3.log.Logger.alert;
import static traffic3.log.Logger.log;

public class AutoVersionSelectParser {

    private Parser[] parser_list_;
    private boolean gui_confirm_;
    private JComponent root_frame_;
    private Parser output_parser_;

    public AutoVersionSelectParser(Parser[] parser_list, boolean gui_confirm) {
	assert parser_list!=null : "parser list is null.";
	parser_list_ = parser_list;
	gui_confirm_ = gui_confirm;
    }

    /*
    public Parser[] getSelectedParserList(File file) throws Exception {
	ArrayList<Parser> supported_parser_list = new ArrayList<Parser>();
	for(Parser parser : parser_list_) {
	    InputStream input = new FileInputStream(file);
	    if(parser.isSupported(input))
		supported_parser_list.add(parser);
	}
	return supported_parser_list.toArray(new Parser[0]);
    }
    */

    public Parser[] getSelectedParserList(File file) throws Exception {
	InputStream input = new FileInputStream(file);
	ElementParser p = new ElementParser(input);
	/*
	final ParserPolicy hide_all_policy = new ParserPolicy() {
		public Element allowElement(Element element) {
		    if(element.isTextElement()) return element;
		    return null;
		}
		public boolean checkEndTag() {
		    return true;
		}
		public boolean finished() {
		    return false;
		}
		public boolean forceEmptyTag(String key) {
		    return false;
		}
		public ParserPolicy getInnerPolicy(Element element) {
		    return this;
		}
		public String selectEncoding(String last_tag_key) {
		    return null;
		}
		public boolean throwExceptionIfDocumentHasError() {
		    return true;
		}
	    };
	*/
	final String[] version = new String[]{null};
	ParserPolicy policy = new ParserPolicy() {
		public Element allowElement(Element element) {
		    if(element.isTagElement()) {
			TagElement tag = (TagElement)element;
			if(tag.getKey().toLowerCase().equals("rcrs:version")) {
			    version[0] = tag.getValue();
			}
			return null;
		    } else
			return element;
		}
		public boolean checkEndTag() {
		    return true;
		}
		public boolean finished() {
		    return version[0]!=null;
		}
		public boolean forceEmptyTag(String key) {
		    return false;
		}
		public ParserPolicy getInnerPolicy(Element element) {
		    return this;
		}
		public String selectEncoding(String last_tag_key) {
		    return null;
		}
		public boolean throwExceptionIfDocumentHasError() {
		    return true;
		}
	    };
	p.setPolicy(policy);
	p.parse();
	
	String v = version[0];
	
	ArrayList<Parser> supported_parser_list = new ArrayList<Parser>();
	for(Parser parser : parser_list_) {
	    if(parser.getVersion().equals(v))
		supported_parser_list.add(parser);
	}
	return supported_parser_list.toArray(new Parser[0]);
    }

    public Parser selectParser(File file) throws Exception {
	Parser[] supported_parser_list = getSelectedParserList(file);
	Parser selected_parser = null;
	if(supported_parser_list.length == 0)
	    throw new Exception("Cannot find supported parser.");

	if(supported_parser_list.length>2 && gui_confirm_) {
	    String message = "";
	    String title = "";
	    int type = JOptionPane.INFORMATION_MESSAGE;
	    Icon icon = null;
	    Object[] choice = supported_parser_list;
	    Object selection = JOptionPane.showInputDialog(root_frame_, message, title, type, icon, choice, choice[0]);
	    selected_parser = (Parser)selection;
	} else {
	    selected_parser = supported_parser_list[0];
	}
	return selected_parser;
    }

    public void setOutputParser(Parser output_parser) {
	output_parser_ = output_parser;
    }

    public void input(WorldManager world_manager, File file) throws Exception {
	Parser parser = selectParser(file);
	//JOptionPane.showMessageDialog(null, parser);
	System.out.println(parser);
	parser.input(world_manager, new FileInputStream(file));
    }

    public void output(WorldManager world_manager, File file) throws Exception {
	Parser output_parser;
	if(output_parser_ != null)
	    output_parser = output_parser_;
	else if(gui_confirm_) {
	    throw new Exception("not supported yet");
	}else {
	    output_parser = parser_list_[0];
	}
	output_parser.output(world_manager, new FileOutputStream(file));
    }

}
