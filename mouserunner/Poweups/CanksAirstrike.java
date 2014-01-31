package mouserunner.Poweups;

import java.util.Collection;
import mouserunner.Game.Entity.Cat;
import mouserunner.Game.Game;
import mouserunner.Game.Level;
import mouserunner.Game.Player;
import mouserunner.LevelComponents.Nest;


/**
 * CanksAirstrike is a Powerup that makes canks fall down onto all nests
 * except for the nest that got the powerup.<BR>
 * (Cat Attack)
 * @author Zorek
 */
public class CanksAirstrike extends Powerup{
	private Player player;
	private Game game;
	private Level level;
	
	public CanksAirstrike(Player player, Collection<Cat> spawnedCats, Level level, Game game) {
		duration=0;
		this.player=player;
		this.level=level;
		this.game=game;
		for(Nest n: level.getNests()) {
			if(n.getOwner()!= player) {
				Cat cat = new Cat(n,level);
				spawnedCats.add(cat);
			}
		}
	}

	@Override
	public boolean update() {
		if(isDone()) {
			for(Nest n: level.getNests()) {
				if(n.getOwner()!= player) {
					game.spawnBillboard(n.x*Level.tileSize+Level.tileSize/2, n.y*Level.tileSize+Level.tileSize/2, 10.0f, "Assets/Textures/SignNegative.png", "-" + String.valueOf(n.getOwner().catGet()));
					game.spawnSpecialFX("Assets/Scripts/explosion.sfx",n.x*Level.tileSize+Level.tileSize/2, n.y*Level.tileSize+Level.tileSize/2);
				}
			}
			return true;
		}
		return false;
	}
}