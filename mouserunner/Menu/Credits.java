package mouserunner.Menu;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import mouserunner.EventListeners.CommandListener;
import mouserunner.EventListeners.CreditsKeyListener;
import mouserunner.EventListeners.CreditsMouseListener;
import mouserunner.System.Command;
import mouserunner.Managers.ConfigManager;
import mouserunner.Managers.SoundManager;
import mouserunner.System.State;
import mouserunner.Managers.TextureManager;
import mouserunner.System.Timer;

/**
 * A state that shows the credits while awaiting the players input
 * @author Zorek
 */
public class Credits implements State {

	private Timer timer;
	private final int frametime = 5000;
	private final int numFrames = 3;
	private boolean creditsDone = false;
	private CommandListener commandListener;
	private GLU glu;

	/**
	 * Constructs a new state
	 */
	public Credits() {
		timer = new Timer();
		glu = new GLU();
		SoundManager.getInstance().playSound(new File("Assets/Sound/Title.ogg"), false);
	}

	/**
	 * Renders the state to the screen (in orthomode)
	 * @param gl the current gl context
	 */
	@Override
	public void view(GL gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		Texture t = null;
		TextureCoords tc = null;
		int i = timer.read() / frametime;
		if (i < numFrames) {
			t = TextureManager.getInstance().getTexture(new File("Assets/Textures/Credits" + (i) + ".png"));
			tc = t.getSubImageTexCoords(0, 0, 800, 600);
			setAlpha((float) (timer.read() % frametime), frametime, gl);
		} else {
			creditsDone=true;
			return;
		}
		t.bind();

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, ConfigManager.getInstance().width, 0.0, ConfigManager.getInstance().height);

		gl.glMatrixMode(GL.GL_MODELVIEW);

		gl.glLoadIdentity();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(tc.left(), tc.bottom());
		gl.glVertex2i(0, 0);
		gl.glTexCoord2f(tc.right(), tc.bottom());
		gl.glVertex2i(ConfigManager.getInstance().width, 0);
		gl.glTexCoord2f(tc.right(), tc.top());
		gl.glVertex2i(ConfigManager.getInstance().width, ConfigManager.getInstance().height);
		gl.glTexCoord2f(tc.left(), tc.top());
		gl.glVertex2i(0, ConfigManager.getInstance().height);
		gl.glEnd();

		gl.glFlush();

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	/**
	 * Internal method for calculating the alpha value depending on time
	 * @param time the time since last picture
	 * @param maxtime the time when the picture will be alpha 1.0f
	 * @param gl the current gl context
	 */
	private void setAlpha(float time, float maxtime, GL gl) {
		float alpha = 0.0f;
		if (time < maxtime / 4) {
			alpha = time / (maxtime / 4);
		} else if (time > maxtime * 3 / 4) {
			alpha = (maxtime - time) / (maxtime / 4);
		} else {
			alpha = 1.0f;
		}
		gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
	}

	/**
	 * Updates the credits state (checks if the player has made input)
	 * @return always false
	 */
	@Override
	public boolean update() {
		if (creditsDone) {
			commandListener.commandPerformed(Command.GOTOMENU);
		}
		return false;
	}

	/**
	 * Creates all eventlisteners that the credits state uses
	 * @return a collection with all listeners used by Credits
	 */
	@Override
	public Collection<EventListener> getListeners() {
		Collection<EventListener> list = new ArrayList<EventListener>();
		CommandListener cl = new CommandListener();
		CreditsKeyListener ckl = new CreditsKeyListener(this);
		CreditsMouseListener cml = new CreditsMouseListener(this);
		list.add(cl);
		list.add(cml);
		list.add(ckl);
		return list;
	}

	/**
	 * Gives the commandlistener information of this session of credits
	 * @param cl the commandlistener that need the information
	 */
	@Override
	public void setCommandListener(CommandListener cl) {
		commandListener = cl;
	}

	/**
	 * Skips the credits at next update
	 */
	public void skipCredits() {
		creditsDone = true;
	}
	
	@Override
	public String toString() {
		return "Credits";
	}
}

