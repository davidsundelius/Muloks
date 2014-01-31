package mouserunner.Poweups;

import mouserunner.Game.Game;
import mouserunner.System.Timer;

/**
 * MulokRetreat is a Powerup that makes the spawn points only to spawn
 * muloks for a short amount of time.<BR>
 * (MouseMania)
 * @author Zorek
 */
public class MulokRetreat extends Powerup {
	private Game game;
	private Timer updateTimer;
	
	public MulokRetreat(Game game) {
		this.game=game;
		updateTimer = new Timer();
		duration=10;
		game.setSpawning(false);
	}
	
	@Override
	public boolean update() {
		if(updateTimer.read()>100) {
			game.spawnEntity(0);
			updateTimer.setTimestamp();
		}
		if(isDone()) {
			game.setSpawning(true);
			return true;
		}
		return false;
	}
}
