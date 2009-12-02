package traffic3.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficArea;
import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.simulator.SimulatorException;

import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

/**
 *
 */
public class TrafficAgent extends TrafficObject {

  /**
   * radius.
   */
  private static final double RADIUS_DEFAULT = 200;

  private static final int D = 3;

  private final double valueAgentA = 0.0001;
  private final double valueAgentB = 1000.0;
  private final double valueAgentK = 0.000001;
  private final double valueRandomX = Math.random();
  private final double valueRandomY = Math.random();

  private final double valueWallA = 0.005; //0.001
  private final double valueWallB = 100.0; //100
  private final double valueWallK = 0.00001;
  private final double valueWallWidth = 0;

  private double stepDistance = 0;
  //  private double stepDistanceMax = 1000;

  /**
   * dest buf.
   */
  private final double[] destBuf = new double[D];

  /**
   * sumop buf.
   */
  private final double[] sumopBuf = new double[D];

  /**
   * sumw buf.
   */
  private final double[] sumwBuf = new double[D];

  /**
   * Location X.
   */
  private double locationX;

  /**
   * Location Y.
   */
  private double locationY;

  /**
   * Location Z.
   */
  private double locationZ;

  /**
   * Velocity X.
   */
  private double velocityX;

  /**
   * Velocity Y.
   */
  private double velocityY;

  /**
   * Velocity Z.
   */
  private double velocityZ;

  /**
   * Fource X.
   */
  private double forceX;

  /**
   * Force Y.
   */
  private double forceY;

  /**
   * Force Z.
   */
  private double forceZ;

  /**
   * Radius.
   */
  private double radius;

  /**
   * Type.
   */
  private String type;

  /**
   * Colorl.
   */
  private Color renderColor;

  /**
   * The destination that this agent wants to go.
   */
  private TrafficAreaNode finalDestination;

  /**
   * now destination.
   */
  private TrafficAreaNode nowDestination;

  /**
   * now destination edge.
   */
  private TrafficAreaEdge nowDestinationEdge;

  /**
   * limit of velocity.
   */
  private double velocityLimit;
  // private double m_ = 10;
  // private TrafficNetworkPoint now_network_point_;

  /**
   * Now area that this agent is in.
   * This field can be null, and it means this agent is not in any existed area.
   */
  private TrafficArea nowArea;

  /**
   * Whether this agent is now network mode or area mode.
   */
  private boolean isNetworkMode;

  /**
   * Log.
   */
  private double totalDistance = 0;

  /**
   * The Last area that agent is in, but except null.
   */
  private TrafficArea lastArea = null;

  /**
   *
   */
  private List<Point2D> positionHistory = new ArrayList<Point2D>();

  /**
   *
   */
  private boolean savePositionHistory = true;

  /**
   * set location count.
   */
  private int setLocationCount = 0;

  /**
   * Constractor.
   * Id, radius, velocityLimit will automatically be set.
   * radius: 200
   * velocityLimit: 0.7+0.1*(Math.random()-0.5);
   * @param worldManager world manager
   */
  public TrafficAgent(WorldManager worldManager) {
    super(worldManager);
    init();
  }

  /**
   * Constractor.
   * @param worldManager world manager
   * @param radius radius
   * @param velocityLimit velicity limit
   */
  public TrafficAgent(WorldManager worldManager, double radius, double velocityLimit) {
    super(worldManager);
    init(radius, velocityLimit);
  }

  /**
   * Constractor.
   * @param id id
   * @param worldManager world manager
   * @param radius radius
   * @param velocityLimit velicity limit
   */
  public TrafficAgent(WorldManager worldManager, String id, double radius, double velocityLimit) {
    super(worldManager, id);
    init(radius, velocityLimit);
  }

  /**
   * Whether this agent is simulated as network model or area model.
   * @return is network mode
   */
  /*
  public boolean isNetworkMode() {
    return this.isNetworkMode;
  }
  */

  /**
   * initialize by default parameter.
   */
  private void init() {
    final double mean = 0.7;
    final double variable = 0.1;
    final double randomMean = 0.5;
    this.radius = RADIUS_DEFAULT; //[mm]
    this.velocityLimit = mean + variable * (Math.random() - randomMean);
    setColor(Color.green);
  }

  /**
   * initialize by specified parameter.
   * @param r radius
   * @param vl velocityLimit
   */
  private void init(double r, double vl) {
    this.radius = r;
    this.velocityLimit = vl;
    setColor(Color.green);
  }

  /**
   * Get limit of velocity.
   * @return limit of velocity.
   */
  public double getVLimit() {
    return this.velocityLimit;
  }

  /**
   * Set limit of velocity.
   * @param vLimit limit of velocity.
   */
  public void setVLimit(double vLimit) {
    this.velocityLimit = vLimit;
  }

  /**
   * Get color.
   * @return color
   */
  public Color getColor() {
    return this.renderColor;
  }

  /**
   * Set color.
   * @param c color
   */
  public void setColor(Color c) {
    this.renderColor = c;
  }

  /**
   * clear log distance. set totalDistance to 0;
   */
  public void clearLogDistance() {
    this.totalDistance = 0;
  }

  /**
   * get log distance.
   * @return distance
   */
  public double getLogDistance() {
    return this.totalDistance;
  }

  /*
    public void setLocation(TrafficNetworkPoint now_network_point) {
    this.isNetworkMode = true;
    //if()
    this.nowDestination = null;
    now_network_point_ = now_network_point;
    double x = now_network_point_.getX();
    double y = now_network_point_.getY();
    double z = now_network_point_.getZ();
    double dx = x-locationX;
    double dy = y-locationY;
    double dz = y-locationY;
    this.totalDistance += Math.sqrt(dx*dx+dy*dy+dz*dz);
    locationX = x;
    locationY = y;
    locationZ = z;
    if(this.nowArea!=null)
    this.nowArea.removeAgent(this);
    //alert("newtwork move: "+now_network_point);
    }
  */

  /**
   * get Area.
   * @return now area
   */
  public TrafficArea getArea() {
    if (this.nowArea != null) {
      this.lastArea = this.nowArea;
      return this.nowArea;
    }
    return this.lastArea;
  }

  /**
   * get position histroy.
   * @return position history
   */
  public Point2D[] getPositionHistory() {
    return positionHistory.toArray(new Point2D[0]);
  }

  /**
   * clear position history.
   */
  public void clearPositionHistory() {
    positionHistory.clear();
  }

  /**
   * set location.
   * @param x location x
   * @param y location y
   * @param z location z
   */
  public void setLocation(double x, double y, double z) {
    final int locationSaveSkipCount = 60;
    setLocationCount++;
    double dx = x - locationX;
    double dy = y - locationY;
    double dz = y - locationY;
    if (setLocationCount % locationSaveSkipCount == 0 && savePositionHistory) {
      positionHistory.add(new Point2D.Double(x, y));
    }

    this.totalDistance += Math.sqrt(dx * dx + dy * dy + dz * dz);

    locationX = x;
    locationY = y;
    locationZ = z;
    if (this.nowArea == null || !this.nowArea.contains(x, y, z)) {
      TrafficArea area = null;
      if (this.nowArea != null) {
        for (TrafficArea a : this.nowArea.getNeighborList()) {
          if (a.contains(x, y, z)) {
            area = a;
          }
        }
      }
      if (area == null) {
        area = getManager().findArea(x, y);
      }

      if (area != null) {
        this.nowDestination = null;
        if (this.nowArea != null) {
          this.nowArea.removeAgent(this);
        }
        this.nowArea = area;
        this.nowArea.addAgent(this);
        /*
          if (this.nowArea == null)
          this.isNetworkMode = false;
          else
          this.isNetworkMode = !this.nowArea.isSimulateAsOpenSpace();
        */
      }
      else {
        if (this.nowArea != null) {
          this.nowArea.removeAgent(this);
        }
        this.nowArea = null;
        log("cannot find area of agents: " + this);
      }
    }
    //now_network_point_ = null;
    //alert("free space move: "+x+","+y+","+z);
  }

  /**
   * get x.
   * @return x
   */
  public double getX() {
    return locationX;
  }

  /**
   * get y.
   * @return y
   */
  public double getY() {
    return locationY;
  }

  /**
   * get z.
   * @return z
   */
  public double getZ() {
    return locationZ;
  }

  /**
   * get fx.
   * @return fx
   */
  public double getFX() {
    return forceX;
  }

  /**
   * get fx.
   * @return fx
   */
  public double getFY() {
    return forceY;
  }

  /**
   * get vx.
   * @return vx
   */
  public double getVX() {
    return velocityX;
  }

  /**
   * get vx.
   * @return vx
   */
  public double getVY() {
    return velocityY;
  }

  /**
   * set radius.
   * @param r radius
   */
  public void setRadius(double r) {
    this.radius = r;
  }

  /**
   * get radius.
   * @return radius
   */
  public double getRadius() {
    return this.radius;
  }

  /**
   * set destination.
   * @param destination destination
   */
  public void setDestination(TrafficAreaNode destination) {
    this.finalDestination = destination;
    this.nowDestination = null;
    //TrafficArea goal = getManager().findArea(destination.getX(), destination.getY());
    plan();
  }

  /**
   * get final destination.
   * @return final destination
   */
  public TrafficAreaNode getFinalDestination() {
    return this.finalDestination;
  }

  /**
   * get now destination.
   * @return now destination
   */
  public TrafficAreaNode getNowDestination() {
    return this.nowDestination;
  }

  /**
   * plan.
   */
  public void plan() {
    if (this.nowDestination == null) {
      try {
        planDestination();
      }
      catch (WorldManagerException e) {
        alert(e, "error");
      }
    }

    //if (this.isNetworkMode) {
    //  plan_network();
    //}
    //else {
      planArea();
    //}
  }

  /*
  public void plan_network() {
    forceX = 0;
    forceY = 0;
    forceZ = 0;
    throw new RuntimeException("not supported network mode");
  }
  */

  /**
   * plan area.
   */
  public void planArea() {
    calcDestinationForce(destBuf);
    calcAgentsForce(sumopBuf);
    calcWallsForce(sumwBuf);

    forceX = destBuf[0] + sumopBuf[0] + sumwBuf[0];
    forceY = destBuf[1] + sumopBuf[1] + sumwBuf[1];
    forceZ = destBuf[2] + sumopBuf[2];

    if (Double.isNaN(forceX) || Double.isNaN(forceY) || Double.isNaN(forceZ)) {
      System.err.println("plan_area(): force is NaN!");
      forceX = 0;
      forceY = 0;
      forceZ = 0;
    }
  }

  private double[] calcDestinationForce(double[] dest) {

    double destx = 0;
    double desty = 0;
    double destz = 0;
    if (this.nowDestination != null) {
      double dx = this.nowDestination.getX() - locationX;
      double dy = this.nowDestination.getY() - locationY;
      //double dz = this.nowDestination.getZ() - locationZ;
      double dist = Math.sqrt(dx * dx + dy * dy);

      if (this.nowDestination != this.finalDestination && this.nowDestinationEdge != null && dist < this.nowDestinationEdge.length() / 2) {
        double edist = this.nowDestinationEdge.distance(locationX, locationY);
        TrafficAreaNode n1 = this.nowDestinationEdge.getDirectedNodes()[0];
        TrafficAreaNode n2 = this.nowDestinationEdge.getDirectedNodes()[1];
        double ndx = n1.getX() - n2.getX();
        double ndy = n1.getY() - n2.getY();
        //double ndz = n1.getZ() - n2.getZ();
        double ndist = Math.sqrt(ndx * ndx + ndy * ndy);
        ndx /= ndist;
        ndy /= ndist;
        //ndz /= ndist;

        double cdx = locationX - n2.getX();
        double cdy = locationY - n2.getY();
        //double cdz = locationZ - n2.getZ();
        double dd = cdx * ndx + cdy * ndy;

        dx = dd * ndx - cdx;
        dy = dd * ndy - cdy;

        assert !(dx == 0 && dy == 0);
        //dz = dd * ndz - cdz;
        dist = Math.sqrt(dx * dx + dy * dy);
      }
      if (dist == 0) {
        dx = 0;
        dy = 0;
      }
      else {
        dx /= dist;
        dy /= dist;
        //dz /= dist;
      }
      final double ddd = 0.001;
      if (this.nowDestination == this.finalDestination) {
        dx = Math.min(this.velocityLimit, ddd * dist) * dx;
        dy = Math.min(this.velocityLimit, ddd * dist) * dy;
        //dz = Math.min(this.velocityLimit, 0.001 * dist) * dz;
      }
      else {
        dx = (1.0) * dx;
        dy = (1.0) * dy;
     // dz = (1.0) * dz;
      }

      //destx = 0.0001*(dx-velocityX);
      //desty = 0.0001*(dy-velocityY);
      //destz = 0.0001*(dz-velocityZ);

      final double sss2 = 0.0002;
      destx = sss2 * (dx - velocityX);
      desty = sss2 * (dy - velocityY);
   // destz = sss2 * (dz - velocityZ);
      assert (!Double.isNaN(velocityX) && !Double.isNaN(velocityY));
      assert (!Double.isNaN(destx) && !Double.isNaN(desty));
    }
    else {
      final double sss = 0.0001;
      destx = sss * (-velocityX);
      desty = sss * (-velocityY);
   // destz = sss * (-velocityZ);
      assert (!Double.isNaN(destx) && !Double.isNaN(desty));
    }

    /*
      if (Double.isNaN(destx) || Double.isNaN(desty)) {
      try{throw new Exception("NaN");}catch(Exception e){e.printStackTrace();}
      destx=desty=0;
      }
    */
    dest[0] = destx;
    dest[1] = desty;
    dest[2] = 0;
    return dest;
  }

  private double[] calcAgentsForce(double[] sumop) {
    double sumopx = 0;
    double sumopy = 0;
    double sumopz = 0;

    if (this.nowArea == null) {
      Arrays.fill(sumop, 0);
      return sumop;
    }

    TrafficArea[] areaList = this.nowArea.getNeighborList();
    for (int j = -1; j < areaList.length; j++) {
      TrafficAgent[] agentList = null;
      if (j == -1) {
        agentList = this.nowArea.getAgentList();
      }
      else {
        agentList = areaList[j].getAgentList();
      }
      double opdx;
      double opdy;
      for (int i = 0; i < agentList.length; i++) {
        TrafficAgent op = agentList[i];
        if (op == this) {
          continue;
        }
        opdx = op.getX() - locationX;
        final double cutoffX = 3000;
        final double cutoffY = 3000;
        if (opdx < -cutoffX || cutoffX < opdx) {
          continue;
        }
        opdy = op.getY() - locationY;
        if (opdy < -cutoffY || cutoffY < opdy) {
          continue;
        }
     // double opdz = op.getZ() - locationZ;
        double r = this.radius + op.getRadius();
        double opdist2 = opdx * opdx + opdy * opdy;
        final double randomMax = 0.001;
        final double randomMean = 0.5;
        if (opdist2 == 0) {
          sumopx += randomMax * (this.valueRandomX - randomMean);
          sumopy += randomMax * (this.valueRandomY - randomMean);
          continue;
        }
        double opdist = Math.sqrt(opdist2);
        double opdxn = opdx / opdist;
        double opdyn = opdy / opdist;
     // double opdzn = opdz / opdist;
        double opp = r - opdist;
        double tmp = -this.valueAgentA * Math.exp(opp / this.valueAgentB);
        if (Double.isInfinite(tmp)) {
          System.out.println("calculateAgentsForce(): A result of exp is infinite: exp(" + (opp / this.valueAgentB) + ")");
        }
        else {
          sumopx += tmp * opdxn;
          sumopy += tmp * opdyn;
        }
        if (opp > 0) {
          sumopx += -this.valueAgentK * (opp) * opdxn;
          sumopy += -this.valueAgentK * (opp) * opdyn;
       // sumopz += -this.valueAgentK * (opp) * opdzn;
        }
      }
    }

    double d2 = sumopx * sumopx + sumopy * sumopy;
 // double d = Math.sqrt(sumopx * sumopx + sumopy * sumopy + sumopz * sumopz);
    final double lim = 0.0001;
    final double lim2 = 0.00000001;
    if (d2 > lim2) {
   // System.out.println("limit force: " + d2);
      final double limit = 0.001;
      double lpd = limit / Math.sqrt(d2);
      sumopx *= lpd;
      sumopy *= lpd;
   // sumopz *= lim / d;
    }
    assert (!Double.isNaN(sumopx) && !Double.isNaN(sumopy));
    sumop[0] = sumopx;
    sumop[1] = sumopy;
    sumop[2] = 0;
    return sumop;
  }


  private double[] calcWallsForce(double[] sumw) {
    //for wall
    double sumwx = 0;
    double sumwy = 0;
    double sumwz = 0;
    if (this.nowArea != null) {
      Line2D[] lineList = this.nowArea.getNeighborWallList();
      double r = getRadius() + valueWallWidth;
      double dx;
      double dy;
      double dist;
      final double cutoffDistance = 3000;
      for (int j = 0; j < lineList.length; j++) {
        Point2D p1 = lineList[j].getP1();
        Point2D p2 = lineList[j].getP2();
        double p1p2X = p2.getX() - p1.getX();
        double p1p2Y = p2.getY() - p1.getY();
        double p1pX =  locationX - p1.getX();
        double p1pY =  locationY - p1.getY();
        double p1p2Dist = Math.sqrt(p1p2X * p1p2X + p1p2Y * p1p2Y);
        if (p1p2Dist == 0) {
          continue;
        }
        double d = (p1p2X * p1pX + p1p2Y * p1pY) / p1p2Dist;
        if (d < 0) {
          dist = p1.distance(locationX, locationY) - r;
          dx = (locationX - p1.getX()) / dist / 2;
          dy = (locationY - p1.getY()) / dist / 2;
        }
        else if (p1p2Dist < d) {
          dist = p2.distance(locationX, locationY) - r;
          dx = (locationX - p2.getX()) / dist / 2;
          dy = (locationY - p2.getY()) / dist / 2;
        }
        else {
          double p1p2NX = p1p2X / p1p2Dist;
          double p1p2NY = p1p2Y / p1p2Dist;
          dx = -d * p1p2NX + p1pX;
          dy = -d * p1p2NY + p1pY;
          dist = Math.sqrt(dx * dx + dy * dy) - r;
          dx /= dist;
          dy /= dist;
          if (Double.isNaN(dist)) {
            System.out.println("c: NaN: Math.sqrt(" + (dx * dx + dy * dy) + "): " + dx + "," + dy + ": " + p1p2Dist);
          }
        }
        if (dist > cutoffDistance) {
          continue;
        }
        if (dist < 0) {
          //System.out.println("mark@");
          sumwx += valueWallK * (dist) * dx;
          sumwy += valueWallK * (dist) * dy;
        }
        else {
          double tmp = valueWallA * Math.exp(-(dist) / valueWallB);
          if (Double.isInfinite(tmp)) {
            System.out.println("calculateWallForce(): A result of exp is infinite: exp(" + (-dist / valueWallB) + ")");
          }
          else if (Double.isNaN(tmp)) {
            System.out.println("calculateWallForce(): A result of exp is NaN: exp(" + (-(dist) / valueWallB) + ")");
          }
          else {
            sumwx += tmp * dx;
            sumwy += tmp * dy;
          }
        }
      }
    }
    if (Double.isNaN(sumwx) || Double.isNaN(sumwy)) {
      sumwx = 0;
      sumwy = 0;
    }
    assert (!Double.isNaN(sumwx) && !Double.isNaN(sumwy));

    sumw[0] = sumwx;
    sumw[1] = sumwy;
    sumw[2] = sumwz;
    return sumw;
  }

  private void planDestination() throws WorldManagerException {
    if (this.finalDestination == null) {
      return;
    }
    TrafficArea start = getManager().findArea(getX(), getY());
    TrafficArea goal = getManager().findArea(this.finalDestination.getX(), this.finalDestination.getY());
    // this should be changed!
    if (start == null || goal == null) {
      this.nowDestination = this.finalDestination;
      return;
    }

    if (start.equals(goal)) {
      this.nowDestination = this.finalDestination;
      return;
    }

    Map<TrafficArea, Double> traceAreaMap = new HashMap<TrafficArea, Double>();
    Map<TrafficArea, TrafficArea> traceTransfer = new HashMap<TrafficArea, TrafficArea>();
    List<TrafficArea> buf = new ArrayList<TrafficArea>();
    traceAreaMap.put(start, 0.0);
    buf.add(start);

    for (int i = 0; traceTransfer.get(goal) == null; i++) {
      TrafficArea[] tmp = buf.toArray(new TrafficArea[0]);
      buf.clear();
      for (TrafficArea target : tmp) {
        double distance = traceAreaMap.get(target);
        List<TrafficArea> neighbors = new ArrayList<TrafficArea>();
        for (TrafficArea t : target.getNeighborList()) {
          neighbors.add(t);
        }

        for (TrafficArea n : neighbors) {
          double newDistance = distance + target.getDistance(n);
          if (traceAreaMap.get(n) == null || newDistance < traceAreaMap.get(n)) {
            traceAreaMap.put(n, newDistance);
            traceTransfer.put(n, target);
            buf.add(n);
          }
        }
      }
      if (i > 1000) {
        throw new RuntimeException("cannot trace to goal.(step>1000)");
      }
    }
    buf.clear();
    TrafficArea last = goal;
    buf.add(last);
    while (last != start) {
      last = traceTransfer.get(last);
      buf.add(last);
    }
    StringBuffer sblog = new StringBuffer("[");
    for (int i = 0; i < buf.size(); i++) {
      sblog.append(buf.get(i).getID() + ",");
    }
    sblog.append("]\n");
    //alert(sblog, "error");
    //log(sblog.toString());
    TrafficArea tnn = buf.get(buf.size() - 2);
    TrafficAreaEdge[] edgeList = tnn.getConnector(buf.get(buf.size() - 1));
    TrafficAreaEdge selected = edgeList[0];
    double min = selected.distance(locationX, locationY);
    for (int i = 1; i < edgeList.length; i++) {
      double distance = edgeList[i].distance(locationX, locationY);
      if (min > distance) {
        min = distance;
        selected = edgeList[i];
      }
    }
    this.nowDestinationEdge = selected;
    TrafficAreaNode[] cons = selected.getDirectedNodes();
    double x = (cons[0].getX() + cons[1].getX()) / 2;
    double y = (cons[0].getY() + cons[1].getY()) / 2;
    TrafficAreaNode tan = getManager().createAreaNode(x, y, 0);
    this.nowDestination = tan;
  }

  /**
   * step.
   * @param dt dt
   */
  public void step(double dt) {
    if (this.isNetworkMode) {
      stepDistance += this.velocityLimit * dt;
      velocityX = 0;
      velocityY = 0;
      velocityZ = 0;
      forceX = 0;
      forceY = 0;
      forceZ = 0;
    }
    else {
      double x = locationX + dt * velocityX;
      double y = locationY + dt * velocityY;
      double z = locationZ + dt * velocityZ;
      velocityX += dt * forceX;
      velocityY += dt * forceY;
      velocityZ += dt * forceZ;
      double v = Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
      if (v > this.velocityLimit) {
        //System.out.println("v limit");
        v /= this.velocityLimit;
        velocityX /= v;
        velocityY /= v;
        velocityZ /= v;
      }
      setLocation(x, y, z);
    }
  }

  /**
   * set type.
   * @param type type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * get type.
   * @return type
   */
  public String getType() {
    return this.type;
  }

  /**
   * is checked.
   * @return is checked
   */
  public boolean isChecked() { return checked; }

  /**
   * check validation.
   * @throws Exception failed to check validation
   */
  public void checkObject() throws WorldManagerException { checked = true; }

  /**
   * to string.
   * @return string
   */
  public String toString() {
    StringBuffer sb = new StringBuffer("TrafficAgent[");
    sb.append("id:").append(getID()).append(";");
    sb.append("x:").append(getX()).append(";");
    sb.append("y:").append(getY()).append(";");
    sb.append("]");
    return sb.toString();
  }

  /**
   * to long string.
   * @return long string
   */
  public String toLongString() {
    StringBuffer sb = new StringBuffer("TrafficAgent[");
    sb.append(getID());
    sb.append("x:").append(getX()).append(";");
    sb.append("y:").append(getY()).append(";");
    sb.append("z:").append(getZ()).append(";");
    sb.append("now area:").append(this.nowArea).append(";");
    sb.append("is network mode:").append(this.isNetworkMode).append(";");
    sb.append("now destination:").append(this.nowDestination).append(";");
    sb.append("final destination:").append(this.finalDestination).append(";");
    //sb.append("now area:").append().append(";");
    sb.append("]");
    return sb.toString();
  }
}
