package mouserunner.EventListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import mouserunner.Menu.Credits;

/**
 * A listener used to control the keyboard input on the title screen
 * @author Zorek
 */
public class CreditsKeyListener implements KeyListener {
	
	private Credits credits;
	
	
  /**
   * Empty constructor
   */
  public CreditsKeyListener(Credits credits) {
		this.credits = credits;
  }


  /**
   * Reacts to different keyboard inputs (pressed keys)
   * @param e gets information from OS about the action
   */
	@Override
  public void keyPressed(KeyEvent e) {
    credits.skipCredits();
  }

  /**
   * Reacts to different keyboard inputs (released keys)
   * @param e gets information from OS about the action
   */
	@Override
  public void keyReleased(KeyEvent e) {
  }

  /**
   * Unimplemented
   * @param e KeyEvent
   */
	@Override
  public void keyTyped(KeyEvent e) {
  }
}
