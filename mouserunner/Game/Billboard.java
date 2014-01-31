package mouserunner.Game;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import java.awt.Font;
import java.io.File;
import javax.media.opengl.GL;
import mouserunner.Managers.TextureManager;
import mouserunner.Managers.FontManager;
import mouserunner.System.Timer;
import mouserunner.System.Updatable;
import mouserunner.System.Viewable;

/**
 * Billboard is a class representing a billboarded textured square
 * that can be placed anywhere on the field
 * @author Zorek
 */
public class Billboard implements Updatable, Viewable {

	private float alpha;
	private Timer timer;
	private String texture;
	private String text;
	private TextRenderer textRenderer;
	private float x,y,z,zoffset;
	private float[] matrix = new float[16];

	/**
	 * A protected empty constructor used to be able to create subclasses from
	 * this class without using the ordanary constructor
	 */
	protected Billboard() {
	}
	
	/**
	 * Constructs a new billboard, initiates the fontrenderer, its timer and
	 * sets its other initial values
	 * @param texture the path to the billboarded texture
	 * @param text the text that will be written on the billboard
	 * @param x the x value of the billboard
	 * @param y the y value of the billboard
	 */
	public Billboard(String texture, String text, float x, float y, float z) {
		this.texture = texture;
		this.text = text;
		if(text!=null) {
			textRenderer = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.PLAIN, 16));
		}
		this.x=x;
		this.y=y;
		zoffset=z;
		timer=new Timer();
		calculateAlpha();
	}

	@Override
	public boolean update() {
		//Do nothing right now
		if(timer.read()>2000)
			return true;
		if(texture.equals("Assets/Textures/MouseGhost.png"))
			z=(float)(timer.read()/100.0f)+zoffset;
		else
			z=zoffset;
		calculateAlpha();
		return false;
	}

	@Override
	public void view(GL gl) {
		gl.glPushMatrix();
		gl.glDepthMask(false);
		gl.glTranslatef(x, y, z);
		setBillboardMatrix(gl);
		
		gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
		Texture t = TextureManager.getInstance().getTexture(new File(texture));
		t.bind();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 0.0f	,-5.0f , 0.0f);
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(	10.0f	,-5.0f , 0.0f);
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(	10.0f	, 5.0f, 0.0f);
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 0.0f	, 5.0f, 0.0f);
		
		gl.glEnd();
		gl.glDepthMask(true);
		gl.glPopMatrix();
		
		
		/*if(text!=null) {
			textRenderer.begin3DRendering();
			textRenderer.setColor(1.0f, 0.8f, 0.0f, alpha);
			textRenderer.draw3D(text, (float)(x-textRenderer.getBounds(text).getX()/2), (float)(y+textRenderer.getBounds(text).getY()/2), z, 0.5f);
			textRenderer.end3DRendering();
		}*/
	}
	
	/**
	 * Calculates a new ModelView matrix for rendering of billboarded
	 * objects. Essentially sets every element except the translation ones to
	 * the identity matrix on the current modelview matrix.
	 * @param gl the reference to the current gl context
	 */
	protected void setBillboardMatrix(GL gl) {
		gl.glGetFloatv(gl.GL_MODELVIEW_MATRIX, matrix, 0);
		matrix[0] = 1.0f; matrix[1] = 0.0f; matrix[2] = 0.0f;
		matrix[4] = 0.0f; matrix[5] = 1.0f; matrix[6] = 0.0f;
		matrix[8] = 0.0f; matrix[9] = 0.0f; matrix[10] = 1.0f;
		gl.glLoadMatrixf(matrix, 0);
	}

	/**
	 * Calculates the new alpha value of the billboard.
	 */
	private void calculateAlpha() {
		if(timer.read()>1000)
			alpha=(1000.0f-((float)timer.read()-1000.0f))/1000.f;
		else
			alpha = 1.0f;
	}
}
