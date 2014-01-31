package mouserunner.Menu.Components;

/**
 * A component for representing a button in a menu
 * @author Zorek
 */
public class SendButton extends Button {
	/**
	 * Creates a new button, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new button
	 * @param y the position on the y-axis for the new button
	 * @param buttonText the text that will be written on the button
	 */
	public SendButton(final int x, final int y, final String buttonText) {
		super(x,y, buttonText);
		width=75;
		texturePath = "MenuCSendButton";
	}
}