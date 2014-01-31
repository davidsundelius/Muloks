package mouserunner.Game;

import com.sun.opengl.util.texture.Texture;
import java.io.File;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import mouserunner.Managers.ConfigManager;
import mouserunner.Managers.TextureManager;
import mouserunner.System.Viewable;

/**
 * This class simulates a sky that is drawn behind the game board
 * during gameplay to enhance the feeling of being in the intended
 * enviroment
 * @author Zorek
 */
public class Skyplane implements Viewable {

	private String texturePath;
	private float[] offset;
	private Camera camera;

	/**
	 * Constructor to create a new skyplane to use in game
	 * @param texturePath the path to the skyplane texture
	 * @param camera the camera used in the game, used to get the rotation.
	 */
	public Skyplane(String texturePath, Camera camera) {
		this.texturePath = texturePath;
		offset = new float[2];
		offset[0] = 0.0f;
		offset[1] = 0.0f;
		this.camera=camera;
	}

	/**
	 * Displays and animates the skybox
	 * @param gl the current gl context
	 */
	@Override
	public void view(GL gl) {
		GLU glu = new GLU();
		
		animate();
		
		gl.glDepthMask(false);
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, 800.0 , 0.0, 600.0);
		gl.glMatrixMode(gl.GL_TEXTURE);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		//gl.glRotated(camera.rot*30/(Math.PI*2), 0.0, 0.0, 1.0f);
		gl.glTranslatef(offset[0], offset[1], 0.0f);
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glEnable(GL.GL_TEXTURE_2D);
		Texture t = TextureManager.getInstance().getTexture(new File(texturePath));
		t.bind();
		gl.glBegin(gl.GL_QUADS);
		gl.glTexCoord2i(0, 0);
		gl.glVertex2i(0, 0);
		gl.glTexCoord2i(1, 0);
		gl.glVertex2i(800, 0);
		gl.glTexCoord2i(1, 1);
		gl.glVertex2i(800, 800);
		gl.glTexCoord2i(0, 1);
		gl.glVertex2i(0, 800);
		gl.glEnd();
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(gl.GL_TEXTURE);
		gl.glPopMatrix();
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glDepthMask(true);
	}

	/**
	 * Animates the skybox
	 */
	private void animate() {
		offset[0] -= 0.001f;
		offset[1] += 0.001f;
		if (offset[0] < -1.0f) {
			offset[0] = 0.0f;
		}
		if (offset[1] > 1.0f) {
			offset[1] = 0.0f;
		}
	}
}
