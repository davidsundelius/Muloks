package mouserunner.Poweups;

import mouserunner.Game.Game;
import mouserunner.System.Timer;

/**
 * CanksAmbush is a Powerup that makes spawn points only spawn canks for
 * a short amount of time<BR>
 * (CatMania)
 * @author Zorek
 */
public class CanksAmbush extends Powerup{
	private Game game;
	private Timer updateTimer;
	
	public CanksAmbush(Game game) {
		this.game=game;
		updateTimer = new Timer();
		duration=10;
		game.setSpawning(false);
	}

	@Override
	public boolean update() {
		if(updateTimer.read()>1000) {
			game.spawnEntity(1);
			updateTimer.setTimestamp();
		}
		if(isDone()) {
			game.setSpawning(true);
			return true;
		}
		return false;
	}
}