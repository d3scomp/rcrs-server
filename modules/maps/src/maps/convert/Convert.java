package maps.convert;

import maps.osm.OSMMap;
import maps.osm.OSMMapViewer;
import maps.osm.OSMException;
import maps.osm.OSMBuilding;
import maps.osm.OSMRoad;
import maps.osm.OSMNode;
import maps.gml.GMLMap;
import maps.gml.GMLMapViewer;
import maps.gml.GMLException;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLFace;
import maps.gml.debug.GMLFaceShapeInfo;
import maps.gml.debug.GMLEdgeShapeInfo;
import maps.gml.debug.GMLNodeShapeInfo;
import maps.gml.FaceType;
import maps.ConstantConversion;
import maps.convert.osm2gml.Convertor;
import maps.gml.formats.RobocupFormat;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.JProgressBar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Collections;
import java.util.Collection;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Random;

import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ShapeDebugFrame;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.Line2D;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

public class Convert {
    // Nodes that are close are deemed to be co-located.
    private final static double NEARBY_NODE_THRESHOLD = 0.000001;

    private final static int PROGRESS_WIDTH = 200;
    private final static int PROGRESS_HEIGHT = 10;
    private final static int STATUS_WIDTH = 500;
    private final static int STATUS_HEIGHT = 10;
    private final static int MARGIN = 4;

    private OSMMap osmMap;
    private GMLMap gmlMap;

    //    private ShapeDebugFrame debug;
    //    private List<ShapeDebugFrame.ShapeInfo> allOSMNodes;
    //    private List<ShapeDebugFrame.ShapeInfo> allGMLNodes;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: Convert <osm-mapname> <gml-mapname>");
            return;
        }
        try {
            OSMMap osmMap = readOSMMap(args[0]);
            OSMMapViewer osmViewer = new OSMMapViewer(osmMap);
            Convertor convert = new Convertor();
            GMLMap gmlMap = convert.convert(osmMap);
            Document output = new RobocupFormat().write(gmlMap);
            XMLWriter writer = new XMLWriter(new FileOutputStream(new File(args[1])), OutputFormat.createPrettyPrint());
            writer.write(output);
            writer.flush();
            writer.close();
            GMLMapViewer gmlViewer = new GMLMapViewer(gmlMap);
            JFrame frame = new JFrame("Convertor");
            JPanel main = new JPanel(new GridLayout(1, 2));
            osmViewer.setPreferredSize(new Dimension(500, 500));
            gmlViewer.setPreferredSize(new Dimension(500, 500));
            osmViewer.setBorder(BorderFactory.createTitledBorder("OSM map"));
            gmlViewer.setBorder(BorderFactory.createTitledBorder("GML map"));
            main.add(osmViewer);
            main.add(gmlViewer);
            frame.setContentPane(main);
            frame.pack();
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static OSMMap readOSMMap(String file) throws OSMException, IOException, DocumentException {
        File f = new File(file);
        return new OSMMap(f);
    }
}