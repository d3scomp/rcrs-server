package traffic3.objects.area;

import java.util.ArrayList;
import java.util.List;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficNode;

import org.util.xml.element.TagElement;

/**
 *
 */
public class TrafficAreaEdge extends TrafficObject {

    private String nodeId1;
    private String nodeId2;
    private GeneralPath path;
    private List<Line2D> lineList = new ArrayList<Line2D>();
    private String[] directedAreaIdList;
    private TrafficArea[] directedAreaList;

    private TrafficAreaNode node1;
    private TrafficAreaNode node2;
    private TrafficAreaNode center;

    /**
     * Constructor.
     * @param wm world manager
     */
    public TrafficAreaEdge(WorldManager wm) {
        super(wm);
    }

    /**
     * Constructor.
     * @param wm world manager
     * @param id id
     */
    public TrafficAreaEdge(WorldManager wm, String id) {
        super(wm, id);
    }

    /**
     * set end nodes.
     * @param id1 node1 id
     * @param id2 node2 id
     */
    public void setDirectedNodes(String id1, String id2) {
        nodeId1 = id1;
        nodeId2 = id2;
        if (lineList.size() == 0) {
            TrafficAreaNode n1 = (TrafficAreaNode)getManager().getTrafficObject(nodeId1);
            TrafficAreaNode n2 = (TrafficAreaNode)getManager().getTrafficObject(nodeId2);
            lineList.add(new Line2D.Double(n1.getX(), n1.getY(), n2.getX(), n2.getY()));
            GeneralPath gp = new GeneralPath();
            gp.moveTo(n1.getX(), n1.getY());
            gp.lineTo(n2.getX(), n2.getY());
            path = gp;
        }
        fireChanged();
    }

    /**
     * length.
     * @return length
     */
    public double length() {
        return node1.getDistance(node2);
    }

    /**
     * get directed nodes.
     * @return nodes (length == 2)
     */
    public TrafficAreaNode[] getDirectedNodes() {
        return new TrafficAreaNode[]{node1, node2};
    }

    /**
     * get center.
     * @return node
     */
    public TrafficNode getCenter() {
        if (center == null) {
            double x = (node1.getX() + node2.getX()) / 2;
            double y = (node1.getY() + node2.getY()) / 2;
            double z = 0;
            try {
                center = getManager().createAreaNode(x, y, z);
            }
            catch (WorldManagerException e) {
                e.printStackTrace();
            }
        }
        return center;
    }

    /*
    public void setLineList(ArrayList<Line2D> line_list) {
    GeneralPath gp = new GeneralPath();
    lineList = line_list;
    for(Line2D line : line_list)
        gp.append(line);
    path = path;
    }
    */

    /**
     * get node1 id.
     * @return node1 id
     */
    public String getID1() {
        return nodeId1;
    }

    /**
     * get node2 id.
     * @return node2 id
     */
    public String getID2() {
        return nodeId2;
    }

    /**
     * check object.
     * @throws Exception exception
     */
    public void checkObject() throws WorldManagerException {
        directedAreaList = new TrafficArea[directedAreaIdList.length];
        for (int i = 0; i < directedAreaIdList.length; i++) {
            directedAreaList[i] = (TrafficArea)getManager().getTrafficObject(directedAreaIdList[i]);
        }
        node1 = (TrafficAreaNode)getManager().getTrafficObject(nodeId1);
        node2 = (TrafficAreaNode)getManager().getTrafficObject(nodeId2);

        if (node1 == null) {
            throw new WorldManagerException("Error: Node cannot be found: " + nodeId1 + ": " + toString());
        }
        if (node2 == null) {
            throw new WorldManagerException("Error: Node cannot be found: " + nodeId2 + ": " + toString());
        }
        checked = true;
    }

    /**
     * this AreaEdge has a line or not.
     * @param o line
     * @return has o or not
     */
    public boolean has(Line2D o) {
        for (int i = 0; i < lineList.size(); i++) {
            if ((lineList.get(i).getP1().getX() == o.getP1().getX() && lineList.get(i).getP2().getX() == o.getP2().getX())
                && (lineList.get(i).getP1().getY() == o.getP1().getY() && lineList.get(i).getP2().getY() == o.getP2().getY())) {
                return true;
            }
            if ((lineList.get(i).getP1().getX() == o.getP2().getX() && lineList.get(i).getP2().getX() == o.getP1().getX())
                && (lineList.get(i).getP1().getY() == o.getP2().getY() && lineList.get(i).getP2().getY() == o.getP1().getY())) {
                return true;
            }
            //System.out.print(lineList.get(i).getP1()+o.getP1().toString()+", ");
            //System.out.println(lineList.get(i).getP2()+o.getP2().toString());
        }
        return false;
    }

    /**
     * get next area.
     * @param now area
     * @return next area
     */
    public TrafficArea getNextArea(TrafficArea now) {
        if (directedAreaList.length == 1) {
            return null;
        }
        assert directedAreaList.length == 2 : "three directed area!";

        //alert("<html>"+now+" :</br> "+toLongString()+"</html>", "error");
        if (directedAreaList[0] == now) {
            return directedAreaList[1];
        }
        if (directedAreaList[1] == now) {
            return directedAreaList[0];
        }
        return null;
    }

    /**
     * get directed area.
     * @return directed areas
     */
    public TrafficArea[] getDirectedArea() {
        return directedAreaList;
    }

    /**
     * set directed area id list.
     * @param ids ids
     */
    public void setDirectedAreaIDList(String... ids) {
        directedAreaIdList = ids;
    }

    /**
     * get line list.
     * @return line list
     */
    public Line2D[] getLineList() {
        return lineList.toArray(new Line2D[0]);
    }

    /**
     * distance.
     * @param x x
     * @param y y
     * @return distance
     */
    public double distance(double x, double y) {
        double min = lineList.get(0).ptSegDist(x, y);
        for (int i = 1; i < lineList.size(); i++) {
            min = Math.min(min, lineList.get(i).ptSegDist(x, y));
        }
        return min;
    }

    /**
     * set properties.
     * @param gmlElement gml element
     * @throws Exception exception
     */
    public void setProperties(TagElement gmlElement) throws WorldManagerException {
        // System.out.println("gml edge:"+gmlElement);
        TagElement[] ids = gmlElement.getTagChildren("gml:directedNode");
        String id1 = ids[0].getAttributeValue("xlink:href").replaceAll("#", "");
        String id2 = ids[1].getAttributeValue("xlink:href").replaceAll("#", "");
        String coordinatesText = gmlElement.getTagChild("gml:centerLineOf").getTagChild("gml:LineString").getChildValue("gml:coordinates");
        String[] coordinatesTextList = coordinatesText.split(" ");
        GeneralPath gp = new GeneralPath();
        String[] xy = coordinatesTextList[0].split(",");
        double lx = Double.parseDouble(xy[0]);
        double ly = Double.parseDouble(xy[1]);
        gp.moveTo(lx, ly);
        for (int i = 1; i < coordinatesTextList.length; i++) {
            xy = coordinatesTextList[i].split(",");
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);
            gp.lineTo(x, y);
            lineList.add(new Line2D.Double(lx, ly, x, y));
            lx = x;
            ly = y;
        }
        path = gp;
        TagElement[] fids = gmlElement.getTagChildren("gml:directedFace");
        directedAreaIdList = new String[fids.length];
        for (int i = 0; i < fids.length; i++) {
            directedAreaIdList[i] = fids[i].getAttributeValue("xlink:href").replaceAll("#", "");
        }
        setDirectedNodes(id1, id2);
    }

    /**
     * get path.
     * @return path
     */
    public GeneralPath getPath() {
        return path;
    }

    /**
     * description.
     * @return description
     */
    public String toString() {
        return "TrafficAreaEdge[id:" + getID() + ";id1:" + nodeId1 + ";id2:" + nodeId2 + ";]";
    }

    /**
     * detailed description.
     * @return detailed description
     */
    public String toLongString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<div><div style='font-size:18;'>TrafficAreaEdge(id:" + getID() + ")</div>");
        sb.append("<div>id: ").append(getID()).append("</div>");
        sb.append("<div style='margin-left:50px;'>");
        sb.append("<div>node: ").append(node1).append("</div>");
        sb.append("<div>node: ").append(node2).append("</div>");
        if (directedAreaList != null) {
            for (int i = 0; i < directedAreaList.length; i++) {
                sb.append("<div>directed face").append(i + 1).append(":  ").append(directedAreaList[i]).append("</div>");
            }
        }
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }
}
