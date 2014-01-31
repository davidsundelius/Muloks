package mouserunner.EventListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import mouserunner.Game.Game;

/**
 * A listener used to control the mouse motion input (used for debugging)
 * @author Zorek
 */
public class GameMouseMotionListener implements MouseMotionListener{
	private Game game;
	
  /**
   * Creates a new MouseMotionListener object
   */  
  public GameMouseMotionListener(Game game) {
		this.game=game;
  }
  
  /**
   * Registers and saves mouse position
   * @param e MouseEvent sent from awt
   */
	@Override
  public void mouseMoved(MouseEvent e) {
    game.registerMouseMotion(e.getX(),e.getY());
  }
  
  /**
   * Unimplemented
   * @param e MouseEvent
   */
	@Override
  public void mouseDragged(MouseEvent e) {
  }

}
