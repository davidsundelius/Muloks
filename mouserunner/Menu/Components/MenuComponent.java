package mouserunner.Menu.Components;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import java.io.File;
import javax.media.opengl.GL;
import mouserunner.Managers.TextureManager;
import mouserunner.System.Viewable;

/**
 * A abstract class to be a template for creating components such as
 * buttons, lists and comboboxes for the menu interfaces
 * @author Zorek
 */
public abstract class MenuComponent implements Viewable {
	public int x, y;
	public int width, height;
	protected String texturePath;
	protected boolean active;
	
	/**
	 * Template constructor for a component, call super(x,y,texturePath) to use it
	 * @param x the position on the x-axis for the new component
	 * @param y the position on the y-axis for the new component
	 * @param texturePath the path to the texture that will be used on the component
	 */
	public MenuComponent(final int x, final int y, final int width, final int height, final String texturePath) {
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
		this.texturePath=texturePath;
		active=false;
	}
	
	public void setFocus(boolean active) {
		this.active=active;
	}
	
	/**
	 * This will occur when the component is clicked
	 */
	public abstract void activateComponent(int x, int y);
	
	/**
	 * This will occur when a key is typed while this component is focused
	 * @param c the typed character
	 */
	public abstract void setCharInput(final char c);
	
	/**
	 * This will view the component on screen (demands a ortho2d setting on projection matrix)
	 * @param gl a reference provided by the current gl context
	 */
	@Override
	public void view(GL gl) {
		Texture t = TextureManager.getInstance().getTexture(new File("Assets/Textures/"+texturePath+".png"));
		TextureCoords tc= t.getSubImageTexCoords(0, 0, width, height);
		t.enable();
		t.bind();
		if(!active)
			gl.glColor4f(0.9f, 0.9f, 0.9f, 1.0f);
		else
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(tc.left()	, tc.bottom());
		gl.glVertex2i(x, y);
		gl.glTexCoord2f(tc.right(),	tc.bottom());
		gl.glVertex2i(x+width, y);
		gl.glTexCoord2f(tc.right(),	tc.top());
		gl.glVertex2i(x+width, y+height);
		gl.glTexCoord2f(tc.left(),	tc.top());
		gl.glVertex2i(x, y+height);
		gl.glEnd();
		t.disable();
	}
}
