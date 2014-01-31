package mouserunner.Menu.Components;

import mouserunner.Managers.FontManager;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import javax.media.opengl.GL;
import mouserunner.Managers.ConfigManager;

/**
 * A component for representing a ordanary list in a menu
 * @author Zorek
 */
public class List extends MenuComponent {
	private TextRenderer text;
	protected java.util.List<String> list;
	protected int cursor;
	
	/**
	 * Creates a new list, sets its position, dimensions and texturepath
	 * @param x the position on the x-axis for the new list
	 * @param y the position on the y-axis for the new list
	 */
	public List(final int x, final int y) {
		super(x,y,200,240, "MenuCList");
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
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
	
	public void add(final String value) {
		list.add(value);
	}
	
	public void addAll(final java.util.List<String> list) {
		this.list.addAll(list);
	}
	
	public void addAll(final String[] list) {
		for(String s: list)
			this.list.add(s);
	}
	
	public void remove() {
		if(cursor!=-1) {
			list.remove(cursor);
			cursor=-1;
		}
	}
	
	public void clear() {
		list.clear();
	}
	
	public int size() {
		return list.size();
	}
	
	public String get(int index) {
		return list.get(index);
	}
	
	public String getValue() {
		if(cursor==-1)
			return null;
		else
			return list.get(cursor);
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
