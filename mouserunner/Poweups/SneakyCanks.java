package mouserunner.Poweups;

import java.util.HashSet;
import mouserunner.Game.Entity.Cat;


/**
 * SneakyCanks is a Powerup that makes canks invisible for a short amount
 * of time.
 * @author Zorek
 */
public class SneakyCanks extends Powerup{
	HashSet<Cat> cats;
	
	public SneakyCanks(HashSet<Cat> cats) {
		duration=5;
		this.cats=cats;
		for(Cat c: cats) {
			c.setFade(true);
		}
	}

	@Override
	public boolean update() {
		if(isDone()) {
			for(Cat c: cats) {
				c.setFade(false);
			}
			return true;
		}
		return false;
	}
}