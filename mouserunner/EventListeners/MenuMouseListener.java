package mouserunner.EventListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import mouserunner.Managers.ConfigManager;
import mouserunner.Menu.Menu;

/**
 * A listener used to control the mouse button input in the menu
 * @author Zorek
 */
public class MenuMouseListener implements MouseListener {

	private Menu menu;

	public MenuMouseListener(Menu menu) {
		this.menu = menu;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		menu.registerMouseClick(e.getX(), ConfigManager.getInstance().height - e.getY());
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
