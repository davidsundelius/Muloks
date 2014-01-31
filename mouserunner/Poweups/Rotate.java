package mouserunner.Poweups;

import mouserunner.Game.Camera;
import mouserunner.System.Direction;

/**
 * Rotate is a Powerup that rotates the playfield 2pi for a short
 * amount of time
 * @author Zorek
 */
public class Rotate extends Powerup{
	private Camera camera;
	
	public Rotate(Camera camera) {
		this.camera=camera;
		camera.setDirection(Direction.UP);
		duration=10;
	}

	@Override
	public boolean update() {
		if(isDone()) {
			camera.setDirection(Direction.DOWN);
			return true;
		}
		return false;
	}
}