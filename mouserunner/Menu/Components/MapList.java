package mouserunner.Menu.Components;

import mouserunner.Managers.FontManager;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.media.opengl.GL;
import mouserunner.Managers.ConfigManager;

/**
 * A component for representing a button in a menu
 * @author Zorek
 */
public class MapList extends MenuComponent {
	private TextRenderer text;
	private Map<String,String> map;
	private List<String> list;
	private int cursor;
	
	/**
	 * Creates a new chooselist, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new list
	 * @param y the position on the y-axis for the new list
	 * @param width the width of the new list
	 * @param height the height of the new list
	 */
	public MapList(final int x, final int y, final int width, final int height) {
		super(x,y,width,height, "MenuCList");
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
		map = new HashMap<String,String>();
		list = new ArrayList<String>();
		cursor=-1;
	}

	/**
	 * This will occur when the button is clicked
	 */
	@Override
	public void activateComponent(int x, int y) {
		int upper=super.y+super.height;
		for(int i=0; i<list.size(); i++) {
			if(y<upper-i*20&&y>upper-i*20-20) {
				cursor=i;
				break;
			}
		}
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
	
	public void add(final String key, final String value) {
		map.put(key,value);
		list.add(key);
	}
	
	public void addAll(final Map<String, String> map) {
		this.map.putAll(map);
		for(String s: map.keySet()) {
			list.add(s);
		}
	}
	
	public void remove(int index) {
		map.remove(list.get(index));
		list.remove(index);
		cursor=-1;
	}
	
	public void clear() {
		map.clear();
		list.clear();
	}
	
	public String getValue() {
		if(cursor==-1)
			return null;
		else
			return map.get(list.get(cursor));
	}
	
	@Override
	public void view(GL gl) {
		super.view(gl);
		/*if(cursor!=-1) {
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			gl.glBegin(gl.GL_QUADS);
				gl.glVertex2f(x						,		y						);
				gl.glVertex2f(x+20*cursor	,		y						);
				gl.glVertex2f(x+20*cursor	,		y+20*cursor	);
				gl.glVertex2f(x						,		y+20*cursor	);
			gl.glEnd();
		}*/
		text.beginRendering(800, 600);
		for(int i=0;i<list.size();i++) {
			if(cursor==i)
				text.setColor(Color.RED);
			else
				text.setColor(Color.BLACK);
			text.draw(list.get(i), x+20, y+height-(20*(i+1)));
		}
		text.endRendering();
	}
}
