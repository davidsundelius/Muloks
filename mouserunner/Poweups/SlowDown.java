package mouserunner.Poweups;

import mouserunner.EventListeners.CommandListener;
import mouserunner.System.Command;

/**
 * SlowDown is a Powerup that slows the game down for a short
 * amount of time.
 * @author Zorek
 */
public class SlowDown extends Powerup {
	private CommandListener commandListener;
	
	public SlowDown(CommandListener commandListener) {
		this.commandListener=commandListener;
		commandListener.commandPerformed(Command.SLOWDOWN);
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
