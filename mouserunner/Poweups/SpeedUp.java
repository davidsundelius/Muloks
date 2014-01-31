package mouserunner.Poweups;

import mouserunner.EventListeners.CommandListener;
import mouserunner.System.Command;

/**
 * SpeedUp is a Powerup that speeds up the game for a short
 * amount of time
 * @author Zorek
 */
public class SpeedUp extends Powerup {
	private CommandListener commandListener;
	
	public SpeedUp(CommandListener commandListener) {
		this.commandListener=commandListener;
		commandListener.commandPerformed(Command.SPEEDUP);
		duration=5;
	}
	
	@Override
	public boolean update() {
		if(isDone()) {
			commandListener.commandPerformed(Command.RESTORESPEED);
			return true;
		} else
			return false;
	}
}
