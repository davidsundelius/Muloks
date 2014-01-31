package mouserunner.Poweups;

/**
 * BackupPlan is a Powerup that destroys the players space crafts
 * and makes them place new ones
 * @author Zorek
 */
public class BackupPlan extends Powerup{
	
	
	public BackupPlan() {
		duration=3;
		//No idea at this time, how to implement this
	}

	@Override
	public boolean update() {
		return isDone();
	}
}