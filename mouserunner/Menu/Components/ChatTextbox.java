package mouserunner.Menu.Components;

import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import javax.media.opengl.GL;
import mouserunner.Managers.ConfigManager;
import mouserunner.Managers.FontManager;

/**
 * A component for representing a textbox in a menu
 * @author Zorek
 */
public class ChatTextbox extends MenuComponent {

	private TextRenderer text;
	private String value;

	/**
	 * Creates a new textbox, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new button
	 * @param y the position on the y-axis for the new button
	 * @param value the initial value of the textbox
	 */
	public ChatTextbox(final int x, final int y, String value) {
		super(x, y, 265, 30, "MenuCChatTextbox");
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
		this.value = value;
	}

	/**
	 * Called upon component selection
	 */
	@Override
	public void activateComponent(int x, int y) {
		setFocus(true);
	}

	/**
	 * Called character input, appends or substrings the value string
	 * @param c the char that is inputed into the textbox
	 */
	@Override
	public void setCharInput(char c) {
		if (Character.isLetter(c) || Character.isDigit(c) || Character.isSpaceChar(c) || c=='.') {
			if(value.length()<30) {
				value += c;
			}
		} else if (c == '\b' && value.length() != 0) {
			value = value.substring(0, value.length() - 1);
		}
	}

	/**
	 * Returns the current value of the textbox
	 * @return the current value of the textbox
	 */
	public String getValue() {
		return new String(value);
	}
	
	/**
	 * Clears the value of the current textbox
	 */
	public void clear() {
		value="";
	}

	/**
	 * Renders the textbox onto the screen
	 * @param gl the reference obtained by the current gl context
	 */
	@Override
	public void view(GL gl) {
		super.view(gl);
		text.beginRendering(800, 600);
		text.setColor(Color.BLACK);
		text.draw(value, x + width / 2 - (int) text.getBounds(value).getWidth() / 2, y + height / 2 - (int) text.getBounds(value).getHeight() / 2);
		if (active) {
			text.draw("|", x + width / 2 + (int) text.getBounds(value).getWidth() / 2, y + height / 2 - (int) text.getBounds(value).getHeight() / 2);
		}
		text.endRendering();

	}
}
