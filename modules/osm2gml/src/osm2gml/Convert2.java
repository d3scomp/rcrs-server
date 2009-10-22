package osm2gml;

import osm2gml.osm.OSMMap;
import osm2gml.osm.OSMMapViewer;
import osm2gml.osm.OSMException;
import osm2gml.osm.OSMBuilding;
import osm2gml.osm.OSMRoad;
import osm2gml.osm.OSMNode;
import osm2gml.gml.GMLMap;
import osm2gml.gml.GMLMapViewer;
import osm2gml.gml.GMLException;
import osm2gml.gml.GMLNode;
import osm2gml.gml.GMLEdge;
import osm2gml.gml.GMLDirectedEdge;
import osm2gml.gml.GMLFace;
import osm2gml.gml.GMLFaceShapeInfo;
import osm2gml.gml.GMLEdgeShapeInfo;
import osm2gml.gml.GMLNodeShapeInfo;
import osm2gml.gml.FaceType;

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

public class Convert2 {
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
            System.out.println("Usage: Convert2D <osm-mapname> <gml-mapname>");
            return;
        }
        try {
            OSMMap osmMap = readOSMMap(args[0]);
            OSMMapViewer osmViewer = new OSMMapViewer(osmMap);
            Convert2 convert = new Convert2();
            GMLMap gmlMap = convert.convert(osmMap);
            Document output = gmlMap.toXML(1000.0 / ConvertTools.sizeOf1Metre(osmMap));
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

    public GMLMap convert(final OSMMap map) throws InterruptedException {
        osmMap = map;
        gmlMap = new GMLMap();

        JFrame frame = new JFrame("OSM to GML converter");
        JPanel main = new JPanel(new BorderLayout());
        JComponent top = Box.createVerticalBox();
        top.add(new JLabel("Converting OSM map with " + map.getRoads().size() + " roads and " + map.getBuildings().size() + " buildings"));
        top.add(new JLabel("Map size: " + (map.getMaxLongitude() - map.getMinLongitude()) + " x " + (map.getMaxLatitude() - map.getMinLatitude())));
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(MARGIN, MARGIN, MARGIN, MARGIN);
        JPanel progress = new JPanel(layout);

        Random random = new Random();

        List<ConvertStep> steps = new ArrayList<ConvertStep>();
        ScanOSMStep scan = new ScanOSMStep(osmMap);
        addStep(new CleanOSMStep(osmMap), steps, progress, layout, c);
        addStep(scan, steps, progress, layout, c);
        addStep(new MakeGMLStep(scan, gmlMap), steps, progress, layout, c);
        addStep(new SplitIntersectingEdgesStep(gmlMap), steps, progress, layout, c);
        addStep(new SplitFacesStep(gmlMap), steps, progress, layout, c);
        addStep(new RemoveFacesStep(gmlMap), steps, progress, layout, c);
        addStep(new MergeFacesStep(gmlMap), steps, progress, layout, c);
        addStep(new CreateBuildingsStep(gmlMap, ConvertTools.sizeOf1Metre(osmMap), random), steps, progress, layout, c);
        addStep(new CreateEntrancesStep(gmlMap), steps, progress, layout, c);
        addStep(new ComputePassableEdgesStep(gmlMap), steps, progress, layout, c);
        addStep(new PruneStep(gmlMap), steps, progress, layout, c);

        main.add(top);
        main.add(progress);

        frame.setContentPane(main);
        frame.pack();
        frame.setVisible(true);

        for (ConvertStep next : steps) {
            next.doStep();
        }

        return gmlMap;
    }

    private void addStep(ConvertStep step, List<ConvertStep> steps, JComponent panel, GridBagLayout layout, GridBagConstraints c) {
        JLabel title = new JLabel(step.getDescription());
        JProgressBar progress = step.getProgressBar();
        JComponent status = step.getStatusComponent();

        c.gridx = 0;
        c.weightx = 1;
        layout.setConstraints(title, c);
        panel.add(title);
        c.gridx = 1;
        c.weightx = 0;
        layout.setConstraints(progress, c);
        panel.add(progress);
        c.gridx = 2;
        c.weightx = 1;
        layout.setConstraints(status, c);
        panel.add(status);
        ++c.gridy;
        progress.setPreferredSize(new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));
        status.setPreferredSize(new Dimension(STATUS_WIDTH, STATUS_HEIGHT));

        steps.add(step);
    }
}