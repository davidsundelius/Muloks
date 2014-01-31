package mouserunner.Poweups;

import mouserunner.System.Timer;
import mouserunner.System.Updatable;

/**
 * A abstract class to discribe and genralize powerups
 * @author Zorek
 */
public abstract class Powerup implements Updatable {
	public static final int numPowerups = 10; /** the number of powerups available in the game at this moment */
	protected Timer powerUpTimer = new Timer();
	protected int duration;
	
	protected boolean isDone() {
		if(powerUpTimer.read()/1000>duration) {
			System.out.println("Powerup effect took off");
			return true;
		}
		return false;
	}
}
