package mouserunner.EventListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import mouserunner.Menu.Credits;

/**
 * A listener used to control the mouse button input on the titlescreen
 * @author Zorek
 */

public class CreditsMouseListener implements MouseListener{

	private Credits credits;
	
	/**
   * Empty constructor
   */
	public CreditsMouseListener(Credits credits) {
		this.credits = credits;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		credits.skipCredits();
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
