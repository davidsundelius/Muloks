package Server;

import mouserunner.EventListeners.CommandListener;
import mouserunner.Game.Game;
import mouserunner.Managers.GameplayManager;
import mouserunner.System.Command;

/**
 *
 * @author Zorek
 */
public class ServerCommandListener extends CommandListener{
	Server server;
	
	public ServerCommandListener(Server server) {
		this.server=server;
	}
	
	/**
	 * Performs the changes to affected objects according to sent command
	 * @param c sent command
	 */
	@Override
	public void commandPerformed(Command c) {
		System.out.println("ServerCommandListener executes command: " + c);
		switch (c) {
			case NEXTGAME:
				if (GameplayManager.getInstance().nextGame()) {
					//Start a new game on the server
					server.startGame();
				} else {
					commandPerformed(Command.ENDTOURNAMENT);
				}
				break;
			case ENDTOURNAMENT:
				server.sendStatistics();
				server.broadcastMessage(7);
				server.shutdown();
				server = null;
				break;
			case PAUSE:
				//updater.setRunning(false);
				GameplayManager.getInstance().gameTimer.pause();
				break;
			case UNPAUSE:
				//updater.setRunning(true);
				GameplayManager.getInstance().gameTimer.unPause();
				break;
			case SLOWDOWN:
				server.interval=1500;
				break;
			case SPEEDUP:
				server.interval=900;
				break;
			case RESTORESPEED:
				server.interval=1000;
				break;
		}
	}
}
