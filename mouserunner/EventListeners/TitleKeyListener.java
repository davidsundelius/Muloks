package mouserunner.EventListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import mouserunner.Menu.Title;

/**
 * A listener used to control the keyboard input on the title screen
 * @author Zorek
 */
public class TitleKeyListener implements KeyListener {
	
	private Title title;
	
	
  /**
   * Empty constructor
   */
  public TitleKeyListener(Title title) {
		this.title=title;
  }


  /**
   * Reacts to different keyboard inputs (pressed keys)
   * @param e gets information from OS about the action
   */
	@Override
  public void keyPressed(KeyEvent e) {
    title.skipIntro();
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
