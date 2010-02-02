package traffic3.simulator;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.util.concurrent.CountDownLatch;

import traffic3.manager.WorldManager;
import traffic3.objects.area.TrafficArea;

import rescuecore2.misc.gui.ScreenTransform;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class TrafficSimulatorGUI extends JPanel {
    private static final Log LOG = LogFactory.getLog(TrafficSimulatorGUI.class);

    private WorldManager manager;

    private volatile boolean waitOnRefresh;
    private final Object lock = new Object();
    private CountDownLatch latch;

    private WorldView view;
    private JButton cont;
    private JCheckBox wait;

    public TrafficSimulatorGUI(WorldManager manager) {
        super(new BorderLayout());
        this.manager = manager;
        waitOnRefresh = false;

        view = new WorldView();
        cont = new JButton("Continue");
        wait = new JCheckBox("Wait on refresh", waitOnRefresh);
        cont.setEnabled(false);
        cont.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (lock) {
                        if (latch != null) {
                            latch.countDown();
                        }
                    }
                    cont.setEnabled(false);
                }
            });
        wait.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    waitOnRefresh = wait.isSelected();
                }
            });

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(wait, BorderLayout.WEST);
        buttons.add(cont, BorderLayout.CENTER);

        add(view, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public void initialise() {
        view.initialise();
    }

    public void refresh() {
        repaint();
        if (waitOnRefresh) {
            SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        cont.setEnabled(true);
                    }
                });
            synchronized (lock) {
                latch = new CountDownLatch(1);
            }
            try {
                latch.await();
            }
            catch (InterruptedException e) {
                LOG.error("Error waiting for continue", e);
            }
        }
    }

    public void setWaitOnRefresh(boolean b) {
        waitOnRefresh = b;
    }

    private class WorldView extends JComponent {
        private ScreenTransform transform;

        public WorldView() {
        }

        public void initialise() {
            Rectangle2D bounds = null;
            for (TrafficArea area : manager.getAreaList()) {
                Rectangle2D r = area.getShape().getBounds2D();
                if (bounds == null) {
                    bounds = new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                }
                else {
                    Rectangle2D.union(bounds, r, bounds);
                }
            }
            transform = new ScreenTransform(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
        }

        @Override
        public void paintComponent(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            Insets insets = getInsets();
            width -= insets.left + insets.right;
            height -= insets.top + insets.bottom;
            transform.rescale(width, height);
            Graphics2D copy = (Graphics2D)g.create(insets.left, insets.top, width, height);
            drawObjects(copy);
        }

        private void drawObjects(Graphics2D g) {
            drawAreas((Graphics2D)g.create());
        }

        private void drawAreas(Graphics2D g) {
            g.setColor(Color.black);
            for (TrafficArea area : manager.getAreaList()) {
                Line2D[] lines = area.getLines();
                for (Line2D line : lines) {
                    g.drawLine(transform.xToScreen(line.getX1()),
                               transform.yToScreen(line.getY1()),
                               transform.xToScreen(line.getX2()),
                               transform.yToScreen(line.getY2()));
                }
            }
        }
    }
}