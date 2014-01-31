package mouserunner.EventListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import mouserunner.Menu.Title;

/**
 * A listener used to control the mouse button input on the titlescreen
 * @author Zorek
 */

public class TitleMouseListener implements MouseListener{

	private Title title;
	
	/**
   * Empty constructor
   */
	public TitleMouseListener(Title title) {
		this.title=title;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		title.skipIntro();
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
