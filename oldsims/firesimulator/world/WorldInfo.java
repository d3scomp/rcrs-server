package firesimulator.world;

/**
 * @author tn
 *
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class WorldInfo extends RescueObject {

	private int windForce;
	private int windDirection;

	public WorldInfo(int id) {
		super(id);
		windForce = 0;
		windDirection = 0;
	}

	public String getType() {
		return "WORLD";
	}

	public void setWindForce(int windForce) {
		this.windForce = windForce;
	}

	public int getWindForce() {
		return windForce;
	}

	public void setWindDirection(int windDirection) {
		this.windDirection = windDirection;
	}

	public int getWindDirection() {
		return windDirection;
	}
}
