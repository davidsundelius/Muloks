package mouserunner.Menu.Components;

import mouserunner.Managers.FontManager;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import javax.media.opengl.GL;
import mouserunner.Managers.ConfigManager;

/**
 * A component for representing a checkbox in a menu
 * @author Zorek
 */
public class Checkbox extends MenuComponent {
	
	private TextRenderer text;
	private String checkboxText;
	private boolean state;
	/**
	 * Creates a new checkbox, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new button
	 * @param y the position on the y-axis for the new button
	 * @param checkboxText the text that will be written next to the checkbox
	 */
	public Checkbox(final int x, final int y, final String checkboxText) {
		super(x,y,20,20, "MenuCCheckboxUnchecked");
		this.checkboxText=checkboxText;
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
		state=false;
	}

	/**
	 * This will occur when the checkbox is clicked
	 */
	@Override
	public void activateComponent(int x, int y) {
		if(state)
			texturePath="GuiCheckBoxUnchecked";
		else
			texturePath="GuiCheckBoxChecked";
		state=!state;
	}

	/**
	 * This will occur when a key is typed while this component is focused,
	 * (not used for a chackbox)
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
		text.draw(checkboxText, x+30, y);
		text.endRendering();
	}
	
	public boolean getState() {
		return state;
	}
}
