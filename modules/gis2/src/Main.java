import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class Main {

	public static void main(String[] args) throws Exception {
		alert("hello");
	}
	
	public static void alert(Object message) {
		System.out.println(message);
		// javax.swing.JOptionPane.showMessageDialog(null, message);
	}
	public static double inputValue(String msg,String a)
	{
		boolean continueflag = true;
		double tmpd = -1;
		while(continueflag)
		try{
			tmpd = Double.parseDouble(JOptionPane.showInputDialog(msg,a));
			continueflag = false;
		}catch(Exception e){}
		return tmpd;
	}
}
