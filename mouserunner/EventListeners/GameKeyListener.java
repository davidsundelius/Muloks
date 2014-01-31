package mouserunner.EventListeners;

import mouserunner.Game.Game;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A listener used to control the keyboard input ingame
 * @author Zorek
 */
public class GameKeyListener implements KeyListener {

	private Game game;

	/**
	 * Inserts control for a game
	 * @param game affected game
	 */
	public GameKeyListener(Game game) {
		this.game = game;
	}

	/**
	 * Reacts to different keyboard inputs (pressed keys)
	 * @param e gets information from OS about the action
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		game.registerKeyState(e.getKeyCode(),true);
	}

	/**
	 * Reacts to different keyboard inputs (released keys)
	 * @param e gets information from OS about the action
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		game.registerKeyState(e.getKeyCode(),false);
	}

	/**
	 * Unimplemented
	 * @param e KeyEvent
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}
}
