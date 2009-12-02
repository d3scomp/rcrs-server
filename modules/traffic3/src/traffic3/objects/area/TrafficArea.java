package traffic3.objects.area;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;
//import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;
import traffic3.objects.area.event.TrafficAreaListener;
import traffic3.objects.area.event.TrafficAreaEvent;
import org.util.xml.element.TagElement;

/**
 * Traffic area.
 */
public class TrafficArea extends TrafficObject {

    private List<TrafficAreaListener> areaListenerList = new ArrayList<TrafficAreaListener>();
    private List<TrafficAgent> agentList = new ArrayList<TrafficAgent>();
    private List<TrafficBlockade> bockadeList = new ArrayList<TrafficBlockade>();
    private boolean simulateAsOpenSpace = true;
    private String type;

    // cannot be null
    private double centerX;
    private double centerY;
    //private String[] neighbor_area_id_list_;

    // can be null
    private String[] connectorIdList;
    private String[] unconnectorIdList;
    private TrafficAreaNode[] nodeList;
    private GeneralPath shape;

    // these properties will be set at check
    private List<TrafficAreaEdge> connectorList = new ArrayList<TrafficAreaEdge>();
    private List<TrafficAreaEdge> unconnectorList = new ArrayList<TrafficAreaEdge>();
    private List<Line2D> unconnectEdgeList = new ArrayList<Line2D>();
    //    private List<TrafficArea> neighborAreaList = new ArrayList<TrafficArea>();


    private TrafficAgent[] agentListBuf = null;
    private volatile Line2D[] getNeighborWallListCACHE = null;

    /**
     * Constructor.
     * @param worldManager world manager
     * @param id id of this object (it must be unique in world manager WorldManager.getUniqueID("type"))
     */
    public TrafficArea(WorldManager worldManager, String id) {
        super(worldManager, id);
    }

    /**
     * Constructor.
     *
     *          /       /
     *         /       /
     *        /  a3   /
     *       /       /
     * ----[p1]----[p4]----
     *      |       |
     *   a1 |   a   |  a2
     *      |       |
     * ----[p2]----[p3]----
     *
     *
     * apexis p1, p2, p3, p4
     * where p1 = (x1, y1)
     *       p2 = (x2, y2)
     *       p3 = (x3, y3)
     *       p4 = (x4, y4)
     *
     * xyList {x1, y1, x2, y2, x3, y3, x4, y4}
     *         ------  ------  ------  ------
     *           p1      p2      p3      p4
     *            |------||------||------||------|
     * nexts  {      a1,    null,    a2,     a3  }
     *
     * @param worldManager world manager
     * @param id id of this object (it must be unique in world manager WorldManager.getUniqueID("type"))
     * @param cx x of center of this area
     * @param cy y of center of this area
     * @param xyList apexces of this area
     * @param nexts nexts area it must be correspnds xyList
     */
    public TrafficArea(WorldManager worldManager, String id, double cx, double cy, int[] xyList, String[] nexts) {
        super(worldManager, id);
        centerX = cx;
        centerY = cy;
        try {
            List<TrafficAreaNode> nodeBuf = new ArrayList<TrafficAreaNode>();
            List<String> directedEdgeIdList = new ArrayList<String>();
            List<String> notDirectedEdgeIdList = new ArrayList<String>();
            GeneralPath gp = new GeneralPath();

            double lastX = xyList[0];
            double lastY = xyList[1];
            gp.moveTo(lastX, lastY);
            TrafficAreaNode firstNode = new TrafficAreaNode(worldManager);
            firstNode.setLocation(lastX, lastY, 0);
            worldManager.appendWithoutCheck(firstNode);
            nodeBuf.add(firstNode);

            TrafficAreaNode lastNode = firstNode;
            TrafficAreaEdge edge;
            for (int i = 1; i < nexts.length; i++) {
                double x = xyList[i * 2];
                double y = xyList[i * 2 + 1];
                gp.lineTo(x, y);

                TrafficAreaNode node = worldManager.createAreaNode(x, y, 0);

                String nextId = nexts[i - 1];
                if ("rcrs(-1)".equals(nextId)) { // not connector
                    String edgeId = worldManager.getUniqueID("_");
                    edge = new TrafficAreaEdge(worldManager, edgeId);
                    edge.setDirectedNodes(lastNode.getID(), node.getID());
                    edge.setDirectedAreaIDList(new String[]{id});
                    notDirectedEdgeIdList.add(edgeId);
                    worldManager.appendWithoutCheck(edge);
                }
                else if (worldManager.getTrafficObject(nextId) == null) { // connector and it's not exists
                    String edgeId = worldManager.getUniqueID("_");
                    edge = new TrafficAreaEdge(worldManager, edgeId);
                    edge.setDirectedNodes(lastNode.getID(), node.getID());
                    edge.setDirectedAreaIDList(new String[]{id, nextId});
                    directedEdgeIdList.add(edgeId);
                    worldManager.appendWithoutCheck(edge);
                }
                else {  // connector and it's already exists
                    String edgeId = worldManager.getUniqueID("_");
                    edge = new TrafficAreaEdge(worldManager, edgeId);
                    edge.setDirectedNodes(lastNode.getID(), node.getID());
                    edge.setDirectedAreaIDList(id, nextId);
                    directedEdgeIdList.add(edgeId);
                    worldManager.appendWithoutCheck(edge);
                }

                nodeBuf.add(node);
                lastNode = node;
                lastX = x;
                lastY = y;
            }
            String edgeId = worldManager.getUniqueID("_");
            edge = new TrafficAreaEdge(worldManager, edgeId);
            edge.setDirectedNodes(lastNode.getID(), firstNode.getID());
            edge.setDirectedAreaIDList(new String[]{id});
            worldManager.appendWithoutCheck(edge);
            notDirectedEdgeIdList.add(edgeId);
            nodeList = nodeBuf.toArray(new TrafficAreaNode[0]);
            connectorIdList = directedEdgeIdList.toArray(new String[0]);
            unconnectorIdList = notDirectedEdgeIdList.toArray(new String[0]);
            shape = gp;
        }
        catch (WorldManagerException e) {
            alert(e, "error");
        }
    }

    /**
     * add blockade.
     * @param blockade blockade
     */
    public void addBlockade(TrafficBlockade blockade) {
        bockadeList.add(blockade);

        getNeighborWallListCACHE = null;
        for (TrafficArea na : getNeighborList()) {
            na.clearNeighborWallListCache();
        }
    }

    /**
     * remove blockade.
     * @param blockade blockade
     */
    public void removeBlockade(TrafficBlockade blockade) {
        bockadeList.remove(blockade);

        getNeighborWallListCACHE = null;
        for (TrafficArea na : getNeighborList()) {
            na.clearNeighborWallListCache();
        }
    }

    /**
     * set blockade list.
     * @param blockadeList blockade list
     */
    public void setBlockadeList(TrafficBlockade[] blockadeList) {
        bockadeList.clear();
        for (TrafficBlockade blockade : blockadeList) {
            bockadeList.add(blockade);
        }

        getNeighborWallListCACHE = null;
        for (TrafficArea na : getNeighborList()) {
            na.clearNeighborWallListCache();
        }
    }

    /**
     * get blockade list.
     * @return blockade list
     */
    public TrafficBlockade[] getBlockadeList() {
        return bockadeList.toArray(new TrafficBlockade[0]);
    }

    /**
     * check object.
     * @throws Exception has some errors.
     */
    public void checkObject() throws WorldManagerException {
        for (int i = 0; i < connectorIdList.length; i++) {
            String edgeId = connectorIdList[i];
            TrafficAreaEdge edge = (TrafficAreaEdge)getManager().getTrafficObject(edgeId);
            connectorList.add(edge);
        }
        for (int i = 0; i < unconnectorIdList.length; i++) {
            String edgeId = unconnectorIdList[i];
            TrafficAreaEdge edge = (TrafficAreaEdge)getManager().getTrafficObject(edgeId);
            unconnectorList.add(edge);
        }
        /*
          for (String area_id : neighbor_area_id_list_) {
          TrafficArea area = (TrafficArea)getManager().getTrafficObject(area_id);
          neighborAreaList.add(area);
          }
        */
        createUnconnectEdgeList();
        checked = true;
    }

    /**
     * set type of this area.
     * @param t type
     */
    public void setType(String t) {
        type = t;
    }

    /**
     * get type of this area.
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * get center x.
     * @return center x
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     * get center y.
     * @return center y
     */
    public double getCenterY() {
        return centerY;
    }

    /**
     * get distance from an area.
     * return the distance between center points of the areas.
     * @param area area
     * @return distance from the area
     */
    public double getDistance(TrafficArea area) {
        double dx = getCenterX() - area.getCenterX();
        double dy = getCenterY() - area.getCenterY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * get connector(border) between this area and next area.
     * @param area area
     * @return connector(border) which can be plural because an area can have two entrance.
     */
    public TrafficAreaEdge[] getConnector(TrafficArea area) {
        List<TrafficAreaEdge> list = new ArrayList<TrafficAreaEdge>();
        for (TrafficAreaEdge edge : getConnectorEdgeList()) {
            if (edge.getNextArea(this) == area) {
                list.add(edge);
            }
        }
        return list.toArray(new TrafficAreaEdge[0]);
    }

    /**
     * get agent list.
     * @return agent list
     */
    public TrafficAgent[] getAgentList() {
        if (agentListBuf == null) {
            agentListBuf = agentList.toArray(new TrafficAgent[0]);
        }
        return agentListBuf;
    }

    /**
     * add agent into this area.
     * @param agent agent
     */
    public void addAgent(TrafficAgent agent) {
        agentList.add(agent);
        agentListBuf = null;
        for (TrafficAreaListener listener : areaListenerList) {
            listener.entered(new TrafficAreaEvent(this, agent));
        }
    }

    /**
     * remove agent from this area.
     * @param agent agent
     */
    public void removeAgent(TrafficAgent agent) {
        agentList.remove(agent);
        agentListBuf = null;
        for (TrafficAreaListener listener : areaListenerList) {
            listener.exited(new TrafficAreaEvent(this, agent));
        }
    }

    /**
     * this method is not working.
     * @param s space
     */
    public void setSimulateAsOpenSpace(boolean s) {
        throw new RuntimeException("warning: traffic3.object.area.TrafficArea.java: setSimulateAsOpenSpace(boolean)");
        //simulateAsOpenSpace = s;
    }

    /**
     * this megthod is not working.
     * @return simulate as open space
     */
    public boolean isSimulateAsOpenSpace() {
        throw new RuntimeException("warning: traffic3.object.area.TrafficArea.java: isSimulateAsOpenSpace()");
        //return simulateAsOpenSpace;
    }

    /**
     * set properties with xml.
     * @param gmlElement xml
     * @throws Exception exception
     */
    public void setProperties(TagElement gmlElement) throws WorldManagerException {
        // Alert("gml:"+gmlElement, "error");
        String coordinatesText = gmlElement.getTagChild("gml:polygon").getTagChild("gml:LinearRing").getChildValue("gml:coordinates");
        String[] coordinatesTextList = coordinatesText.split(" ");
        GeneralPath gp = new GeneralPath();
        String[] xy = coordinatesTextList[0].split(",");
        double x = Double.parseDouble(xy[0]);
        double y = Double.parseDouble(xy[1]);
        nodeList = new TrafficAreaNode[coordinatesTextList.length];
        gp.moveTo(x, y);
        TrafficAreaNode pos = new TrafficAreaNode(getManager());
        pos.setLocation(x, y, 0);
        getManager().appendWithoutCheck(pos);
        nodeList[0] = pos;
        double xSum = x;
        double ySum = y;
        for (int i = 1; i < coordinatesTextList.length; i++) {
            xy = coordinatesTextList[i].split(",");
            x = Double.parseDouble(xy[0]);
            y = Double.parseDouble(xy[1]);
            xSum += x;
            ySum += y;
            gp.lineTo(x, y);
            pos = getManager().createAreaNode(x, y, 0);
            nodeList[i] = pos;
        }
        centerX = xSum / coordinatesTextList.length;
        centerY = ySum / coordinatesTextList.length;

        TagElement[] directedEdgeTagList = gmlElement.getTagChildren("gml:directedEdge");
        List<String> directedEdgeIdList = new ArrayList<String>();
        List<String> notDirectedEdgeIdList = new ArrayList<String>();
        for (int i = 0; i < directedEdgeTagList.length; i++) {
            String de = directedEdgeTagList[i].getAttributeValue("xlink:href").replaceAll("#", "");
            String or = directedEdgeTagList[i].getAttributeValue("orientation");
            if ("+".equals(or)) {
                directedEdgeIdList.add(de);
            }
            else {
                notDirectedEdgeIdList.add(de);
            }
        }
        connectorIdList = directedEdgeIdList.toArray(new String[0]);
        unconnectorIdList = notDirectedEdgeIdList.toArray(new String[0]);

        /*
          TagElement[] directed_area_tag_list = gmlElement.getTagChildren("gml:directedFace");
          List<String> directed_area_id_list = new ArrayList<String>();
          for (int i = 0; i < directed_area_tag_list.length; i++) {
          String de = directed_area_tag_list[i].getAttributeValue("xlink:href").replaceAll("#", "");
          String or = directed_area_tag_list[i].getAttributeValue("orientation");
          if ("+".equals(or))
          directed_area_id_list.add(de);
          }
          neighbor_area_id_list_ = directed_area_id_list.toArray(new String[0]);
          alert(neighbor_area_id_list_.length ,"error");
        */
        shape = gp;
    }

    private void createUnconnectEdgeList() {
        for (TrafficAreaEdge edge : unconnectorList) {
            for (Line2D line : edge.getLineList()) {
                unconnectEdgeList.add(line);
            }
        }
    }

    /**
     * get connector edge list.
     * @return connector edge list
     */
    public TrafficAreaEdge[] getConnectorEdgeList() {
        return connectorList.toArray(new TrafficAreaEdge[0]);
    }

    /**
     * get unconnector edge list.
     * @return unconnector edge list
     */
    public TrafficAreaEdge[] getUnConnectorEdgeList() {
        return unconnectorList.toArray(new TrafficAreaEdge[0]);
    }

    /**
     * get unconnected edge list as Line2D.
     * @return unconnected edge list
     */
    public Line2D[] getUnconnectedEdgeList() {
        /*
          if (getBlockadeList().length==0) {
          return unconnectEdgeList.toArray(new Line2D[0]);
          }
        */
        List<Line2D> lineList = new ArrayList<Line2D>();
        for (int i = 0; i < unconnectEdgeList.size(); i++) {
            lineList.add(unconnectEdgeList.get(i));
        }
        /*
          for (TrafficArea na : getNeighborList())
          for (TrafficBlockade blockade : na.getBlockadeList())
          for (Line2D line : blockade.getLineList())
          lineList.add(line);
        */
        for (TrafficBlockade blockade : getBlockadeList()) {
            for (Line2D line : blockade.getLineList()) {
                lineList.add(line);
            }
        }
        return lineList.toArray(new Line2D[0]);
    }

    /**
     * clear neighbor wall list cache.
     */
    public void clearNeighborWallListCache() {
        getNeighborWallListCACHE = null;
    }

    /**
     * get neighbor wall list.
     * @return neighbor wall list
     */
    public Line2D[] getNeighborWallList() {
        if (getNeighborWallListCACHE == null) {
            List<Line2D> list = new ArrayList<Line2D>();
            for (Line2D line : getUnconnectedEdgeList()) {
                list.add(line);
            }
            for (TrafficArea na : getNeighborList()) {
                for (Line2D line : na.getUnconnectedEdgeList()) {
                    list.add(line);
                }
            }
            getNeighborWallListCACHE = list.toArray(new Line2D[0]);
        }
        return getNeighborWallListCACHE;
   }

    /**
     * get neighbor list.
     * @return neighbor list
     */
    public TrafficArea[] getNeighborList() {
        Map<String, TrafficArea> neighborAreaList = new HashMap<String, TrafficArea>();
        for (TrafficAreaEdge edge : connectorList) {
            TrafficArea next = edge.getNextArea(this);
            neighborAreaList.put(next.getID(), next);
        }
        return neighborAreaList.values().toArray(new TrafficArea[0]);
    }

    /**
     * get shape.
     * @return shape
     */
    public GeneralPath getShape() {
        return shape;
    }

    /**
     * whether this area include  a point (x, y, z).
     * @param x x
     * @param y y
     * @param z z
     * @return contain
     */
    public boolean contains(double x, double y, double z) {
        return shape.contains(x, y);
    }

    /**
     * get node list.
     * @return node list(apexes) of this area
     */
    public TrafficAreaNode[] getNodeList() {
        return nodeList;
    }

    /**
     * add traffic area listener.
     * @param listener listener
     */
    public void addTrafficAreaListener(TrafficAreaListener listener) {
        areaListenerList.add(listener);
    }

    /**
     * remove traffic area listener.
     * @param listener listener
     */
    public void removeTrafficAreaListener(TrafficAreaListener listener) {
        areaListenerList.remove(listener);
    }

    /**
     * to string.
     * @return explanation
     */
    public String toString() {
        return "TrafficArea[id:" + getID() + ";type:" + getType() + ";]";
    }

    /**
     * to long string.
     * @return explanation
     */
    public String toLongString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<div><div style='font-size:18;'>TrafficArea(id:" + getID() + ")</div>");
        sb.append("type: " + getType() + "<br/>");
        if (isChecked()) {
            sb.append("checked object.<br/>");
        }
        else {
            sb.append("unchecked object.<br/>");
        }
        sb.append("center: (" + centerX + "," + centerY + ")<br/>");

        sb.append("<div style='font-size:15;'>Node List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (int i = 0; i < nodeList.length; i++) {
            sb.append(nodeList[i]).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Connected Edge List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (int i = 0; i < connectorList.size(); i++) {
            TrafficAreaEdge tae = connectorList.get(i);
            if (tae == null) {
                continue;
            }
            sb.append(tae.toLongString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Unconnected Edge List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (int i = 0; i < unconnectorList.size(); i++) {
            sb.append(unconnectorList.get(i).toString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Neighbor area List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (TrafficArea area : getNeighborList()) {
            sb.append(area.toString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Agents</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (TrafficAgent agent : getAgentList()) {
            sb.append(agent.toString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Blockades</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (TrafficBlockade blockade : getBlockadeList()) {
            sb.append(blockade.toString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("</div>");
        return sb.toString();
    }
}
