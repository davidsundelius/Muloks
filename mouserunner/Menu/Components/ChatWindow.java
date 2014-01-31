package mouserunner.Menu.Components;

import mouserunner.Managers.FontManager;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;
import java.util.List;
import javax.media.opengl.GL;
import mouserunner.Managers.ConfigManager;

/**
 * A component for representing a chat window in a menu
 * @author Zorek
 */
public class ChatWindow extends MenuComponent {
	private TextRenderer text;
	private List<String> list;
	
	/**
	 * Creates a new chat window, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new chatwindow
	 * @param y the position on the y-axis for the new chatwindow
	 */
	public ChatWindow(final int x, final int y) {
		super(x,y,350,260, "MenuCChatWindow");
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
		list = new LinkedList<String>();
	}

	/**
	 * This will occur when the chatwindow is clicked
	 * @param x the inputs position on the x-axis
	 * @param y the inputs position on the y-axis
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
	
	public void addMessage(final String message) {
		if(list.size()>5)
			list.remove(0);
		list.add(message);
	}
	
	public void clear() {
		list.clear();
	}
	
	@Override
	public void view(GL gl) {
		super.view(gl);
		text.beginRendering(800, 600);
		for(int i=0;i<list.size();i++) {
			text.setColor(Color.BLACK);
			text.draw(list.get(i), x+20, y+height-(20*(i+1)));
		}
		text.endRendering();
	}
}
