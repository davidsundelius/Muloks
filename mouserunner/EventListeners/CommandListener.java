package mouserunner.EventListeners;

import mouserunner.Game.Game;
import java.util.EventListener;
import mouserunner.Menu.Menu;
import mouserunner.Menu.Title;
import mouserunner.System.Updater;
import mouserunner.System.Command;
import mouserunner.Managers.GameplayManager;
import mouserunner.Managers.SoundManager;
import mouserunner.Menu.Credits;
import mouserunner.Menu.Statistics;
import mouserunner.Menu.Lobby;

/**
 * A listener used to control the game and updater. Any class
 * with a commandlistener can send a command, the CommandListener then
 * setup all the affected objects accordingly
 * For known commands: see javadoc for Command
 * @author Zorek
 */
public class CommandListener implements EventListener {

	protected Updater updater;
	protected Game game;

	/**
	 * A empty constructor
	 */
	public CommandListener() {
	}

	/**
	 * Retrieves information of an updater
	 * @param updater affected updater
	 */
	public void setUpdater(Updater updater) {
		this.updater = updater;
	}

	/**
	 * Retrieves information of a game
	 * @param game affected game
	 */
	public void setGame(Game game) {
		this.game = game;
	}

	/**
	 * Performs the changes to affected objects according to sent command
	 * @param c sent command
	 */
	public void commandPerformed(Command c) {
		System.out.println("CommandListener executes command: " + c);
		switch (c) {
			case NEWGAME:
				SoundManager.getInstance().stopSounds();
				updater.setState(new Game(null));
				break;
			case NEXTGAME:
				SoundManager.getInstance().stopSounds();
				if(GameplayManager.getInstance().networkClient!=null && GameplayManager.getInstance().networkClient.isHost())
					if(GameplayManager.getInstance().currentGameIndex<GameplayManager.getInstance().levels.size()) {
						updater.setState(new Game(null));
					} else {
						commandPerformed(Command.ENDTOURNAMENT);
					}
				else {
					if (GameplayManager.getInstance().nextGame()) {
						updater.setState(new Game(null));
					} else {
						commandPerformed(Command.ENDTOURNAMENT);
					}
				}
				break;
			case ENDTOURNAMENT:
				SoundManager.getInstance().stopSounds();
				game = null;
				updater.setState(new Statistics());
				break;
			case GOTOTITLE:
				SoundManager.getInstance().stopSounds();
				game = null;
				Title title = new Title();
				updater.setState(title);
				title.startIntro();
				break;
			case GOTOMENU:
				SoundManager.getInstance().stopSounds();
				game = null;
				updater.setState(new Menu());
				break;
			case GOTOCREDITS:
				SoundManager.getInstance().stopSounds();
				game = null;
				updater.setState(new Credits());
				break;
			case GOTOLOBBY:
				SoundManager.getInstance().stopSounds();
				game = null;
				updater.setState(new Lobby());
				break;
			case PAUSE:
				updater.setRunning(false);
				GameplayManager.getInstance().gameTimer.pause();
				break;
			case UNPAUSE:
				updater.setRunning(true);
				GameplayManager.getInstance().gameTimer.unPause();
				break;
			case SLOWDOWN:
				updater.setTps(950);
				break;
			case SPEEDUP:
				updater.setTps(1010);
				break;
			case RESTORESPEED:
				updater.setTps(1000);
				break;
		}
	}
}

