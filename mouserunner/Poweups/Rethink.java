package mouserunner.Poweups;

import mouserunner.Game.Player;
import mouserunner.Managers.GameplayManager;

/**
 * Rethink is a powerup that erases all arrows from the players and
 * to make them place them again<BR>
 * (Place arrows again)
 * @author Zorek
 */
public class Rethink extends Powerup{
	
	public Rethink() {
		for(Player p: GameplayManager.getInstance().players)
			p.clearArrows();
		duration=0;
	}
	
	@Override
	public boolean update() {
		return isDone();
	}
}
