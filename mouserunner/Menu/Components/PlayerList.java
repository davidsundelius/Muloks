package mouserunner.Menu.Components;

import mouserunner.Managers.FontManager;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL;
import mouserunner.Game.Player;
import mouserunner.Managers.ConfigManager;

/**
 * A component for representing a player list in the lobby
 * @author Zorek
 */
public class PlayerList extends MenuComponent {
	private TextRenderer text;
	private List<Player> list;
	
	/**
	 * Creates a new player list, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new list
	 * @param y the position on the y-axis for the new list
	 */
	public PlayerList(final int x, final int y) {
		super(x,y,150,300, "MenuCPlayerList");
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
		list = new ArrayList<Player>();
	}

	/**
	 * This will occur when the player list is clicked,
	 * (not used for a player list)
	 */
	@Override
	public void activateComponent(int x, int y) {
		return;
	}

	/**
	 * This will occur when a key is typed while this component is focused,
	 * (not used for a player list)
	 * @param c the typed character
	 */
	@Override
	public void setCharInput(final char c) {
		return;
	}
	
	public void add(final Player object) {
		list.add(object);
	}
	
	public void addAll(final List<Player> objects) {
		list.addAll(objects);
	}
	
	public void removeSelected(Player object) {
		list.remove(object);
	}
	
	public void clear() {
		list.clear();
	}
	
	@Override
	public void view(GL gl) {
		super.view(gl);
		text.beginRendering(800, 600);
		
		for(int i=0;i<list.size();i++) {
			text.setColor(list.get(i).getColor());
			text.draw(list.get(i).getName(), x+20, y+height-(20*(i+1)));
		}
		text.endRendering();
	}
}
