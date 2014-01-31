package mouserunner.EventListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import mouserunner.Game.Game;

/**
 * A listener used to control the mouse button input on the titlescreen
 * @author Zorek
 */

public class GameMouseListener implements MouseListener{

	private Game game;
	
	public GameMouseListener(Game game) {
		this.game=game;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		game.registerMouseClick(e.getX(), e.getY());
	}
	
	/**
	 * Unimplemented
	 * @param e MouseEvent
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

	}
	
	/**
	 * Unimplemented
	 * @param e MouseEvent
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	/**
	 * Unimplemented
	 * @param e MouseEvent
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	/**
	 * Unimplemented
	 * @param e MouseEvent
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

}
