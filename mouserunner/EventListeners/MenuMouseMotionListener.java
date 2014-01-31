package mouserunner.EventListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import mouserunner.Managers.ConfigManager;
import mouserunner.Menu.Menu;

/**
 * A listener used to control the mouse motion input in the menus
 * @author Zorek
 */
public class MenuMouseMotionListener implements MouseMotionListener{
	private Menu menu;

  /**
   * Creates a new MouseMotionListener object
   */  
  public MenuMouseMotionListener(Menu menu) {
		this.menu=menu;
  }
  
  /**
   * Registers and saves mouse position
   * @param e MouseEvent sent from awt
   */
	@Override
  public void mouseMoved(MouseEvent e) {
		menu.registerMouseMotion(e.getX(), ConfigManager.getInstance().height-e.getY());
  }
  
  /**
   * Unimplemented
   * @param e MouseEvent
   */
	@Override
  public void mouseDragged(MouseEvent e) {
  }
}
