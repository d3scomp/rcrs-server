package firesimulator.simulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import firesimulator.util.Rnd;
import firesimulator.world.World;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class Wind {
	
	private static final Log LOG = LogFactory.getLog(Wind.class);
		
	private class Coordinate {
		public final double fractionX;
		public final double fractionY;
		public final int x;
		public final int y;
		
		private double weight;
		
		private Coordinate(int x, int y) {
			this.fractionX = x;
			this.fractionY = y;
			this.x = x;
			this.y = y;
		}
		
		public Coordinate(double x, double y) {
			this.fractionX = x;
			this.fractionY = y;
			// Floor is needed because simple type cast doesn't round down the negative numbers
			this.x = (int) Math.floor(x);
			this.y = (int) Math.floor(y);
			
			// Overlap in the X and Y axis
			double diffX = 1 - (this.fractionX - this.x);
			double diffY = 1 - (this.fractionY - this.y);
			// Weight is the fraction of overlap
			setWeight(diffX, diffY);
		}
		
		public Coordinate xPlusOne() {
			Coordinate next = new Coordinate(x+1, y);
			double diffX = fractionX - x;
			double diffY = 1 - (this.fractionY - this.y);
			next.setWeight(diffX, diffY);
			
			return next;
		}
		
		public Coordinate yPlusOne() {
			Coordinate next = new Coordinate(x, y+1);
			double diffX = 1 - (this.fractionX - this.x);
			double diffY = fractionY - y;
			next.setWeight(diffX, diffY);
			
			return next;
		}
		
		public Coordinate xyPlusOne() {
			Coordinate next = new Coordinate(x+1, y+1);
			double diffX = fractionX - x;
			double diffY = fractionY - y;
			next.setWeight(diffX, diffY);
			
			return next;
		}
		
		private void setWeight(double diffX, double diffY) {
			// Area covered by the diff rectangle in the cell
			// Cells are unitary (with width and height 1)
			weight = diffX * diffY;
		}
		
		public double getWeight() {
			return weight;
		}
		
		@Override
		public String toString() {
			return String.format("(%.2f; %.2f) weight: %f", fractionX, fractionY, weight);
		}
	}
	
	private static final int CIRCLE_MAX_ANGLE = 360; // deg

	public static boolean WIND_RANDOM = false;
	public static double WIND_BIG_CHANGE_PROBABILITY = 0.05;
	
	public static int WIND_SPEED = 0;
	public static int WIND_SPEED_CHANGE = 2000; // mm/step
	public static int WIND_BIG_SPEED_CHANGE = 10000; // mm/step
	public static int WIND_DIRECTION = 0;
	public static int WIND_DIRECTION_CHANGE = 15; // deg
	
	World world;
	/**
	 * Wind speed in (mm/step)
	 */
	int speed;
	/**
	 * Wind direction in degrees.
	 * 0° is North
	 * 90° is East
	 * 180° is South
	 * 270° is West
	 */
	int direction;
	
	public Wind(World world) {
		this.world = world;
	}
	
	/**
	 * Set the given speed and direction.
	 * 
	 * @param speed Speed in mm/step
	 * @param direction Angle in degrees.
	 */
	public void initialize() {
		this.speed = WIND_SPEED;
		this.direction = normalizeDirection(WIND_DIRECTION);
		if(WIND_RANDOM) {
			LOG.info("DYNAMIC WIND");
		} else {
			LOG.info("CONSTANT WIND");
		}
	}
	
	private int normalizeDirection(int direction) {
		while(direction < 0) {
			direction += CIRCLE_MAX_ANGLE;
		}
		
		return direction % CIRCLE_MAX_ANGLE;
	}
	
	public void step() {
		// Beaufort scale:
		// Calm: < 0.3 m/s
		// Light air: 0.3–1.5 m/s
		// Light breeze: 1.6–3.3 m/s
		// Gentle breeze: 3.4–5.5 m/s
		// Moderate breeze: 5.5–7.9 m/s
		// Fresh breeze: 8–10.7 m/s
		// Strong breeze: 10.8–13.8 m/s
		// High wind: 13.9–17.1 m/s
		// Gale: 17.2–20.7 m/s
		// Severe gale: 20.8–24.4 m/s
		// Storm: 24.5–28.4 m/s
		// Violent storm: 28.5–32.6 m/s
		// Hurricane: ≥ 32.7 m/s
		
		// This can be parameterized in the future, now let's keep in Calm - Fresh breeze
		if(WIND_RANDOM) {
			// with higher probability
			 	// change direction by up to 15°
				// change speed by up to 2 m
			// with small probability
				// set random direction
				// set random speed
			if(Rnd.get01() < WIND_BIG_CHANGE_PROBABILITY) {
				int newDirection = (int) (Rnd.get01() * CIRCLE_MAX_ANGLE);
				direction = normalizeDirection(newDirection);
				
				speed = (int) (Rnd.get01() * WIND_BIG_SPEED_CHANGE);
			} else {
				int sign = Rnd.get01() < 0.5 ? 1 : -1;
				int newDirection = (int) (direction + sign * Rnd.get01() * WIND_DIRECTION_CHANGE);
				direction = normalizeDirection(newDirection);
				
				sign = Rnd.get01() < 0.5 ? 1 : -1;
				speed = (int) (speed + sign * Rnd.get01() * WIND_SPEED_CHANGE);
			}
		}
		
		world.setWindForce(speed);
		world.setWindDirection(direction);
	}
	
	public double[][] shiftAirTemperature(double[][] airTemperature){
		int width = airTemperature.length;
		int height = airTemperature[0].length;
		double [][] resultAirTemperature = new double[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				resultAirTemperature[x][y] = getNewTemperature(airTemperature, x, y);
			}
		}
		
		return resultAirTemperature;
	}
	
	/**
	 * The world is divided into air cells. Each cell holds a temperature.
	 * The size of a cell is world.SAMPLE_SIZE by world.SAMPLE_SIZE.
	 * The target cell (for which new temperature is being computed) is
	 * projected against the wind and there it overlaps up to four source cells.
	 * Based on the fraction of area overlap over the source cells a weighted
	 * temperature is taken and summed up with the rest of source cells to get
	 * the resulting temperature.
	 * Draw a picture of cells in grid and shift the target cell to visualize the math ;-)
	 * 
	 * @param airTemperature The grid with air temperatures
	 * @param x X index into the grid
	 * @param y Y index into the grid
	 * 
	 * @return computed temperature of the target air cell 
	 */
	private double getNewTemperature(double[][] airTemperature, int x, int y) {
		
		// Decompose speed and direction to vertical and horizontal vectors
		// Wind speed needs to be expressed as fraction of cell size
		double shift_y = Math.cos(Math.toRadians(direction)) * ((double) speed / world.SAMPLE_SIZE);
		double shift_x = Math.sin(Math.toRadians(direction)) * ((double) speed / world.SAMPLE_SIZE);
		
		// To get the source cells for given target cell, we need to go against
		// the shift vectors (against the wind)
		double source_x = x - shift_x;
		double source_y = y - shift_y;
		
		// source_x and source_y express the placement of the provided target cell
		// shifted against the wind to create source cell. This source cell can overlap
		// up to four cells in the area (each time the source_x and source_y are not integers)
		// Flooring the source_x and source_y provides the bottom left overlapped cell.
		// The rest three overlapped cells will be computed by adding 1 to x and y alternating way.
		
		// Get source cells
		Coordinate source1 = new Coordinate(source_x, source_y);
		Coordinate source2 = source1.xPlusOne();
		Coordinate source3 = source1.yPlusOne();
		Coordinate source4 = source1.xyPlusOne();
		
		// sum the temperatures
		double temperature = 0;
		temperature += getTemperatureAt(airTemperature, source1) * source1.getWeight();
		temperature += getTemperatureAt(airTemperature, source2) * source2.getWeight();
		temperature += getTemperatureAt(airTemperature, source3) * source3.getWeight();
		temperature += getTemperatureAt(airTemperature, source4) * source4.getWeight();
		
		return temperature;
	}
		
	
	private double getTemperatureAt(double[][] airTemperature, Coordinate target){
        if(target.x < 0 || target.x >= airTemperature.length
        	|| target.y < 0 || target.y >= airTemperature[0].length) {
            return World.DEFAULT_TEMPERATURE;
        }
        
        return airTemperature[target.x][target.y];
    }
}
