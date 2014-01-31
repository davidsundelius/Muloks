package mouserunner.EventListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import mouserunner.Menu.Menu;

/**
 * A listener used to control the keyboard input in the menu
 * @author Zorek
 */
public class MenuKeyListener implements KeyListener {
	private Menu menu;

	/**
	 * Empty constructor
	 */
	public MenuKeyListener(Menu menu) {
		this.menu=menu;
	}

	/**
	 * Reacts to different keyboard inputs (pressed keys)
	 * @param e gets information from OS about the action
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		//Menu navigation
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			menu.subMenuPointer();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			menu.addMenuPointer();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			menu.executeEvent();
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.out.println("---MouseRunner exited, user pressed the escape-key---");
			System.exit(0);
		}
	}

	/**
	 * Reacts to different keyboard inputs (released keys)
	 * @param e gets information from OS about the action
	 */
	@Override
	public void keyReleased(KeyEvent e) {
	}

	/**
	 * Used for textinput, proceeds the char to the selected component
	 * @param e KeyEvent
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		menu.registerKeyType(e.getKeyChar());
	}
}
