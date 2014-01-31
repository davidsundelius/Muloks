package mouserunner.Menu;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import mouserunner.EventListeners.CommandListener;
import mouserunner.EventListeners.TitleKeyListener;
import mouserunner.EventListeners.TitleMouseListener;
import mouserunner.System.Command;
import mouserunner.Managers.ConfigManager;
import mouserunner.Managers.ModelManager;
import mouserunner.Managers.SoundManager;
import mouserunner.Managers.FontManager;
import mouserunner.Managers.GameplayManager;
import mouserunner.System.State;
import mouserunner.Managers.TextureManager;
import mouserunner.System.Timer;

/**
 * A state that shows a short intro while awaiting the players input
 * @author Zorek
 */
public class Title implements State {

	private Timer timer;
	private TextRenderer text;
	private final int frametime = 5000;
	private final int numFrames = 3;
	private final String message = "Press any key to continue...";
	private boolean introDone = false;
	private boolean gotoMenu = false;
	private CommandListener commandListener;
	private GLU glu;

	/**
	 * Constructs a new state
	 */
	public Title() {
		timer = new Timer();
		timer.pause();
		glu = new GLU();
		text = new TextRenderer(FontManager.getInstance().getFont("Assets/Misc/Meow.ttf", Font.BOLD, 16));
		
	}

	/**
	 * Starts the intro
	 */
	public void startIntro() {
		timer.unPause();
		SoundManager.getInstance().playSound(new File("Assets/Sound/SonicTeam.ogg"), false);
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
		
		if(i >= numFrames) {
			timer.setTimestamp();
			introDone=!introDone;
			ConfigManager.getInstance().skipIntro=introDone;
		}
		
		if (!introDone) {
				t = TextureManager.getInstance().getTexture(new File("Assets/Textures/Intro" + (i) + ".png"));
				tc = t.getSubImageTexCoords(0, 0, 800, 600);
				setAlpha((float) (timer.read() % frametime), frametime, gl);
		} else {
				t = TextureManager.getInstance().getTexture(new File("Assets/Textures/TitleScreen.png"));
				tc = t.getSubImageTexCoords(0, 0, 800, 600);
				if(timer.read() < 3000)
					setAlpha((float)(timer.read() % frametime), 6000, gl);
				else
					gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
		t.bind();

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, 800.0, 0.0, 600.0);

		gl.glMatrixMode(GL.GL_MODELVIEW);

		t.enable();
		gl.glLoadIdentity();
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(tc.left(), tc.bottom());
		gl.glVertex2i(0, 0);
		gl.glTexCoord2f(tc.right(), tc.bottom());
		gl.glVertex2i(800, 0);
		gl.glTexCoord2f(tc.right(), tc.top());
		gl.glVertex2i(800, 600);
		gl.glTexCoord2f(tc.left(), tc.top());
		gl.glVertex2i(0, 600);
		gl.glEnd();

		gl.glFlush();

		//Preload models
		t.disable();
		if (timer.read() < 5000 && !introDone) {
			text.beginRendering(800, 600);
			text.setColor(new Color(30, 30, 30));
			text.draw("Loading...", 650, 50);
			text.endRendering();
			ModelManager.getInstance().getModel(new File("Assets/Models/AAMouse.ms3d"), false);
			ModelManager.getInstance().getModel(new File("Assets/Models/StandardCat.ms3d"), true);
			ModelManager.getInstance().getModel(new File("Assets/Models/Wall.ms3d"), false);
			ModelManager.getInstance().getModel(new File("Assets/Models/Rocket.ms3d"), false);
		} else if(introDone) {
			text.beginRendering(800, 600);
			text.setColor(new Color(30, 30, 30));
			text.draw(message, (int)(400-text.getBounds(message).getWidth()/2), 200);
			text.endRendering();
		}

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
	 * Updates the title screen (checks if the player has made input)
	 * @return always false
	 */
	@Override
	public boolean update() {
		if (gotoMenu) {
			commandListener.commandPerformed(Command.GOTOMENU);
		} else if (ConfigManager.getInstance().debug) {
			GameplayManager.getInstance().newGame("Classic.rls");
			GameplayManager.getInstance().loadDebugTournament();
			commandListener.commandPerformed(Command.NEWGAME);
		} else if(ConfigManager.getInstance().skipIntro && !introDone) {
			skipIntro();
		}
		return false;
	}

	/**
	 * Creates all eventlisteners that the title screen uses
	 * @return A collection with all listeners used by Title
	 */
	@Override
	public Collection<EventListener> getListeners() {
		Collection<EventListener> list = new ArrayList<EventListener>();
		CommandListener cl = new CommandListener();
		TitleKeyListener tkl = new TitleKeyListener(this);
		TitleMouseListener tml = new TitleMouseListener(this);
		list.add(cl);
		list.add(tml);
		list.add(tkl);
		return list;
	}

	/**
	 * Gives the commandlistener information of this session of titlescreen
	 * @param cl the commandlistener that need the information
	 */
	@Override
	public void setCommandListener(CommandListener cl) {
		commandListener = cl;
	}

	/**
	 * Skips the intro at next update
	 */
	public void skipIntro() {
		if (timer.read() > 5000 || introDone) {
			if(introDone) {
				ConfigManager.getInstance().skipIntro=true;
				ConfigManager.getInstance().saveSettings();
				gotoMenu=true;
			} else {
				introDone = true;
				timer.setTimestamp();
			}
		}
	}
	
	@Override
	public String toString() {
		return "Intro";
	}
}

