package mouserunner.Poweups;

import mouserunner.Game.Game;
import mouserunner.Game.Player;
import mouserunner.System.Timer;

/**
 * Favoured spaceship is a powerup that makes the nest of the player
 * that got the powerup to absorb alot of muloks for a short amount of
 * time<BR>
 * (MouseMonopoly)
 * @author Zorek
 */
public class FavouredSpacecraft extends Powerup {
	private Timer updateTimer;
	private Player player;
	private Game game;
	
	public FavouredSpacecraft(Player player, Game game) {
		this.player=player;
		this.game=game;
		updateTimer = new Timer();
		duration=5;
		game.setSpawning(false);
	}

	@Override
	public boolean update() {
		if(updateTimer.read()>100) {
			player.mouseGet();
			updateTimer.setTimestamp();
		}
		if(isDone()) {
			game.setSpawning(true);
			return true;
		}
		return false;
	}
}