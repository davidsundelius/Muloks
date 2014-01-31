package mouserunner.Menu.Components;

import mouserunner.Managers.FontManager;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import javax.media.opengl.GL;
import mouserunner.Managers.ConfigManager;

/**
 * A component for representing a button in a menu
 * @author Zorek
 */
public class Button extends MenuComponent {
	
	protected TextRenderer text;
	protected String buttonText;
	/**
	 * Creates a new button, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new button
	 * @param y the position on the y-axis for the new button
	 * @param buttonText the text that will be written on the button
	 */
	public Button(final int x, final int y, final String buttonText) {
		super(x,y,150,30, "MenuCButton");
		this.buttonText=buttonText;
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
	}

	/**
	 * This will occur when the button is clicked
	 */
	@Override
	public void activateComponent(int x, int y) {
	}

	/**
	 * This will occur when a key is typed while this component is focused,
	 * (not used for a button)
	 * @param c the typed character
	 */
	@Override
	public void setCharInput(final char c) {
		return;
	}
	
	@Override
	public void view(GL gl) {
		super.view(gl);
		text.beginRendering(800, 600);
		text.setColor(Color.BLACK);
		text.draw(buttonText, x+width/2-(int)text.getBounds(buttonText).getWidth()/2, y+height/2-(int)text.getBounds(buttonText).getHeight()/2);
		text.endRendering();
	}
}
