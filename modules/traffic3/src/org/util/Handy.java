package org.util;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.Action;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import static traffic3.log.Logger.alert;

/**
 * handy class.
 */
public final class Handy {

    private static final int NATURAL_NUMBER_COMMA = 3;

    private Handy() {};

    /**
     * to natural string of a int value.
     * @param value value (12345)
     * @return string value of the value (12,345)
     */
    public static String toNaturalString(int value) {
        boolean minus = (value <= 0);
        if (minus) {
            value = -value;
        }
        String buf = String.valueOf(value);
        StringBuffer result = new StringBuffer();
        if (minus) {
            result.append("-");
        }
        int length = buf.length();
        int part = length % NATURAL_NUMBER_COMMA;
        int p = 0;
        if (part != 0) {
            result.append(buf.substring(p, p + part));
            p += part;
        }
        if (p < length) {
            result.append(",");
        }
        while (p < length) {
            result.append(buf.substring(p, p + NATURAL_NUMBER_COMMA));
            p += NATURAL_NUMBER_COMMA;
            if (p < length) {
                result.append(",");
            }
        }
        return result.toString();
    }


    /**
     * input string.
     * This method block thread while user is inputting.
     * So this method must not be called from Event Dispatch Thread.
     * @param parent parent
     * @param message message
     * @return inputted value
     * @throws Exception called from Event Dispatch Thread
     */
    public static String inputString(final JComponent parent, final Object message) throws CannotStopEDTException {

        if (SwingUtilities.isEventDispatchThread()) {
            throw new CannotStopEDTException("input string method is called from Event Dispatch Thread!");
        }

        final String[] result = new String[1];
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            final JFrame frame = new JFrame("Input");
            final JTextField tf = new JTextField();
            final JButton button = new JButton("OK");

            Action finishAction = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        button.requestFocus();
                        result[0] = tf.getText();
                        frame.setVisible(false);
                        frame.dispose();
                        try {
                            synchronized (result) {
                                result.notifyAll();
                            }
                        }
                        catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                };
            tf.addActionListener(finishAction);

            JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            button.addActionListener(finishAction);
            buttonpanel.add(button);

            JComponent messagepanel = null;
            if (message instanceof JComponent) {
                messagepanel = (JComponent)message;
            }
            else {
                messagepanel = new JLabel(message.toString());
            }


            final Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(messagepanel, BorderLayout.NORTH);
            panel.add(tf, BorderLayout.CENTER);
            panel.add(buttonpanel, BorderLayout.SOUTH);
            panel.setBorder(border);

            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(parent);
            frame.setVisible(true);
            //result[0] = JOptionPane.showInputDialog(thisObject, message);
        } });
        try {
            synchronized (result) {
                result.wait();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result[0];
    }

    /**
     * input double value.
     * @param parent parent
     * @param message message
     * @return inputted value
     * @throws Exception e
     */
    public static  double inputDouble(JComponent parent, Object message) throws CannotStopEDTException {
        return Double.parseDouble(inputString(parent, message));
    }

    /**
     * input int value.
     * @param parent parent
     * @param message message
     * @return inputted value
     * @throws Exception e
     */
    public static int inputInt(JComponent parent, Object message) throws CannotStopEDTException {
        return Integer.parseInt(inputString(parent, message));
    }

    /**
     * confirm yes or no (true/false).
     * @param parent parent
     * @param message message
     * @return yes or no
     * @throws Exception e
     */
    public static boolean confirm(JComponent parent, Object message) throws CannotStopEDTException {
        int result = JOptionPane.showConfirmDialog(parent, message, "confirm", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

}
